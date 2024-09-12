package org.blockchain.model;

import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.crypto.ECPrivateKey;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    void testSignedBlockSerialization() throws IOException {
        ECPrivateKey myPrivKey = new ECPrivateKey();
        Transaction coinbase = Transaction.payCoinbaseTo(myPrivKey.getPublicKey().getHash());
        Transaction spend = Transaction.payCoinbaseTo(myPrivKey.getPublicKey().getHash());

        Bytes32 globalStateRootHash = Bytes32.random();
        Bytes32 previousHash = Bytes32.ZERO;

        Block block = Block.buildUnsignedFromTxList(previousHash, globalStateRootHash, List.of(coinbase, spend));
        assertFalse(block.isSignatureValid());

        block = block.signed(myPrivKey);
        assertTrue(block.isSignatureValid());
    }
}