package org.blockchain.consensus;

import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.storage.KVStore;
import org.blockchain.storage.MemoryTreeKVStore;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class EngineTest {

    @Test
    void advance() throws IOException {
        KVStore<Block> blockKVStore = new MemoryTreeKVStore<Block>(
                Block::asBytes, Block::fromBytes
        );
        KVStore<Transaction> txKVStore = new MemoryTreeKVStore<>(
                Transaction::asBytes, Transaction::fromBytes
        );

        ECPrivateKey key = new ECPrivateKey();
        Engine engine = new Engine(blockKVStore, txKVStore, key);
        engine.advance();
        engine.advance();
        engine.advance();
    }
}