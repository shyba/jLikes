package org.blockchain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionOutputTest {

    @Test
    public void testFromBytes() {
        byte[] hash = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
        };
        TransactionOutput out = new TransactionOutput(hash, Long.MAX_VALUE);
        byte[] outBytes = out.asBytes();
        TransactionOutput out2 = TransactionOutput.fromBytes(outBytes);
        assertEquals(out.getAmount(), out2.getAmount());
        assertEquals(out, out2);
    }
}