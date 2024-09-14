package org.blockchain.consensus;

import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.storage.KVStore;
import org.blockchain.storage.MemoryTreeKVStore;
import org.junit.jupiter.api.Test;

class EngineTest {

    @Test
    void advance() {
        KVStore<Block> blockKVStore = new MemoryTreeKVStore<Block>(
                Block::asBytes, Block::fromBytes
        );
        KVStore<Transaction> txKVStore = new MemoryTreeKVStore<>(
                Transaction::asBytes, Transaction::fromBytes
        );
    }
}