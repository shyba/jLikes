package org.blockchain.model;


import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.blockchain.crypto.ECPrivateKey;
import org.blockchain.crypto.ECPublicKey;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.hyperledger.besu.ethereum.trie.MerkleTrie;
import org.hyperledger.besu.ethereum.trie.SimpleMerkleTrie;
import org.hyperledger.besu.ethereum.trie.patricia.SimpleMerklePatriciaTrie;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.function.Function;

public class Block {
    private final byte version = 0;
    private final Bytes32 transactionsRootHash;
    private final Bytes32 globalStateRootHash;
    private final Bytes32 previousHash;
    private final List<Transaction> txs;
    private final byte[] signature;
    private final byte[] signerPubkey;

    public Block(
            Bytes32 transactionsRootHash, Bytes32 globalStateRootHash, Bytes32 previousHash, byte[] signature,
            byte[] pubkey, List<Transaction> txs) {
        this.transactionsRootHash = transactionsRootHash;
        this.globalStateRootHash = globalStateRootHash;
        this.previousHash = previousHash;
        this.signature = signature;
        this.signerPubkey = pubkey;
        this.txs = txs;
    }

    public List<Transaction> getTxs() {
        return List.copyOf(txs);
    }

    public static Block buildUnsignedFromTxList(
            Bytes32 previousHash, Bytes32 globalStateRootHash, List<Transaction> txs) throws IOException {
        // builds an unsigned block
        Bytes32 txRootHash = Block.getRootHashForTransactionList(txs);
        return new Block(txRootHash, globalStateRootHash, previousHash, new byte[0], new byte[0], txs);
    }

    public boolean isSignatureValid() {
        if(this.signature.length > 0 && this.signerPubkey.length > 0) {
            ECPublicKey pubKey = new ECPublicKey(this.signerPubkey);
            try {
                return pubKey.verify(this.signature, this.getHash(true).toArray());
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public Block signed(ECPrivateKey signer) throws IOException {
        byte[] signature = signer.sign(this.getHash(true).toArray());
        return new Block(this.transactionsRootHash, this.globalStateRootHash, this.previousHash,
                signature, signer.getPublicKey().asBytes(), this.txs);
    }

    public static Bytes32 getRootHashForTransactionList(List<Transaction> txs) throws IOException {
        MerkleTrie<Bytes, Bytes> trie = new SimpleMerklePatriciaTrie<>(Function.identity());
        for(int i=0; i<txs.size(); i++) {
            Transaction tx = txs.get(i);
            trie.put(Bytes.ofUnsignedInt(i, ByteOrder.BIG_ENDIAN), tx.getTransactionHash());
        }
        return trie.getRootHash();
    }

    public byte[] asBytes() throws IOException {
        return this.asBytes(false);
    }

    public byte[] asBytes(boolean forSigning) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(this.version);
        result.write(this.transactionsRootHash.toArray());
        result.write(this.globalStateRootHash.toArray());
        result.write(this.previousHash.toArray());
        result.write(this.txs.size());
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
        }
        return result.toByteArray();
    }

    public Bytes32 getPreviousHash() {
        return previousHash;
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
