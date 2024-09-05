package org.blockchain.crypto;


import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ECKeyTest {

    @Test
    public void testGenerateVerify() throws IOException {
        byte[] msg = "test".getBytes();
        ECPrivateKey key1 = new ECPrivateKey();
        ECPrivateKey key2 = new ECPrivateKey();
        byte[] sig1 = key1.sign(msg);
        byte[] sig2 = key2.sign(msg);
        assertTrue(key1.getPublicKey().verify(sig1, msg));
        assertTrue(key2.getPublicKey().verify(sig2, msg));
        assertFalse(key1.getPublicKey().verify(sig2, msg));
        assertFalse(key2.getPublicKey().verify(sig1, msg));
    }

    @Test
    public void testSerializeVerify() throws IOException {
        byte[] msg = "test".getBytes();
        ECPrivateKey key1 = new ECPrivateKey();
        byte[] sig1 = key1.sign(msg);

        byte[] pubkeybytes = key1.getPublicKey().asBytes();
        assertTrue(new ECPublicKey(pubkeybytes).verify(sig1, msg));
    }
}