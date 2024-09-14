package org.blockchain.consensus;

import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.storage.KVStore;
import org.blockchain.storage.MemoryTreeKVStore;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class EngineTest {

    @Test
    void emptySimpleAdvance() throws IOException {
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

        Bytes32 latestHash = engine.getLatestBlockHash();
        int expectedHeight = 3;
        while (!latestHash.equals(Bytes32.ZERO)) {
            expectedHeight -= 1;
            Block latestBlock = blockKVStore.get(latestHash);
            if (latestBlock == null) {
                fail(String.format("Block %s not found!", latestHash));
            }
            latestHash = latestBlock.getPreviousHash();
        }
        assertEquals(expectedHeight, 0);
    }
}