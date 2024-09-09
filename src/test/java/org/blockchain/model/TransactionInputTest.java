package org.blockchain.model;

import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionInputTest {

    @Test
    public void testFromBytes() {
        Bytes32 txHash = Bytes32.random();
        int idx = 12;
        byte[] signature = new byte[70];
        ThreadLocalRandom.current().nextBytes(signature);
        byte[] pubkeyBytes = new byte[33];
        ThreadLocalRandom.current().nextBytes(pubkeyBytes);
        TransactionInput tin = new TransactionInput(txHash, idx, signature, pubkeyBytes);

        byte[] tinBytes = tin.asBytes();
        TransactionInput tin2 = TransactionInput.fromBytes(tinBytes);
        assertArrayEquals(tin.getTxHash().toArray(), tin2.getTxHash().toArray());
        assertArrayEquals(tin.getPublicKeyBytes(), tin2.getPublicKeyBytes());
        assertArrayEquals(tin.getSignature(), tin2.getSignature());
        assertEquals(tin.getTxOutIdx(), tin2.getTxOutIdx());
        assertEquals(tin, tin2);

        byte[] unsignedBytes = new byte[TransactionInput.MIN_SIZE];
        System.arraycopy(tinBytes, 0, unsignedBytes, 0, TransactionInput.MIN_SIZE);
        TransactionInput unsignedTin = TransactionInput.fromBytes(unsignedBytes);
        assertArrayEquals(tin.getTxHash().toArray(), unsignedTin.getTxHash().toArray());
        assertArrayEquals(tin.getPublicKeyBytes(), unsignedTin.getPublicKeyBytes());
        assertEquals(0, unsignedTin.getSignature().length);
    }
}