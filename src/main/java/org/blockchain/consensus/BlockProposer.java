package org.blockchain.consensus;

import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.model.Block;
import org.blockchain.model.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockProposer {

    private final ECPrivateKey privKey;

    public BlockProposer(ECPrivateKey proposerKey) {
        this.privKey = proposerKey;
    }

    public Block proposeBlock(Bytes32 latestHash, List<Transaction> txs) throws IOException {
        Bytes32 target = this.privKey.getPublicKey().getHash();
        Transaction coinbase = Transaction.payCoinbaseTo(target);
        txs = new ArrayList<>(txs);
        txs.addFirst(coinbase);
        Block unsignedBlock = Block.buildUnsignedFromTxList(latestHash, Bytes32.ZERO, txs);
        return unsignedBlock.signed(this.privKey);
    }
}
