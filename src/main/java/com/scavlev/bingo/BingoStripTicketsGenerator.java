package com.scavlev.bingo;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.binarySearch;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.*;

class BingoStripTicketsGenerator {

    static List<BingoTicket> generateBingoStripTickets() {
        return new BingoStripTicketsGenerator().generate();
    }

    private final AtomicInteger ticketCounter;
    private final Map<Integer, LinkedList<Integer>> unassignedNumberGroups;

    private BingoStripTicketsGenerator() {
        this.ticketCounter = new AtomicInteger();
        this.unassignedNumberGroups = IntStream.rangeClosed(1, 90)
                .boxed()
                .collect(toShuffledStream())
                .collect(groupingBy(BingoStripTicketsGenerator::groupByBingoTicketColumns, toCollection(ListWithSortedInsert::new)));
    }

    private static int groupByBingoTicketColumns(int number) {
        var group = BigInteger.valueOf(number).divide(BigInteger.valueOf(10)).intValue();
        return group == 9 ? 8 : group;
    }

    private static <T> Collector<T, ?, Stream<T>> toShuffledStream() {
        return Collectors.collectingAndThen(toList(), collected -> {
            shuffle(collected);
            return collected.stream();
        });
    }

    private List<BingoTicket> generate() {
        return Stream.generate(this::seedTicketColumns)
                .limit(6)
                .collect(toList())
                .stream()
                .peek(this::draftRemainingNumbers)
                .map(Map::values)
                .peek(this::spreadZeroes)
                .map(columns -> columns.stream().flatMap(List::stream).toList())
                .map(BingoTicket::new)
                .collect(toList());
    }

    private Map<Integer, ListWithSortedInsert<Integer>> seedTicketColumns() {
        var ticketColumns = new HashMap<Integer, ListWithSortedInsert<Integer>>();
        unassignedNumberGroups.forEach((k, v) -> ticketColumns.computeIfAbsent(k, key -> new ListWithSortedInsert<>()).add(v.pop()));
        return ticketColumns;
    }

    private void draftRemainingNumbers(Map<Integer, ListWithSortedInsert<Integer>> ticketColumns) {
        var ticketNumber = ticketCounter.incrementAndGet();

        if (ticketNumber < 4) {
            draftRandomNumbers(ticketColumns);
        }

        if (ticketNumber == 4) {
            if (unassignedNumberGroups.get(8).size() == 5) {
                ticketColumns.get(8).sortedInsert(unassignedNumberGroups.get(8).pop());
            }
            draftRandomNumbers(ticketColumns);
        }

        if (ticketNumber == 5) {
            unassignedNumberGroups
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().size() > 2)
                    .forEach(e -> {
                        var unassignedNumberGroup = e.getValue();
                        if (unassignedNumberGroup.size() == 4) {
                            var list = ticketColumns.get(e.getKey());
                            list.sortedInsert(unassignedNumberGroup.pop());
                            list.sortedInsert(unassignedNumberGroup.pop());
                        }
                        if (unassignedNumberGroup.size() == 3) {
                            ticketColumns.get(e.getKey()).sortedInsert(unassignedNumberGroup.pop());
                        }
                    });
            draftRandomNumbers(ticketColumns);
        }

        if (ticketNumber == 6) {
            unassignedNumberGroups.forEach((key, value) -> value.forEach(number -> ticketColumns.get(key).sortedInsert(number)));
        }
    }

    private void draftRandomNumbers(Map<Integer, ListWithSortedInsert<Integer>> ticketColumns) {
        var numbersToDraft = 15 - ticketColumns
                .values()
                .stream()
                .map(List::size)
                .reduce(0, Integer::sum);

        var firstDraft = Math.min(numbersToDraft, 3);
        var secondDraft = numbersToDraft - firstDraft;

        Stream.of(firstDraft, secondDraft)
                .filter(l -> l > 0)
                .forEach(limit -> unassignedNumberGroups
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().size() > 0 && ticketColumns.get(e.getKey()).size() < 3)
                        .collect(toShuffledStream())
                        .limit(limit)
                        .forEach(e -> ticketColumns.get(e.getKey()).sortedInsert(e.getValue().pop())));
    }

    private void spreadZeroes(Collection<ListWithSortedInsert<Integer>> columns) {
        columns.stream()
                .filter(column -> column.size() < 3)
                .collect(toShuffledStream())
                .limit(4)
                .forEach(column -> column.addFirst(0));

        var secondRowInsertCounter = new AtomicLong();
        columns.stream()
                .filter(column -> column.size() == 1)
                .forEach(column -> {
                    secondRowInsertCounter.incrementAndGet();
                    column.add(1, 0);
                    column.addLast(0);
                });

        columns.stream()
                .filter(column -> column.size() == 2)
                .collect(toShuffledStream())
                .limit(4 - secondRowInsertCounter.get())
                .forEach(column -> column.add(1, 0));

        columns.stream()
                .filter(column -> column.size() == 2)
                .forEach(column -> column.addLast(0));
    }

    private static class ListWithSortedInsert<E extends Comparable<? super E>> extends LinkedList<E> {
        void sortedInsert(E key) {
            var index = binarySearch(this, key);
            if (index < 0) index = ~index;
            this.add(index, key);
        }
    }
}
