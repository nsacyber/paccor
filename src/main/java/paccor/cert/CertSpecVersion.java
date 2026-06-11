package paccor.cert;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TCGSpecificationVersion;

/**
 * Represents the TCG Platform Certificate Specification version.
 * Note: This is distinct from the PlatformConfiguration attribute version.
 * <br/>
 * V1_0: Platform Certificate Specification v1.0 (uses PlatformConfiguration, attribute or public key certificates)
 * V1_1: Platform Certificate Specification v1.1 (uses PlatformConfigurationV2, attribute certificates only)
 * V2_0: Platform Certificate Specification v2.x family (uses PlatformConfigurationV3, attribute or public key certificates)
 */
@AllArgsConstructor
@Getter
public enum CertSpecVersion {
    /**
     * Platform Certificate Specification v1.0
     * - Uses PlatformConfiguration (tcg-at-platformConfiguration-v1)
     * - Supports both attribute and public key certificates
     */
    V1_0("1.0", TCGObjectIdentifier.tcgAtPlatformConfigurationV1, true, true),

    /**
     * Platform Certificate Specification v1.1
     * - Uses PlatformConfigurationV2 (tcg-at-platformConfiguration-v2)
     * - Attribute certificates only
     */
    V1_1("1.1", TCGObjectIdentifier.tcgAtPlatformConfigurationV2, true, false),

    /**
     * Platform Certificate Specification v2.x - At least 2.0 and 2.1.
     * - Uses PlatformConfigurationV3 (tcg-at-platformConfiguration-v3)
     * - Supports both attribute and public key certificates
     */
    V2_0("2.0", TCGObjectIdentifier.tcgAtPlatformConfigurationV3, true, true);

    private final String versionString;
    private final ASN1ObjectIdentifier platformConfigOid;
    private final boolean supportsAttributeCertificates;
    private final boolean supportsPublicKeyCertificates;

    /**
     * Parse a string to a CertSpecVersion.
     * @param value The version string (e.g., "1.1", "2.0", "V1_1", "V2_0")
     * @return The corresponding CertSpecVersion
     * @throws IllegalArgumentException if the version string is not recognized
     */
    public static final CertSpecVersion fromString(String value) {
        if (value == null) {
            return V2_0; // default
        }
        String normalized = value.toUpperCase().replace(".", "_");
        if (!normalized.startsWith("V")) {
            normalized = "V" + normalized;
        }
        if (normalized.startsWith("V2_1")) {
            return V2_0;
        }
        return CertSpecVersion.valueOf(normalized);
    }

    public static final CertSpecVersion fromTcgSpecVersion(TCGSpecificationVersion version) {
        if (version == null) {
            return null;
        }
        BigInteger major = version.getMajorVersion().getValue();
        BigInteger minor = version.getMinorVersion().getValue();
        if (BigInteger.ONE.equals(major) && BigInteger.ZERO.equals(minor)) {
            return V1_0;
        }
        if (BigInteger.ONE.equals(major) && BigInteger.ONE.equals(minor)) {
            return V1_1;
        }
        if (BigInteger.valueOf(2L).equals(major)) {
            return V2_0;
        }
        return null;
    }

    /**
     * Check if the given output type is supported by this certificate specification version.
     * @param outputType The output type (AC or PKC)
     * @return true if supported, false otherwise
     */
    public boolean supportsOutputType(CertKind outputType) {
        return switch (outputType) {
            case AC -> supportsAttributeCertificates;
            case PKC -> supportsPublicKeyCertificates;
        };
    }

    /**
     * Get a human-readable description of this version.
     * @return Description string
     */
    public String getDescription() {
        return switch (this) {
            case V1_0 -> "Platform Certificate v1.0 (PlatformConfigurationV1, AC or PKC)";
            case V1_1 -> "Platform Certificate v1.1 (PlatformConfigurationV2, AC only)";
            case V2_0 -> "Platform Certificate v2.x (PlatformConfigurationV3, AC or PKC)";
        };
    }

    @Override
    public String toString() {
        return versionString;
    }
}
