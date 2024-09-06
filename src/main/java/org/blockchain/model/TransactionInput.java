package org.blockchain.model;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class TransactionInput {
    private static final int SIZE = 32 + 4 + 73 + 33;
    private final byte[] txHash;
    private final int txOutIdx;
    private final byte[] signature;
    private final byte[] publicKeyBytes;

    public TransactionInput(byte[] txHash, int txOutIdx, byte[] signature, byte[] publicKeyBytes) {
        assert txHash.length == 32;
        this.txHash = txHash;
        assert txOutIdx >= 0;
        this.txOutIdx = txOutIdx;
        assert signature.length <= 73;
        this.signature = signature;
        assert publicKeyBytes.length == 33;
        this.publicKeyBytes = publicKeyBytes;
    }

    public static TransactionInput fromBytes(byte[] raw) {
        byte[] txHash = new byte[32];
        System.arraycopy(raw, 0, txHash, 0, 32);
        byte[] rawIdx = new byte[4];
        System.arraycopy(raw, 32, rawIdx, 0, 4);
        int idx = ByteBuffer.wrap(rawIdx).getInt();
        byte[] signature = new byte[73];
        System.arraycopy(raw, 36, signature, 0, 73);
        byte[] pubkeyBytes = new byte[33];
        System.arraycopy(raw, 36 + 73, pubkeyBytes, 0, 33);
        return new TransactionInput(txHash, idx, signature, pubkeyBytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionInput that)) return false;
        return getTxOutIdx() == that.getTxOutIdx() && Objects.deepEquals(getTxHash(), that.getTxHash())
                && Objects.deepEquals(getSignature(), that.getSignature())
                && Objects.deepEquals(getPublicKeyBytes(), that.getPublicKeyBytes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Arrays.hashCode(getTxHash()), getTxOutIdx(),
                Arrays.hashCode(getSignature()), Arrays.hashCode(getPublicKeyBytes()));
    }

    public byte[] getTxHash() {
        return txHash;
    }

    public int getTxOutIdx() {
        return txOutIdx;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getPublicKeyBytes() {
        return publicKeyBytes;
    }

    public byte[] asBytes() {
        byte[] result = new byte[SIZE];
        System.arraycopy(this.txHash, 0, result, 0, 32);
        byte[] idxBytes = ByteBuffer.allocate(4).putInt(this.txOutIdx).array();
        System.arraycopy(idxBytes, 0, result, 36 - idxBytes.length, idxBytes.length);
        System.arraycopy(this.signature, 0, result, 36, 73);
        System.arraycopy(this.publicKeyBytes, 0, result, 36 + 73, 33);
        return result;
    }
}
