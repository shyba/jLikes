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
    class TestChain {
        public KVStore<Block> blockKVStore;
        public KVStore<Transaction> txKVStore;
        public ECPrivateKey key;
        public Engine engine;

        TestChain() {
            blockKVStore = new MemoryTreeKVStore<>(
                    Block::asBytes, Block::fromBytes
            );
            txKVStore = new MemoryTreeKVStore<>(
                    Transaction::asBytes, Transaction::fromBytes
            );

            key = new ECPrivateKey();
            engine = new Engine(blockKVStore, txKVStore, key);
        }
    }

    @Test
    void emptySimpleAdvance() throws IOException {
        TestChain chain = new TestChain();

        Engine engine = chain.engine;
        engine.advance();
        engine.advance();
        engine.advance();

        Bytes32 latestHash = engine.getLatestBlockHash();
        int expectedHeight = 3;
        while (!latestHash.equals(Bytes32.ZERO)) {
            expectedHeight -= 1;
            Block latestBlock = chain.blockKVStore.get(latestHash);
            if (latestBlock == null) {
                fail(String.format("Block %s not found!", latestHash));
            }
            for (Transaction tx : latestBlock.getTxs()) {
                if (chain.txKVStore.get(tx.getTransactionHash()) == null) {
                    fail(String.format(
                            "Transaction present in a block but missing from store: %s", tx.getTransactionHash()));
                }
            }
            latestHash = latestBlock.getPreviousHash();
        }
        assertEquals(expectedHeight, 0);
    }
}