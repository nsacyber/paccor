package paccor.tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.encoders.Base64;

public class URITraitTest {
    public static final String URI_1_URI = "file://there";
    public static final String URI_1_TRAIT_DESC = "URI Trait Test 1";
    public static final ASN1ObjectIdentifier URI_1_HASHALG = PKCSObjectIdentifiers.sha512WithRSAEncryption;
    public static final String URI_1_HASHVALUE_B64 = "uYmGlSD03n9z4dLvD9ZFvnI1++uDev8F5NeApUUqQk0=";
    public static final URIReference URI_1 = URIReference.builder().uniformResourceIdentifier(new DERIA5String(URI_1_URI)).hashAlgorithm(new AlgorithmIdentifier(URI_1_HASHALG)).hashValue(new DERBitString(Base64.decode(URI_1_HASHVALUE_B64))).build();

    public static final String URI_2_URI = "https://www.example.com/certs/00000.cer";
    public static final String URI_2_TRAIT_DESC = "URI Trait Test 2";
    public static final URIReference URI_2 = URIReference.builder().uniformResourceIdentifier(new DERIA5String(URI_2_URI)).build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test URITrait
     */
    public static final URITrait sampleURITrait1() {
        return URITrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(URITraitTest.URI_1_TRAIT_DESC))
                .traitValue(URITraitTest.URI_1)
                .build();
    }

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test URITrait
     */
    public static final URITrait sampleURITrait2() {
        return URITrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(URITraitTest.URI_2_TRAIT_DESC))
                .traitValue(URITraitTest.URI_2)
                .build();
    }
}
