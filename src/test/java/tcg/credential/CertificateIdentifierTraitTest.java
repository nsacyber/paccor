package tcg.credential;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.bc.BCObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.util.encoders.Base64;

public class CertificateIdentifierTraitTest {
    public static final ASN1ObjectIdentifier CI_1_HASHED_CI_HASHALG = NISTObjectIdentifiers.id_sha256;
    public static final String CI_1_HASHED_CI_HASHVALUE_B64 = "6S5k++IQMc/GcbCpFKTeaQCmFOO+61AZMUgqPUchDFs=";
    public static final HashedCertificateIdentifier CI_1_HASHED_CI = HashedCertificateIdentifier.builder().hashAlgorithm(new AlgorithmIdentifier(CI_1_HASHED_CI_HASHALG)).hashOverSignatureValue(new DEROctetString(Base64.decode(CI_1_HASHED_CI_HASHVALUE_B64))).build();
    public static final String CI_1_GENERIC_CI_NAME = "CN=Issuer A";
    public static final BigInteger CI_1_GENERIC_CI_SERIAL = BigInteger.valueOf(41465);
    public static final IssuerSerial CI_1_GENERIC_CI = new IssuerSerial(new X500Name(CI_1_GENERIC_CI_NAME), CI_1_GENERIC_CI_SERIAL);
    public static final String CERT_ID_1_TRAIT_DESC = "Certificate Identifier Trait Test 1";
    public static final CertificateIdentifier CI_1 = CertificateIdentifier.builder().hashedCertIdentifier(CI_1_HASHED_CI).genericCertIdentifier(CI_1_GENERIC_CI).build();

    public static final ASN1ObjectIdentifier CI_2_HASHED_CI_HASHALG = BCObjectIdentifiers.bc_pbe_sha256;
    public static final String CI_2_HASHED_CI_HASHVALUE_B64 = "NjAwM0EzMzQzMkZEOTE0QjYwMDNBMzM0MzJGRDkxNEI2MDAzQTMzNDMyRkQ5MTRCNjAwM0EzMzQzMkZEOTE0Qg==";
    public static final HashedCertificateIdentifier CI_2_HASHED_CI = HashedCertificateIdentifier.builder().hashAlgorithm(new AlgorithmIdentifier(CI_2_HASHED_CI_HASHALG)).hashOverSignatureValue(new DEROctetString(Base64.decode(CI_2_HASHED_CI_HASHVALUE_B64))).build();
    public static final String CI_2_GENERIC_CI_NAME = "C=US, ST=FL, L=Sample City 1, O=Sample Corporation 1, OU=Platform Certificate Issuer, CN=www.example.com";
    public static final BigInteger CI_2_GENERIC_CI_SERIAL = BigInteger.ONE;
    public static final IssuerSerial CI_2_GENERIC_CI = new IssuerSerial(new X500Name(CI_2_GENERIC_CI_NAME), CI_2_GENERIC_CI_SERIAL);
    public static final String CERT_ID_2_TRAIT_DESC = "Certificate Identifier Trait Test 2";
    public static final CertificateIdentifier CI_2 = CertificateIdentifier.builder().hashedCertIdentifier(CI_2_HASHED_CI).genericCertIdentifier(CI_1_GENERIC_CI).build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test CertificateIdentifierTrait
     */
    public static final CertificateIdentifierTrait sampleCertificateIdentifierTrait1() {
        return CertificateIdentifierTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(CertificateIdentifierTraitTest.CERT_ID_1_TRAIT_DESC))
                .traitValue(CertificateIdentifierTraitTest.CI_1)
                .build();
    }

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test CertificateIdentifierTrait
     */
    public static final CertificateIdentifierTrait sampleCertificateIdentifierTrait2() {
        return CertificateIdentifierTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(CertificateIdentifierTraitTest.CERT_ID_2_TRAIT_DESC))
                .traitValue(CertificateIdentifierTraitTest.CI_2)
                .build();
    }
}
