package tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.encoders.Base64;

public class PublicKeyTraitTest {
    public static final ASN1ObjectIdentifier PUBLIC_KEY_1_HASHALG = NISTObjectIdentifiers.id_sha256;
    public static final String PUBLIC_KEY_1_KEY_B64 = """
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0Mp0ZLoxH2f7Vwnq+jFW
H0DAchozHE/u++DVP45X+RZCbrWKwUnIzqUd3DZE7Ey7hAK0n0wm9IqBtAD3MZRs
YOo1dRulISyzP7NCPfdQ3i0uBJg7rSSKTG0W9yOj/FoV174v0V+nuhhMh1IV63aV
gwio5s7rLilMN+Yz2qalq4gYXBb4MEb/9uLLEPDekL6MYo9TKbgglyfEzckkXAOz
QhgEtj2t4bov/sUGZc7SSDv/uYH5/pRPBM5s1NcyomyG9BhnXkvQuWRWKBa5TjsA
TZRKac4LWc4YX8Mr5Z1dC6gSx8oWNtM5psN+3aMtJb5D4jCih2KCPb3/qRN88Hdz
MwIDAQAB""";
    public static final String PUBLIC_KEY_1_TRAIT_DESC = "Public Key Trait Test 1";
    public static final SubjectPublicKeyInfo PUBLIC_KEY_1 = new SubjectPublicKeyInfo(new AlgorithmIdentifier(PUBLIC_KEY_1_HASHALG), new DERBitString(Base64.decode(PUBLIC_KEY_1_KEY_B64)));

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test PublicKeyTrait
     */
    public static final PublicKeyTrait samplePublicKeyTrait1() {
        return PublicKeyTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(PublicKeyTraitTest.PUBLIC_KEY_1_TRAIT_DESC))
                .traitValue(PublicKeyTraitTest.PUBLIC_KEY_1)
                .build();
    }
}
