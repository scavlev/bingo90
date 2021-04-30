package com.scavlev.bingo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Command(name = "Bingo Ticket Strip Generator",
        description = "Generates Bingo 90 ticket strips! Prints them too!",
        version = "1.33.7",
        showDefaultValues = true,
        mixinStandardHelpOptions = true)
public class Application implements Callable<Integer> {

    @Option(names = {"-a", "--amount"},
            description = "Amount of ticket strips to generate.")
    int amount = 1;

    @Option(names = {"-r", "--repeat"},
            description = "Repeats the generation additional times.")
    int repeat = 0;

    @Option(names = {"-q", "--quiet"},
            description = "Suppresses printing tickets to console.")
    boolean quiet;

    @Override
    public Integer call() {
        do {
            var start = Instant.now();
            var bingoStrips = BingoStripFactory.createBingoStrips(amount);
            var end = Instant.now();

            var plural = bingoStrips.size() == 1 ? "strip" : "strips";
            System.out.println(Ansi.AUTO.string(format("@|bold,italic,underline Generated %d bingo %s in %d ms.|@",
                    bingoStrips.size(),
                    plural,
                    Duration.between(start, end).toMillis())));

            if (!quiet) {
                prettyPrintStrips(bingoStrips);
            }
        } while (repeat-- > 0);

        return 0;
    }

    private void prettyPrintStrips(List<BingoStrip> strips) {
        var stripCounter = new AtomicInteger();
        strips.forEach(strip -> {
            System.out.println(Ansi.AUTO.string(format("%n| @|bold,cyan Strip %d|@", stripCounter.incrementAndGet())));
            var ticketCounter = new AtomicInteger();
            strip.tickets().forEach(ticket -> {
                System.out.println(Ansi.AUTO.string(format("| ∟> @|bold,yellow Ticket %d|@", ticketCounter.incrementAndGet())));
                ticket.rows().forEach(row -> {
                    var line = row.stream()
                            .map(String::valueOf)
                            .map(n -> n.equals("0") ? "" : n)
                            .map(n -> format("@|bold,green %-2s|@", n))
                            .map(Ansi.AUTO::string)
                            .collect(Collectors.joining("│"));
                    System.out.printf("|    ∟> │%s│%n", line);
                });
            });
        });
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }
}
