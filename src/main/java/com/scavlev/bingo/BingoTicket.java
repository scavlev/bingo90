package com.scavlev.bingo;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

record BingoTicket(List<Integer> numbers) {

    List<List<Integer>> columns() {
        return IntStream.range(0, 9)
                .mapToObj(it -> numbers().subList(it * 3, Math.min((it + 1) * 3, numbers.size())))
                .collect(toList());
    }

    List<List<Integer>> rows() {
        return IntStream.range(0, 3)
                .mapToObj(it -> IntStream.iterate(it, val -> val + 3)
                        .limit(9)
                        .mapToObj(numbers()::get)
                        .collect(toList()))
                .collect(toList());
    }
}