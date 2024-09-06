package org.blockchain.model;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionInputTest {

    @Test
    public void testFromBytes() {
        byte[] txHash = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
        };
        int idx = 12;
        byte[] signature = new byte[70];
        ThreadLocalRandom.current().nextBytes(signature);
        byte[] pubkeyBytes = new byte[33];
        ThreadLocalRandom.current().nextBytes(pubkeyBytes);
        TransactionInput tin = new TransactionInput(txHash, idx, signature, pubkeyBytes);

        byte[] tinBytes = tin.asBytes();
        TransactionInput tin2 = TransactionInput.fromBytes(tinBytes);
        assertArrayEquals(tin.getTxHash(), tin2.getTxHash());
        assertArrayEquals(tin.getPublicKeyBytes(), tin2.getPublicKeyBytes());
        assertArrayEquals(tin.getSignature(), tin2.getSignature());
        assertEquals(tin.getTxOutIdx(), tin2.getTxOutIdx());
        assertEquals(tin, tin2);

        byte[] unsignedBytes = new byte[TransactionInput.MIN_SIZE];
        System.arraycopy(tinBytes, 0, unsignedBytes, 0, TransactionInput.MIN_SIZE);
        TransactionInput unsignedTin = TransactionInput.fromBytes(unsignedBytes);
        assertArrayEquals(tin.getTxHash(), unsignedTin.getTxHash());
        assertArrayEquals(tin.getPublicKeyBytes(), unsignedTin.getPublicKeyBytes());
        assertEquals(0, unsignedTin.getSignature().length);
    }
}