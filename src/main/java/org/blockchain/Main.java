package org.blockchain;

import org.blockchain.crypto.ECPrivateKey;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        String priv = "24473924603921903401043805245225966818735256098608866349502516970708301627736";
        ECPrivateKey privkey = new ECPrivateKey(new BigInteger(priv).toByteArray());
        for(int i=0;i<100;i++){
            byte[] s = privkey.sign("hello".getBytes());
            System.out.println(Hex.toHexString(s));
            System.out.println(privkey.getPublicKey().verify(s, "hello".getBytes()));
            System.out.println(s.length);
            System.out.println(privkey.getPublicKey().asBytes().length);

        }
    }
}