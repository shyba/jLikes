package org.blockchain.model;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private static int version = 0;
    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;

    public Transaction(List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }
}
