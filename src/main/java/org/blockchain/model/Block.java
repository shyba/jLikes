package org.blockchain.model;


import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.crypto.ECPrivateKey;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.hyperledger.besu.ethereum.trie.verkle.SimpleVerkleTrie;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class Block {
    private final byte version = 0;
    private final Bytes32 rootHash;
    private final Bytes32 previousHash;
    private final List<Transaction> txs;
    private final byte[] signature;
    private final byte[] signerPubkey;

    public Block(Bytes32 rootHash, Bytes32 previousHash, byte[] signature, byte[] pubkey, List<Transaction> txs) {
        this.rootHash = rootHash;
        this.previousHash = previousHash;
        this.signature = signature;
        this.signerPubkey = pubkey;
        this.txs = txs;
    }

    public static Block buildUnsignedFromTxList(Bytes32 previousHash, List<Transaction> txs) throws IOException {
        // builds an unsigned block
        Bytes32 rootHash = Block.getRootHashForTransactionList(txs);
        return new Block(rootHash, previousHash, new byte[0], new byte[0], txs);
    }

    public Block signed(ECPrivateKey signer) throws IOException {
        byte[] signature = signer.sign(this.getHash(true).toArray());
        return new Block(this.rootHash, this.previousHash, signature, signer.getPublicKey().asBytes(), this.txs);
    }

    public static Bytes32 getRootHashForTransactionList(List<Transaction> txs) throws IOException {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        for(Transaction tx:txs) {
            // todo: what else to use as value? maybe index?
            trie.put(tx.getTransactionHash(), tx.getTransactionHash());
        }
        return trie.getRootHash();
    }

    public byte[] asBytes() throws IOException {
        return this.asBytes(false);
    }

    public byte[] asBytes(boolean forSigning) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(this.version);
        result.write(this.rootHash.toArray());
        result.write(this.previousHash.toArray());
        for(Transaction tx:this.txs){
            byte[] rawtx = tx.asBytes();
            result.write(rawtx.length);
            result.write(rawtx);
        }
        if(!forSigning) {
            result.write(this.signature.length);
            result.write(this.signature);
            result.write(this.signerPubkey.length);
            result.write(this.signerPubkey);
            result.write(this.txs.size());
        }
        return result.toByteArray();
    }

    public Bytes32 getHash() throws IOException {
        return this.getHash(false);

    }
    public Bytes32 getHash(boolean forSigning) throws IOException {
        byte[] serialized = this.asBytes(forSigning);
        final SHA3.DigestSHA3 sha3 = new SHA3.Digest256();
        sha3.update(serialized);
        return Bytes32.wrap(sha3.digest());
    }

}
