package org.blockchain.crypto;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.math.BigInteger;

public class ECPublicKey {
    private final ECPublicKeyParameters publicParams;

    public ECPublicKey(byte[] serialized) {
        ECPoint point = ECParams.CURVE.getCurve().decodePoint(serialized);
        this.publicParams = new ECPublicKeyParameters(point, ECParams.DOMAIN);
    }

    public byte[] asBytes() {
        return this.publicParams.getQ().getEncoded(true);
    }

    public boolean verify(byte[] signature, byte[] hash) throws IOException {
        ASN1Sequence sig = ASN1Sequence.getInstance(signature);
        BigInteger r = ASN1Integer.getInstance(sig.getObjectAt(0)).getValue();
        BigInteger s = ASN1Integer.getInstance(sig.getObjectAt(1)).getValue();
        ECDSASigner signer = new ECDSASigner();
        signer.init(false, this.publicParams);
        return signer.verifySignature(hash, r, s);
    }
}
