package org.blockchain.consensus;

import org.apache.tuweni.bytes.Bytes;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.storage.KVStore;
import org.blockchain.storage.MemoryTreeKVStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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