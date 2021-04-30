package com.scavlev.bingo;

import java.util.List;

record BingoStrip(List<BingoTicket> tickets) {

    BingoStrip() {
        this(BingoStripTicketsGenerator.generateBingoStripTickets());
    }

}