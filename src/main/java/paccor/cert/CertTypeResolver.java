package paccor.cert;

import java.util.Optional;
import paccor.model.CertificateReference;
import paccor.model.PlatformCertificateInformationModel;
import paccor.normalization.PlatformConfigurationNormalizer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.TCGCredentialType;
import paccor.tcg.credential.TCGObjectIdentifier;

public final class CertTypeResolver {
    private CertTypeResolver() {}

    public static ASN1ObjectIdentifier toOid(CertKind kind, CertType type) {
        if (kind == null || type == null) return null;
        return switch (kind) {
            case AC -> switch (type) {
                case BASE -> TCGObjectIdentifier.tcgKpPlatformAttributeCertificate;
                case DELTA -> TCGObjectIdentifier.tcgKpDeltaPlatformAttributeCertificate;
                case REBASE -> TCGObjectIdentifier.tcgKpAdditionalPlatformAttributeCertificate;
            };
            case PKC -> switch (type) {
                case BASE -> TCGObjectIdentifier.tcgKpPlatformKeyCertificate;
                case DELTA -> TCGObjectIdentifier.tcgKpDeltaPlatformKeyCertificate;
                case REBASE -> TCGObjectIdentifier.tcgKpAdditionalPlatformKeyCertificate;
            };
        };
    }

    public static CertType fromOid(ASN1ObjectIdentifier oid) {
        if (oid == null) return null;
        if (TCGObjectIdentifier.tcgKpDeltaPlatformAttributeCertificate.equals(oid)
                || TCGObjectIdentifier.tcgKpDeltaPlatformKeyCertificate.equals(oid)) {
            return CertType.DELTA;
        }
        if (TCGObjectIdentifier.tcgKpAdditionalPlatformAttributeCertificate.equals(oid)
                || TCGObjectIdentifier.tcgKpAdditionalPlatformKeyCertificate.equals(oid)) {
            return CertType.REBASE;
        }
        if (TCGObjectIdentifier.tcgKpPlatformAttributeCertificate.equals(oid)
                || TCGObjectIdentifier.tcgKpPlatformKeyCertificate.equals(oid)) {
            return CertType.BASE;
        }
        return null;
    }

    public static boolean isBaseOid(ASN1ObjectIdentifier oid) {
        CertType type = fromOid(oid);
        return type == CertType.BASE;
    }

    public static boolean isDeltaOid(ASN1ObjectIdentifier oid) {
        CertType type = fromOid(oid);
        return type == CertType.DELTA;
    }

    public static boolean isRebaseOid(ASN1ObjectIdentifier oid) {
        CertType type = fromOid(oid);
        return type == CertType.REBASE;
    }

    public static CertType inferCertType(PlatformCertificateInformationModel pi) {
        if (pi == null) return null;
        if (pi.getTcgCredentialType() != null) {
            CertType fromAttr = fromOid(pi.getTcgCredentialType().getCertificateType());
            if (fromAttr != null) return fromAttr;
        }
        if (Boolean.TRUE.equals(pi.getIsDelta())) {
            return CertType.DELTA;
        }
        if (PlatformConfigurationNormalizer.hasStatusTraits(pi.getPlatformConfiguration())) {
            return CertType.DELTA;
        }
        if (pi.getPreviousPlatformCertificates() != null && !pi.getPreviousPlatformCertificates().isEmpty()) {
            return CertType.REBASE;
        }
        return CertType.BASE;
    }

    public static TCGCredentialType resolveTcgCredentialType(
            PlatformCertificateInformationModel pi,
            CertKind outputType,
            CertType override,
            CertSpecVersion specVersion) {
        if (specVersion == CertSpecVersion.V1_0) {
            return null;
        }
        CertType type = (override != null) ? override : inferCertType(pi);
        ASN1ObjectIdentifier oid = toOid(outputType, type);
        return oid != null ? new TCGCredentialType(oid) : null;
    }

    /**
     * Set the TCG credential type and delta flag on the model for the certificate being generated.
     * @param pi platform model
     * @param profile specification version and output kind
     * @param override certificate type requested on the command line, or null to infer it
     * @throws IllegalArgumentException if the specification version does not support the certificate type
     */
    public static void applyDefaults(PlatformCertificateInformationModel pi, CertificateProfile profile, CertType override) {
        CertType effectiveType = Optional.ofNullable(override).orElseGet(() -> inferCertType(pi));
        boolean supports = supportsCertType(profile.specVersion(), effectiveType);
        if (!supports) {
            throw new IllegalArgumentException(profile.specVersion() + " does not support " + effectiveType + " certificates.");
        }
        TCGCredentialType resolved = resolveTcgCredentialType(pi, profile.outputType(), override, profile.specVersion());

        if (resolved != null || profile.specVersion() == CertSpecVersion.V1_0) {
            pi.setTcgCredentialType(resolved);
        }
        pi.setIsDelta(deltaFlag(pi, profile, override));
    }

    private static Boolean deltaFlag(PlatformCertificateInformationModel pi, CertificateProfile profile, CertType override) {
        if (profile.specVersion() == CertSpecVersion.V1_0) {
            return Boolean.FALSE;
        }
        return Optional.ofNullable(pi.getTcgCredentialType())
                .map(type -> isDeltaOid(type.getCertificateType()))
                .or(() -> Optional.ofNullable(pi.getIsDelta()))
                .orElseGet(() -> isDeltaCredential(pi, profile.outputType(), override));
    }

    public static boolean supportsCertType(CertSpecVersion specVersion, CertType certType) {
        if (specVersion != CertSpecVersion.V1_0 || certType == null) {
            return true;
        }
        return certType == CertType.BASE;
    }

    public static boolean isDeltaCredential(PlatformCertificateInformationModel pi, CertKind outputType, CertType override) {
        ASN1ObjectIdentifier oid = null;
        if (pi != null && pi.getTcgCredentialType() != null) {
            oid = pi.getTcgCredentialType().getCertificateType();
        }
        if (oid == null) {
            CertType type = (override != null) ? override : inferCertType(pi);
            oid = toOid(outputType, type);
        }
        return isDeltaOid(oid);
    }

    public static ASN1ObjectIdentifier toTraitCategory(CertType type) {
        if (type == null) return null;
        return switch (type) {
            case BASE -> TCGObjectIdentifier.tcgTrCatPlatformCertificate;
            case DELTA -> TCGObjectIdentifier.tcgTrCatDeltaPlatformCertificate;
            case REBASE -> TCGObjectIdentifier.tcgTrCatRebasePlatformCertificate;
        };
    }

    public static ASN1ObjectIdentifier toTraitCategory(PlatformCertificate certificate) {
        if (certificate == null) {
            return null;
        }
        return toTraitCategory(certificate.getCertType());
    }

    public static CertificateReference toReference(PlatformCertificate certificate) {
        if (certificate == null) {
            return null;
        }
        ASN1ObjectIdentifier category = toTraitCategory(certificate);
        if (category == null) {
            category = TCGObjectIdentifier.tcgTrCatPlatformCertificate;
        }
        return certificate.toReference(category);
    }
}
