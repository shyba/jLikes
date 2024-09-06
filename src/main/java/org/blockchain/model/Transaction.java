package org.blockchain.model;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class Transaction {
    public static final byte[] COINBASE = new byte[32];

    private static int version = 0;
    private final List<TransactionInput> inputs;
    private final List<TransactionOutput> outputs;

    public Transaction(List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public TransactionInput[] getInputs() {
        return this.inputs.toArray(new TransactionInput[0]);
    }

    public TransactionOutput[] getOutputs() {
        return this.outputs.toArray(new TransactionOutput[0]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Arrays.equals(getInputs(), that.getInputs()) && Arrays.equals(getOutputs(), that.getOutputs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(getInputs()), Arrays.hashCode(getOutputs()));
    }

    public static int getVersion() {
        return version;
    }

    public boolean isCoinbase() {
        for(TransactionInput tin : this.inputs){
            if(!Arrays.equals(tin.getTxHash(), COINBASE) || tin.getTxOutIdx() != 0)
                return false;
        }
        return this.inputs.size() == 1;
    }

    public byte[] asBytes() throws IOException {
        return this.asBytes(false);
    }

    public byte[] asBytes(boolean forSigning) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(version);
        result.write(this.inputs.size());
        for(TransactionInput tin: this.inputs){
            byte[] tinBytes = forSigning ? tin.asUnsignedBytes() : tin.asBytes();
            result.write(tinBytes.length);
            result.write(tinBytes);
        }
        result.write(this.outputs.size());
        for(TransactionOutput out: this.outputs) {
            byte[] outBytes = out.asBytes();
            result.write(outBytes.length);
            result.write(outBytes);
        }
        return result.toByteArray();
    }

    public Transaction fromBytes(byte[] raw) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(raw);
        assert in.read() == 0;
        int inputListSize = in.read();
        List<TransactionInput> inputs = new ArrayList<TransactionInput>(inputListSize);
        for(int i=0; i<inputListSize; i++) {
            int size = in.read();
            inputs.add(TransactionInput.fromBytes(in.readNBytes(size)));
        }
        int outputListSize = in.read();
        List<TransactionOutput> outputs = new ArrayList<>(outputListSize);
        for(int i=0; i<outputListSize; i++) {
            int size = in.read();
            outputs.add(TransactionOutput.fromBytes(in.readNBytes(size)));
        }
        return new Transaction(inputs, outputs);
    }

    public byte[] getTransactionHash() throws IOException {
        byte[] serialized = this.asBytes();
        final SHA3.DigestSHA3 sha3 = new SHA3.Digest256();
        sha3.update(serialized);
        return sha3.digest();
    }
}
