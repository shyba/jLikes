package org.blockchain.consensus;

import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;
import org.blockchain.model.TransactionOutput;
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
        public KVStore<TransactionOutput> utxoKVStore;
        public ECPrivateKey key;
        public Engine engine;

        TestChain() {
            blockKVStore = new MemoryTreeKVStore<>(
                    Block::asBytes, Block::fromBytes
            );
            txKVStore = new MemoryTreeKVStore<>(
                    Transaction::asBytes, Transaction::fromBytes
            );
            utxoKVStore = new MemoryTreeKVStore<>(
                    TransactionOutput::asBytes, TransactionOutput::fromBytes
            );

            key = new ECPrivateKey();
            engine = new Engine(blockKVStore, txKVStore, utxoKVStore, key);
        }
    }

    @Test
    void emptySimpleAdvance() {
        TestChain chain = new TestChain();
        this.verifiedAdvance(chain, 1);
        this.verifiedAdvance(chain, 2);
        this.verifiedAdvance(chain, 3);
    }

    @Test
    void simpleTransfer() {
        TestChain chain = new TestChain();
        ECPrivateKey secondAccount = new ECPrivateKey();

        this.verifiedAdvance(chain, 1);
        Transaction input = chain.blockKVStore.get(chain.engine.getLatestBlockHash()).getTxs().getFirst();
        Transaction send = input.spend(chain.key, secondAccount.getPublicKey().getHash(), 5);
        try {
            chain.engine.submitTransaction(send);
        } catch (Exception e) {
            fail(e);
        }
        this.verifiedAdvance(chain, 2);
        assertEquals(2, chain.blockKVStore.get(chain.engine.getLatestBlockHash()).getTxs().size());
        this.verifiedAdvance(chain, 3);
    }

    @Test
    void doubleSpend() {
        TestChain chain = new TestChain();
        ECPrivateKey secondAccount = new ECPrivateKey();

        this.verifiedAdvance(chain, 1);
        Transaction input = chain.blockKVStore.get(chain.engine.getLatestBlockHash()).getTxs().getFirst();
        Transaction send = input.spend(chain.key, secondAccount.getPublicKey().getHash(), 5);
        try {
            chain.engine.submitTransaction(send);
        } catch (Exception e) {
            fail(e);
        }
        this.verifiedAdvance(chain, 2);
        send = input.spend(chain.key, secondAccount.getPublicKey().getHash(), 5);
        try {
            chain.engine.submitTransaction(send);
            fail("this should fail!");
        } catch (Exception e) {
            // success
        }
    }

    @Test
    void simpleMempoolTransfer() {
        TestChain chain = new TestChain();
        ECPrivateKey secondAccount = new ECPrivateKey();
        ECPrivateKey thirdAccount = new ECPrivateKey();

        this.verifiedAdvance(chain, 1);
        Transaction input = chain.blockKVStore.get(chain.engine.getLatestBlockHash()).getTxs().getFirst();
        Transaction send = input.spend(chain.key, secondAccount.getPublicKey().getHash(), 5);
        try {
            chain.engine.submitTransaction(send);
            Transaction send2 = send.spend(secondAccount, thirdAccount.getPublicKey().getHash(), 5);
            chain.engine.submitTransaction(send2);
        } catch (Exception e) {
            fail(e);
        }
        this.verifiedAdvance(chain, 2);
        assertEquals(3, chain.blockKVStore.get(chain.engine.getLatestBlockHash()).getTxs().size());
        this.verifiedAdvance(chain, 3);
    }

    @Test
    void doubleSpendFromMempool() {
        TestChain chain = new TestChain();
        ECPrivateKey secondAccount = new ECPrivateKey();
        ECPrivateKey thirdAccount = new ECPrivateKey();

        this.verifiedAdvance(chain, 1);
        Transaction input = chain.blockKVStore.get(chain.engine.getLatestBlockHash()).getTxs().getFirst();
        Transaction send = input.spend(chain.key, secondAccount.getPublicKey().getHash(), 5);
        try {
            chain.engine.submitTransaction(send);
        } catch (Exception e) {
            fail(e);
        }
        try {
            Transaction send2 = input.spend(chain.key, thirdAccount.getPublicKey().getHash(), 5);
            chain.engine.submitTransaction(send2);
            fail("double spend from mempool not detected");
        } catch (Exception e) {
            // yay
        }
    }

    void verifiedAdvance(TestChain chain, int expectedHeight) {
        try {
            chain.engine.advance();
            Bytes32 latestHash = chain.engine.getLatestBlockHash();
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

        } catch (IOException e) {
            fail(e);
        }
    }
}