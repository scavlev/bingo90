package com.scavlev.bingo

import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll

class BingoStripFactorySpec extends Specification {

    def "should create a bingo strip consisting of 6 tickets"() {
        expect:
        BingoStripFactory.createBingoStrip().tickets().size() == 6
    }

    def "bingo strip ticket should consist of 9 columns"() {
        expect:
        BingoStripFactory.createBingoStrip().tickets().stream().allMatch({ it.columns().size() == 9 })
    }

    def "bingo strip ticket should consist of 3 rows"() {
        expect:
        BingoStripFactory.createBingoStrip().tickets().stream().allMatch({ it.rows().size() == 3 })
    }

    def "bingo strip ticket rows should have 5 numbers (non zeroes)"() {
        expect:
        BingoStripFactory.createBingoStrip().tickets().stream().allMatch({
            it.rows().stream().allMatch({
                it.count({ it != 0 }) == 5
            })
        })
    }

    def "bingo strip ticket rows should have 4 blanks (zeroes)"() {
        expect:
        BingoStripFactory.createBingoStrip().tickets().stream().allMatch({
            it.rows().stream().allMatch({
                it.count({ it == 0 }) == 4
            })
        })
    }

    @Unroll
    def "bingo strip ticket column #columnNumber should only contain values from #minValue to #maxValue"() {
        expect:
        BingoStripFactory.createBingoStrip().tickets().stream().allMatch({
            it.columns().get(columnNumber - 1)
                    .stream()
                    .filter({ it != 0 })
                    .allMatch({ it >= minValue && it <= maxValue })
        })

        where:
        columnNumber | minValue | maxValue
        1            | 1        | 9
        2            | 10       | 19
        3            | 20       | 29
        4            | 30       | 39
        5            | 40       | 49
        6            | 50       | 59
        7            | 60       | 69
        8            | 70       | 79
        9            | 80       | 90
    }

    def "bingo strip ticket columns (excluding zeroes) must be sorted in ascending order"() {
        expect:
        BingoStripFactory.createBingoStrip().tickets().stream().allMatch({
            it.columns().stream().allMatch({
                it.findAll({ it != 0 }) == it.findAll({ it != 0 }).sort(false)
            })
        })
    }

    def "there should be no duplicate numbers in entire bingo strip"() {
        expect:
        def allNumbers = BingoStripFactory.createBingoStrip().tickets().stream()
                .map({ it.numbers() })
                .flatMap(List::stream)
                .filter({ it != 0 })
                .collect()
        allNumbers == allNumbers.unique(false)
    }

    @Timeout(1)
    def "should generate 10k bingo strips in less than 1 second"() {
        expect:
        BingoStripFactory.createBingoStrips(10000)
    }

}
