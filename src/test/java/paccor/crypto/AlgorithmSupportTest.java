package paccor.crypto;

import java.security.InvalidAlgorithmParameterException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
