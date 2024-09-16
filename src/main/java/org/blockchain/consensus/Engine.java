package org.blockchain.consensus;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.Parameters;
import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.crypto.ECPublicKey;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.model.TransactionInput;
import org.blockchain.model.TransactionOutput;
import org.blockchain.storage.KVStore;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Engine {
    private final BlockValidator validator;
    private final KVStore<Block> blockStore;
    private final BlockProposer proposer;
    private final KVStore<Transaction> txStore;
    private final KVStore<TransactionOutput> utxoStore;
    private final List<Transaction> mempool;
    private Block latest;
    private long maxBlockSize;
    private Logger logger = Logger.getLogger(Engine.class.getName());

    public Engine(
            KVStore<Block> blockStore, KVStore<Transaction> txStore,
            KVStore<TransactionOutput> utxoStore, ECPrivateKey proposerKey) {
        this.blockStore = blockStore;
        this.proposer = new BlockProposer(proposerKey);
        this.validator = new BlockValidator(blockStore, txStore);
        this.txStore = txStore;
        this.mempool = new LinkedList<>();
        this.utxoStore = utxoStore;
        this.maxBlockSize = Parameters.MAX_BLOCK_SIZE_BYTES;
    }

    public Bytes32 getLatestBlockHash() {
        try {
            return latest.getHash();
        } catch (IOException e) {
            return null;
        }
    }

    public void setMaxBlockSize(long maxBlockSize) {
        this.maxBlockSize = maxBlockSize;
    }

    public void advance() throws IOException {
        this.ensureLatest();
        logger.info("Advancing to the next block. Current: "
                + (this.latest != null ? this.latest.getHash().toShortHexString() : "NO BLOCK"));
        if (this.latest == null) {
            this.acceptBlock(this.proposer.proposeBlock(Bytes32.ZERO, List.of()));
        } else {
            Block block = this.proposer.proposeBlock(this.latest.getHash(), mempool);
            int size = this.mempool.size();
            while (block.asBytes().size() > this.maxBlockSize) {
                size -= 1;
                block = this.proposer.proposeBlock(this.latest.getHash(), mempool.subList(0, size));
            }
            if (size >= 0) {
                this.mempool.subList(0, size).clear();
            }
            this.acceptBlock(block);
        }
    }

    public void submitTransaction(Transaction tx) throws Exception {
        if (!tx.verifySignatures()) throw new Exception("Invalid tx signature");
        if (this.txStore.get(tx.getTransactionHash()) != null) throw new Exception("Transaction already confirmed");
        long totalInputValue = 0;
        for (Transaction mempoolTx : this.mempool) {
            if (mempoolTx.getTransactionHash().equals(tx.getTransactionHash()))
                throw new Exception("Transaction already in mempool");
        }

        for (TransactionInput input : tx.getInputs()) {
            Transaction referencedTx = this.txStore.get(input.getTxHash());
            boolean onMempool = false;
            if (referencedTx == null) {
                for (Transaction mempoolTx : this.mempool) {
                    if (mempoolTx.getTransactionHash().equals(input.getTxHash())) {
                        referencedTx = mempoolTx;
                        onMempool = true;
                    }
                }
                if (referencedTx == null) throw new Exception("Input not found");
            }
            for (Transaction mempoolTx : this.mempool) {
                for (TransactionInput mempoolInput : mempoolTx.getInputs()) {
                    if (mempoolInput.getTxHash().equals(input.getTxHash())
                            && mempoolInput.getTxOutIdx() == input.getTxOutIdx()) {
                        throw new Exception("Mempool Input already spent");
                    }
                }
            }
            TransactionOutput referencedOut = referencedTx.getOutputs()[input.getTxOutIdx()];
            if (!onMempool && this.utxoStore.get(this.spentKey(referencedTx.getTransactionHash(), input.getTxOutIdx())) == null) {
                throw new Exception("Input already spent");
            }
            if (!referencedOut.getTargetHash().equals(new ECPublicKey(input.getPublicKeyBytes()).getHash()))
                throw new Exception("Key mismatch");
            totalInputValue += referencedOut.getAmount();
        }
        for (TransactionOutput out : tx.getOutputs()) {
            if (out.getAmount() < 0) throw new Exception("negative amount");
        }
        if (totalInputValue > tx.getTotalValue()) throw new Exception("Amount spent greater than inputs");
        this.logger.info(String.format("New transaction added to mempool: %s", tx));
        this.mempool.add(tx);
    }

    private void acceptBlock(Block block) throws IOException {
        assert this.validator.isValid(block);
        this.blockStore.put(block.getHash(), block);
        for (Transaction tx : block.getTxs()) {
            this.txStore.put(tx.getTransactionHash(), tx);
            for (TransactionInput input : tx.getInputs()) {
                if (!input.getTxHash().equals(Bytes32.ZERO))
                    this.utxoStore.remove(this.spentKey(input.getTxHash(), input.getTxOutIdx()));
            }
            for (int i = 0; i < tx.getOutputs().length; i++)
                this.utxoStore.put(this.spentKey(tx.getTransactionHash(), i), tx.getOutputs()[i]);
        }
        this.ensureLatest();
        assert this.latest.equals(block);
        logger.info(String.format("New block! Current %s previous %s, %d transactions", block.getHash().toHexString(), block.getPreviousHash().toHexString(), block.getTxs().size()));
    }

    private Bytes spentKey(Bytes32 hash, int idx) throws IOException {
        return Bytes.concatenate(hash, Bytes.ofUnsignedInt(idx));
    }

    private void ensureLatest() throws IOException {
        // Scan the db for a block that isn't referenced by any other block. Assumes no uncle blocks
        HashSet<Bytes32> referenced = new HashSet<>();
        for (Block block : this.blockStore.iter(Bytes.of())) {
            referenced.add(block.getPreviousHash());
        }
        for (Block block : this.blockStore.iter(Bytes.of())) {
            if (!referenced.contains(block.getHash())) {
                this.latest = block;
            }
        }
    }
}
