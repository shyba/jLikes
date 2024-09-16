package org.blockchain.model;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionOutputTest {

    @Test
    public void testFromBytes() {
        Bytes32 hash = Bytes32.random();
        TransactionOutput out = new TransactionOutput(hash, Long.MAX_VALUE);
        Bytes outBytes = out.asBytes();
        TransactionOutput out2 = TransactionOutput.fromBytes(outBytes);
        assertEquals(out.getAmount(), out2.getAmount());
        assertEquals(out.getTargetHash(), out2.getTargetHash());
        assertEquals(out, out2);
    }
}