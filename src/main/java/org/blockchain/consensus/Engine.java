package org.blockchain.consensus;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.Parameters;
import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.storage.KVStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Engine {
    private final BlockValidator validator;
    private final KVStore<Block> blockStore;
    private final BlockProposer proposer;
    private final KVStore<Transaction> txStore;
    private Block latest;
    private final List<Transaction> mempool;
    private long maxBlockSize;
    public Engine(KVStore<Block> blockStore, KVStore<Transaction> txStore, ECPrivateKey proposerKey) {
        this.blockStore = blockStore;
        this.proposer = new BlockProposer(proposerKey);
        this.validator = new BlockValidator(blockStore, txStore);
        this.txStore = txStore;
        this.mempool = new LinkedList<>();
        this.maxBlockSize = Parameters.MAX_BLOCK_SIZE_BYTES;
    }

    public void setMaxBlockSize(long maxBlockSize) {
        this.maxBlockSize = maxBlockSize;
    }

    public void advance() throws IOException {
        this.ensureLatest();
        if(this.latest == null) {
            this.acceptBlock(this.proposer.proposeBlock(Bytes32.ZERO, List.of()));
        } else {
            Block block = this.proposer.proposeBlock(Bytes32.ZERO, mempool);
            int size = this.mempool.size() - 1;
            while(block.asBytes().length > this.maxBlockSize) {
                size -= 1;
                block = this.proposer.proposeBlock(Bytes32.ZERO, mempool.subList(0, size));
            }
            if (size >= 0) {
                this.mempool.subList(0, size + 1).clear();
            }
            this.acceptBlock(block);
        }
    }

    private void acceptBlock(Block block) throws IOException {
        assert this.validator.isValid(block);
        this.blockStore.put(block.getHash(), block);
        this.ensureLatest();
        assert this.latest.equals(block);
    }

    private void ensureLatest() throws IOException {
        // Scan the db for a block that isn't referenced by any other block. Assumes no uncle blocks
        HashSet<Bytes32> referenced = new HashSet<>();
        for(Block block:this.blockStore.iter(Bytes.of())){
            referenced.add(block.getPreviousHash());
        }
        for(Block block:this.blockStore.iter(Bytes.of())){
            if(!referenced.contains(block.getHash())){
                this.latest = block;
            }
        }
    }
}
