package paccor.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.normalization.HexNormalizer;

public class AlgorithmSupportTest {
    @Test
    void testJcaHashNameWithUnknownOid() {
        Assertions.assertThrows(InvalidAlgorithmParameterException.class,
                () -> AlgorithmSupport.jcaHashName(new ASN1ObjectIdentifier("1.2.3.4")));
    }

    @Test
    void testMgf1ParameterSpecWithUnknownJcaHashName() {
        Assertions.assertThrows(InvalidAlgorithmParameterException.class,
                () -> AlgorithmSupport.mgf1ParameterSpec("unknown"));
    }

    @Test
    void testDigestUsesOidMapping() throws Exception {
        byte[] digest = AlgorithmSupport.digest("abc".getBytes(StandardCharsets.US_ASCII), NISTObjectIdentifiers.id_sha256);
        Assertions.assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", HexNormalizer.toHexString(digest));
    }
}
