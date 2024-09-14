package org.blockchain.model;

import org.apache.tuweni.bytes.Bytes;
import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.crypto.ECPublicKey;
import org.apache.tuweni.bytes.Bytes32;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class Transaction {
    public static final Bytes32 COINBASE = Bytes32.ZERO;

    private static final int version = 0;
    private final List<TransactionInput> inputs;
    private final List<TransactionOutput> outputs;

    public Transaction(List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public static int getVersion() {
        return version;
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

    public boolean isCoinbase() {
        for (TransactionInput tin : this.inputs) {
            if (!tin.getTxHash().equals(COINBASE) || tin.getTxOutIdx() != 0)
                return false;
        }
        return this.inputs.size() == 1;
    }

    public Bytes asBytes() {
        return this.asBytes(false);
    }

    public Bytes asBytes(boolean forSigning) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            result.write(version);
            result.write(this.inputs.size());
            for (TransactionInput tin : this.inputs) {
                byte[] tinBytes = forSigning ? tin.asUnsignedBytes() : tin.asBytes();
                result.write(tinBytes.length);
                result.write(tinBytes);
            }
            result.write(this.outputs.size());
            for (TransactionOutput out : this.outputs) {
                byte[] outBytes = out.asBytes();
                result.write(outBytes.length);
                result.write(outBytes);
            }
            return Bytes.wrap(result.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Transaction fromBytes(Bytes raw) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(raw.toArray());
            assert in.read() == 0;
            int inputListSize = in.read();
            List<TransactionInput> inputs = new ArrayList<TransactionInput>(inputListSize);
            for (int i = 0; i < inputListSize; i++) {
                int size = in.read();
                inputs.add(TransactionInput.fromBytes(in.readNBytes(size)));
            }
            int outputListSize = in.read();
            List<TransactionOutput> outputs = new ArrayList<>(outputListSize);
            for (int i = 0; i < outputListSize; i++) {
                int size = in.read();
                outputs.add(TransactionOutput.fromBytes(in.readNBytes(size)));
            }
            return new Transaction(inputs, outputs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Transaction payCoinbaseTo(Bytes32 pubkeyHash) {
        TransactionInput tin = new TransactionInput(Transaction.COINBASE, 0, new byte[0], new byte[33]);
        TransactionOutput out = new TransactionOutput(pubkeyHash, 10);
        return new Transaction(List.of(tin), List.of(out));
    }

    public Transaction spendAllTo(ECPrivateKey ownerKey, Bytes32 targetHash) throws Exception {
        byte[] pubkey = ownerKey.getPublicKey().asBytes();
        Bytes32 ownerKeyHash = ownerKey.getPublicKey().getHash();
        long total = 0;
        List<TransactionInput> inputs = new ArrayList<>(this.outputs.size());
        for(int i=0;i<this.inputs.size();i++) {
            TransactionOutput out = this.outputs.get(i);
            if (out.getTargetHash().compareTo(ownerKeyHash) != 0) {
                throw new Exception("The given private key is not the owner of one of the outputs");
            } else {
                total += out.getAmount();
                TransactionInput in = new TransactionInput(this.getTransactionHash(), i, new byte[0], pubkey);
                inputs.add(in);
            }
        }
        TransactionOutput out = new TransactionOutput(targetHash, total);
        Transaction unsignedTx = new Transaction(inputs, List.of(out));
        return unsignedTx.sign(List.of(ownerKey));
    }

    public Bytes32 getTransactionHash() throws IOException {
        final SHA3.DigestSHA3 sha3 = new SHA3.Digest256();
        sha3.update(this.asBytes().toArray());
        return Bytes32.wrap(sha3.digest());
    }

    public long getTotalValue() {
        long totalOutput = 0;
        for (TransactionOutput output : this.outputs) totalOutput += output.getAmount();
        return totalOutput;
    }

    public boolean verifySignatures() {
        if (this.isCoinbase()) return true;
        try {
            final SHA3.DigestSHA3 sha3 = new SHA3.Digest256();
            sha3.update(this.asBytes(true).toArray());
            byte[] hash = sha3.digest();
            for (TransactionInput input : this.inputs) {
                ECPublicKey key = new ECPublicKey(input.getPublicKeyBytes());
                if (!key.verify(input.getSignature(), hash)) return false;
            }
            return true;
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public Transaction sign(List<ECPrivateKey> inputKeys) throws Exception {
        if (inputKeys.size() != this.inputs.size()) throw new Exception("Key set and input set size mismatch");

        final SHA3.DigestSHA3 sha3 = new SHA3.Digest256();
        sha3.update(this.asBytes(true).toArray());
        byte[] hash = sha3.digest();

        List<TransactionInput> signedInputs = new ArrayList<>(this.inputs.size());
        for (int i = 0; i < inputKeys.size(); i++) {
            TransactionInput unsignedInput = this.inputs.get(i);
            ECPrivateKey key = inputKeys.get(i);
            byte[] signature = key.sign(hash);
            signedInputs.add(
                    new TransactionInput(
                            unsignedInput.getTxHash(), unsignedInput.getTxOutIdx(),
                            signature, unsignedInput.getPublicKeyBytes()));
        }
        return new Transaction(signedInputs, List.of(this.getOutputs()));
    }
}
