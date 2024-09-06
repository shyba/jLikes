package org.blockchain.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
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
        assertTrue(coinbase.verify(new ArrayList<>()));
        assertEquals(10, coinbase.getTotalValue());
    }

    @Test
    void coinbaseFromBytes() throws IOException {
        byte[] target = new byte[32];
        ThreadLocalRandom.current().nextBytes(target);
        TransactionInput tin = new TransactionInput(Transaction.COINBASE, 0, new byte[0], new byte[33]);
        TransactionOutput out = new TransactionOutput(target, 10);
        Transaction coinbase = new Transaction(List.of(tin), List.of(out));

        byte[] serialized = coinbase.asBytes();
        Transaction recovered = coinbase.fromBytes(serialized);
        assertEquals(recovered, coinbase);
        assertArrayEquals(recovered.getTransactionHash(), coinbase.getTransactionHash());
    }
}