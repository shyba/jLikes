package org.blockchain.model;

import java.util.Arrays;
import java.util.List;


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

    public boolean isCoinbase() {
        for(TransactionInput tin : this.inputs){
            if(!Arrays.equals(tin.getTxHash(), COINBASE) || tin.getTxOutIdx() != 0)
                return false;
        }
        return this.inputs.size() == 1;
    }
}
