package org.blockchain.crypto;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class ECPrivateKey {
    private final ECPrivateKeyParameters params;

    public ECPrivateKey() {
        SecureRandom secureRandom = new SecureRandom();
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(ECParams.DOMAIN, secureRandom);
        generator.init(keygenParams);
        this.params = (ECPrivateKeyParameters) generator.generateKeyPair().getPrivate();
    }

    public ECPrivateKey(byte[] serialized) {
        this.params = new ECPrivateKeyParameters(new BigInteger(serialized), ECParams.DOMAIN);
    }

    public ECPublicKey getPublicKey() {
        ECPoint q = this.params.getParameters().getG().multiply(this.params.getD());
        q = this.params.getParameters().validatePublicPoint(q);
        return new ECPublicKey(new ECPublicKeyParameters(q, this.params.getParameters()).getQ().getEncoded(true));
    }

    public byte[] sign(byte[] hash) throws IOException {
        ECDSASigner signer = new ECDSASigner();
        signer.init(true, this.params);
        BigInteger[] signature = signer.generateSignature(hash);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DERSequenceGenerator seq = new DERSequenceGenerator(baos);
        seq.addObject(new ASN1Integer(signature[0]));
        seq.addObject(new ASN1Integer(signature[1]));
        seq.close();
        return baos.toByteArray();
    }
}
