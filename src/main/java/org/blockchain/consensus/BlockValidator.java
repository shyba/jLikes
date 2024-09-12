package org.blockchain.consensus;

import org.blockchain.crypto.ECPublicKey;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.model.TransactionInput;
import org.blockchain.model.TransactionOutput;
import org.blockchain.storage.KVStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockValidator {
    private final KVStore<Block> blockStore;
    private final KVStore<Transaction> txStore;

    public BlockValidator(KVStore<Block> blockStore, KVStore<Transaction> txStore) {
        this.blockStore = blockStore;
        this.txStore = txStore;
    }

    public boolean isValid(Block block) throws IOException {
        return this.isValid(block, false);
    }
    
    public boolean isValid(Block block, boolean force) throws IOException {
        if(!force && this.blockStore.get(block.getHash()) != null) {
            return true; // we assume stored blocks were checked
        }
        if(block.isSignatureValid()){
            for(Transaction tx: block.getTxs()) {
                if(!force && this.txStore.get(tx.getTransactionHash()) != null) {
                    return true;
                }
                if(tx.isCoinbase()) {
                    if(tx.getTotalValue() == 10 && tx.getInputs().length == 1) {
                        continue;
                    } else {
                        return false;
                    }
                }
                long totalInputValue = 0;
                for(TransactionInput txIn:tx.getInputs()){
                    Transaction referenced = this.txStore.get(txIn.getTxHash());
                    if(referenced == null) return false;
                    if(txIn.getTxOutIdx() >= referenced.getOutputs().length) return false;
                    TransactionOutput out = referenced.getOutputs()[txIn.getTxOutIdx()];
                    if(out.getTargetHash() != new ECPublicKey(txIn.getPublicKeyBytes()).getHash()) return false;
                    totalInputValue += out.getAmount();
                }
                for(TransactionOutput out: tx.getOutputs()){
                    if(out.getAmount() < 0) return false;
                }
                if(totalInputValue > tx.getTotalValue()) return false;
            }
        }
        return false;
    }
}
