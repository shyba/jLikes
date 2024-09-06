package org.blockchain.model;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class TransactionOutput {
    private final byte[] targetHash;
    private final long amount;

    public TransactionOutput(byte[] targetHash, long amount) {
        assert targetHash.length == 32;
        assert amount >= 0;
        this.targetHash = targetHash;
        this.amount = amount;
    }

    public static TransactionOutput fromBytes(byte[] raw) {
        byte[] targetHash = new byte[32];
        System.arraycopy(raw, 0, targetHash, 0, 32);
        byte[] rawAmount = new byte[8];
        System.arraycopy(raw, 32, rawAmount, 0, 8);
        return new TransactionOutput(targetHash, ByteBuffer.wrap(rawAmount).getLong());
    }

    public long getAmount() {
        return this.amount;
    }

    public byte[] getTargetHash() {
        return this.targetHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionOutput that)) return false;
        return Objects.deepEquals(getTargetHash(), that.getTargetHash()) && Objects.equals(getAmount(), that.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(getTargetHash()), getAmount());
    }

    public byte[] asBytes() {
        byte[] result = new byte[40];
        System.arraycopy(this.targetHash, 0, result, 0, 32);
        byte[] ambytes = ByteBuffer.allocate(8).putLong(this.amount).array();
        System.arraycopy(ambytes, 0, result, 40 - ambytes.length, ambytes.length);
        return result;
    }
}
