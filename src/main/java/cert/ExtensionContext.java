package cert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import tcg.credential.ASN1Utils;
import tcg.credential.EnumWithStringValue;

@AllArgsConstructor
@Getter
public enum ExtensionContext implements EnumWithStringValue {
    authorityInfoAccess(Extension.authorityInfoAccess.getId(), "Authority Info Access", false),
    authorityKeyIdentifier(Extension.authorityKeyIdentifier.getId(), "Authority Key Identifier", false),
    basicConstraints(Extension.basicConstraints.getId(), "Basic Constraints", true),
    cRLDistributionPoints(Extension.cRLDistributionPoints.getId(), "CRL Distribution", false),
    certificatePolicies(Extension.certificatePolicies.getId(), "Certificate Policies", false),
    extendedKeyUsage(Extension.extendedKeyUsage.getId(), "Extended Key Usage", false),
    keyUsage(Extension.keyUsage.getId(), "Key Usage", true),
    subjectAlternativeName(Extension.subjectAlternativeName.getId(), "Subject Alternative Names", false),
    subjectDirectoryAttributes(Extension.subjectDirectoryAttributes.getId(), "Subject Directory Attributes", false),
    subjectKeyIdentifier(Extension.subjectKeyIdentifier.getId(), "Subject Key Identifier", false),
    targetInformation(Extension.targetInformation.getId(), "Targeting Information", true);

    @NonNull
    private final String value;
    @NonNull
    private final String displayName;
    private final boolean critical;

    /**
     * The Extension OID.
     * @return {@link ASN1ObjectIdentifier}
     */
    public final ASN1ObjectIdentifier getOid() {
        return new ASN1ObjectIdentifier(value);
    }

    /**
     * Criticality of the extension.
     * @return True/false
     */
    public final boolean isCritical() {
        return critical;
    }

    /**
     * Criticality of the extension.
     * @return ASN1Boolean
     */
    public final ASN1Boolean isCriticalAsn1() {
        return ASN1Utils.getBoolean(critical);
    }

}
