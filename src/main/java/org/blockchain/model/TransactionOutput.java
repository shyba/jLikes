package org.blockchain.model;

import org.apache.tuweni.bytes.Bytes32;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class TransactionOutput {
    private final byte type = 0;
    private final Bytes32 targetHash;
    private final long amount;

    public TransactionOutput(Bytes32 targetHash, long amount) {
        assert amount >= 0;
        this.targetHash = targetHash;
        this.amount = amount;
    }

    public static TransactionOutput fromBytes(byte[] raw) {
        assert raw[0] == 0;
        Bytes32 targetHash = Bytes32.secure(raw, 1);
        byte[] rawAmount = new byte[8];
        System.arraycopy(raw, 33, rawAmount, 0, 8);
        return new TransactionOutput(targetHash, ByteBuffer.wrap(rawAmount).getLong());
    }

    public byte getType() {
        return this.type;
    }

    public long getAmount() {
        return this.amount;
    }

    public Bytes32 getTargetHash() {
        return this.targetHash.copy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionOutput that)) return false;
        return Objects.deepEquals(getTargetHash(), that.getTargetHash()) && Objects.equals(getAmount(), that.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(getTargetHash().toArray()), getAmount());
    }

    public byte[] asBytes() {
        byte[] result = new byte[41];
        // skip a zero byte for the type, which is 0 here
        System.arraycopy(this.targetHash.toArray(), 0, result, 1, 32);
        byte[] ambytes = ByteBuffer.allocate(8).putLong(this.amount).array();
        System.arraycopy(ambytes, 0, result, 41 - ambytes.length, ambytes.length);
        return result;
    }
}
