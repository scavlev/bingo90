# Bingo 90 ticket strip generator

[![Build Status](https://travis-ci.com/scavlev/bingo90.svg?branch=main)](https://travis-ci.com/scavlev/bingo90)

Command line tool to generate bingo 90 ticket strips.

## Building & Running

> :warning:  Project requires **Java version 16**.
>
> Mainly because I've used records and decided to keep them in.

> :exclamation: Following commands have `--amount 1` as an example of passing arguments to the process.
>
> Passing this particular argument is entirely optional.

### Gradle

- Build: `./gradlew build`
  - Run the built executable jar: `java -jar build/libs/bingo-all.jar --amount 1`
- Test: `./gradlew test`
- Run without building: `./gradlew run --args='--amount 1'`

### Docker

- Build: `docker build -t bingo-strip-generator .`
- Run: `docker run -it bingo-strip-generator --amount 1`

## Usage

Additional arguments can be provided as follows:

```
Usage: Bingo Ticket Strip Generator [-hqV] [-a=<amount>] [-r=<repeat>]
Generates Bingo 90 ticket strips! Prints them too!
  -a, --amount=<amount>   Amount of ticket strips to generate.
                            Default: 1
  -h, --help              Show this help message and exit.
  -q, --quiet             Suppresses printing tickets to console.
  -r, --repeat=<repeat>   Repeats the generation additional times.
                            Default: 0
  -V, --version           Print version information and exit.

```

> :warning: Do not try to request high amount of ticket strips without suppressing console output by using `-q` flag, or it will flood your console.
>
> If you want to check performance without printing all the ticket strips, use the following arguments: `-qa 10000`

## Performance

> :checkered_flag: Average time to generate 10000 bingo ticket strips after 1000 executions on a hot JVM, and a fairly average hardware is _**~250 ms.**_ :rocket:

> :warning: :wheelchair:️ Classloading and resource allocation overhead of JVM cold start will significantly bonk the performance which might lead to major deviation from average execution time for the first execution.
>
> To observe this behavior try running the program with following arguments: `-qa 10000 -r 10`
>
> It will repeat the process of ticket generation on a hot a JVM 10 times with first execution clearly taking longer than others on average for at least twice as much.

## How it works

The algorithm used to generate a bingo ticket strip can be described in 3 phases:

### Phase 1

- Generate a list of numbers from 1 to 90
- Shuffle them
- Split into 9 lists grouping them by ticket columns forming a pool of unused numbers
- Create 6 entries of 9 lists representing future tickets columns
- Draft one number from each list of unused numbers per ticket column

This way 54 numbers are allocated to the tickets columns ensuring there is at least one number in each column.

### Phase 2

This is where remaining 36 numbers are allocated.

- First 3 tickets can pick any 6 numbers from the pool of unused numbers as long as they don't go out of bounds (no more
  than 3 in a column)
- 4th ticket must take at least one number from 9th column of unused numbers if there are still 5 numbers left.
  Otherwise, it follows same rules as first 3 tickets
- 5th ticket must take 2 numbers from each column of unused numbers where there are 4 numbers left and at least 1 number
  from columns that has 3 left. Remaining numbers are drafted as usual
- 6th ticket gets all remaining numbers

Random number drafting happens in up to 2 batches with up to 3 random numbers per batch (all from different columns).

### Phase 3

This is where blank spaces (zeros) are allocated.

- 4 columns at random that has less than 3 numbers in it are picked and injected with zero at the top position
- After that, all columns with only 1 number remaining are injected with zeros at the middle and last positions
- Columns with 2 numbers at random are getting filled with zeros ensuring there are 4 total on the 2nd row
- All remaining columns that are still at 2 numbers (including zeros now) get filled with zero at the last position

After this the ticket columns are generated and valid and can be mapped to the actual ticket record.

In current implementation columns are being mapped into a flat list of numbers ordered by columns. However, it doesn't
necessary has to be like that and can be adjusted to the usage patterns of those numbers.