package cert;

import lombok.Builder;
import lombok.NonNull;

/**
 * Composite type representing a certificate profile - the combination of
 * a certificate specification version and output type.
 * <p>
 * This class encapsulates the relationship between spec versions and output types,
 * providing validation and convenience methods for common profiles.
 * <p>
 * Keeping the spec version and output type separate (rather than merging into a single enum)
 * provides better extensibility for future certificate types.
 * @param specVersion CertSpecVersion 1.1 or 2.0
 * @param outputType CertKind AC or PKC
 */
@Builder
public record CertificateProfile(@NonNull CertSpecVersion specVersion, @NonNull CertKind outputType) {
    /**
     * Validate that the profile is supported.
     *
     * @return true if the combination of spec version and output type is valid
     */
    public boolean isValid() {
        return specVersion.supportsOutputType(outputType);
    }

    /**
     * Validate and throw if invalid.
     *
     * @throws IllegalArgumentException if the profile is not valid
     */
    public void validate() {
        if (!isValid()) {
            throw new IllegalArgumentException(
                    String.format("Invalid certificate profile: %s does not support %s. %s",
                            specVersion, outputType, specVersion.getDescription()));
        }
    }

    // ========== Named Constructors for Common Profiles ==========

    /**
     * Platform Certificate v1.0 Attribute Certificate
     * @return CertificateProfile
     */
    public static CertificateProfile platformV1_0Ac() {
        return new CertificateProfile(CertSpecVersion.V1_0, CertKind.AC);
    }

    /**
     * Platform Certificate v1.0 Public Key Certificate
     * @return CertificateProfile
     */
    public static CertificateProfile platformV1_0Pkc() {
        return new CertificateProfile(CertSpecVersion.V1_0, CertKind.PKC);
    }

    /**
     * Platform Certificate v1.1 (Attribute Certificate only)
     * @return CertificateProfile
     */
    public static CertificateProfile platformV1_1() {
        return new CertificateProfile(CertSpecVersion.V1_1, CertKind.AC);
    }

    /**
     * Platform Certificate v2.0 Attribute Certificate
     * @return CertificateProfile
     */
    public static CertificateProfile platformV2_0Ac() {
        return new CertificateProfile(CertSpecVersion.V2_0, CertKind.AC);
    }

    /**
     * Platform Certificate v2.0 Public Key Certificate
     * @return CertificateProfile
     */
    public static CertificateProfile platformV2_0Pkc() {
        return new CertificateProfile(CertSpecVersion.V2_0, CertKind.PKC);
    }

    /**
     * Get the default profile (v2.0 AC) for backward compatibility
     * @return CertificateProfile
     */
    public static CertificateProfile defaultProfile() {
        return platformV2_0Ac();
    }

    /**
     * Create a profile from components, with validation.
     *
     * @param specVersion Certificate specification version
     * @param outputType  Output type (AC or PKC)
     * @return Validated CertificateProfile
     * @throws IllegalArgumentException if the combination is invalid
     */
    public static CertificateProfile of(CertSpecVersion specVersion, CertKind outputType) {
        CertificateProfile profile = new CertificateProfile(specVersion, outputType);
        profile.validate();
        return profile;
    }

    /**
     * Create a profile with defaults for null values.
     *
     * @param specVersion Certificate specification version (defaults to V2_0 if null)
     * @param outputType  Output type (defaults to AC if null)
     * @return CertificateProfile with defaults applied
     */
    public static CertificateProfile ofWithDefaults(CertSpecVersion specVersion, CertKind outputType) {
        CertSpecVersion effectiveSpec = specVersion != null ? specVersion : CertSpecVersion.V2_0;
        CertKind effectiveType = outputType != null ? outputType : CertKind.AC;
        return of(effectiveSpec, effectiveType);
    }

    /**
     * Get a human-readable description of this profile.
     * @return Description string
     */
    public String getDescription() {
        return String.format("%s %s Certificate",
                specVersion.getDescription(),
                outputType == CertKind.AC ? "Attribute" : "Public Key");
    }
}
