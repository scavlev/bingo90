package com.scavlev.bingo;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class BingoStripFactory {

    static BingoStrip createBingoStrip() {
        return new BingoStrip();
    }

    static List<BingoStrip> createBingoStrips(int amount) {
        return Stream
                .generate(BingoStripFactory::createBingoStrip)
                .parallel()
                .limit(amount)
                .collect(toList());
    }

}
