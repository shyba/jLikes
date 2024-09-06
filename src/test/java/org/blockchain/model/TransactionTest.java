package org.blockchain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
class TransactionTest {

    @Test
    void isCoinbase() {
        byte[] target = new byte[32];
        ThreadLocalRandom.current().nextBytes(target);
        TransactionInput tin = new TransactionInput(Transaction.COINBASE, 0, new byte[0], new byte[33]);
        TransactionOutput out = new TransactionOutput(target, 10);
        Transaction coinbase = new Transaction(List.of(tin), List.of(out));
        assertTrue(coinbase.isCoinbase());
    }
}