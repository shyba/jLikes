package org.blockchain.model;

import org.blockchain.crypto.ECPrivateKey;
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
        Transaction coinbase = Transaction.payCoinbaseTo(target);
        assertTrue(coinbase.isCoinbase());
        assertTrue(coinbase.verify(new ArrayList<>()));
        assertEquals(10, coinbase.getTotalValue());
    }

    @Test
    void coinbaseFromBytes() throws IOException {
        byte[] target = new byte[32];
        ThreadLocalRandom.current().nextBytes(target);

        Transaction coinbase = Transaction.payCoinbaseTo(target);

        byte[] serialized = coinbase.asBytes();
        Transaction recovered = Transaction.fromBytes(serialized);
        assertEquals(recovered, coinbase);
        assertArrayEquals(recovered.getTransactionHash(), coinbase.getTransactionHash());
    }

    @Test
    void spendCoinbase() {
        ECPrivateKey privateKey = new ECPrivateKey();

    }
}