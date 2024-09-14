package org.blockchain.model;

import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.crypto.ECPrivateKey;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class TransactionTest {

    @Test
    void isCoinbase() {
        Bytes32 target = Bytes32.random();
        Transaction coinbase = Transaction.payCoinbaseTo(target);
        assertTrue(coinbase.isCoinbase());
        assertTrue(coinbase.verifySignatures());
        assertEquals(10, coinbase.getTotalValue());
    }

    @Test
    void coinbaseFromBytes() throws IOException {
        Bytes32 target = Bytes32.random();
        Transaction coinbase = Transaction.payCoinbaseTo(target);

        Transaction recovered = Transaction.fromBytes(coinbase.asBytes());
        assertEquals(recovered, coinbase);
        assertArrayEquals(recovered.getTransactionHash().toArray(), coinbase.getTransactionHash().toArray());
    }

    @Test
    void spendCoinbase() throws Exception {
        ECPrivateKey privKey = new ECPrivateKey();
        Bytes32 targetHash = privKey.getPublicKey().getHash();
        Transaction coinbase = Transaction.payCoinbaseTo(targetHash);

        ECPrivateKey anotherPrivKey = new ECPrivateKey();
        Transaction payment = coinbase.spendAllTo(privKey, anotherPrivKey.getPublicKey().getHash());
        assertTrue(payment.verifySignatures());
    }
}