package paccor.cert;

import paccor.json.ExtensionsJsonHelper;
import paccor.model.ExtensionInfo;
import paccor.model.PlatformCertificateInformationModel;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.encoders.Base64;

/**
 * Helper class for managing X.509 and Attribute Certificate extensions.
 */
public class ExtensionAssembler {

    /**
     * Apply extensions to PlatformCertificateInformationModel from JSON and issuer certificate.
     *
     * @param platformInfo Platform certificate information model to receive extensions
     * @param extJson Extensions JSON helper (optional)
     * @param issuerCert Issuer certificate for AKI generation (optional)
     */
    public static void applyToPlatformInfo(PlatformCertificateInformationModel platformInfo, ExtensionsJsonHelper extJson, X509CertificateHolder issuerCert) {
        if (platformInfo == null) return;

        // Apply AKI if issuer cert is provided
        ExtensionInfo aki = generateAkiExtension(issuerCert);
        if (aki != null) {
            platformInfo.putExtension(aki);
        }

        // Apply extensions from JSON
        if (extJson != null) {
            addExtensionFromJson(platformInfo, ExtensionContext.certificatePolicies, extJson.certificatePolicies());
            addExtensionFromJson(platformInfo, ExtensionContext.authorityInfoAccess, extJson.authorityInformationAccess());
            addExtensionFromJson(platformInfo, ExtensionContext.cRLDistributionPoints, extJson.crlDistPoint());
            addExtensionFromJson(platformInfo, ExtensionContext.targetInformation, extJson.targetingInformation());
            addExtensionFromJson(platformInfo, ExtensionContext.keyUsage, extJson.keyUsage());
        }
    }

    /**
     * Generate Authority Key Identifier extension from the issuer certificate.
     *
     * @param issuerCert Issuer certificate
     * @return ExtensionInfo for AKI. Null if generation failed
     */
    private static ExtensionInfo generateAkiExtension(X509CertificateHolder issuerCert) {
        if (issuerCert == null) {
            return null;
        }

        try {
            final BcX509ExtensionUtils utils = new BcX509ExtensionUtils();
            AuthorityKeyIdentifier aki = utils.createAuthorityKeyIdentifier(issuerCert);
            String valueDerB64 = Base64.toBase64String(aki.getEncoded());

            return ExtensionInfo.builder()
                    .oid(ExtensionContext.authorityKeyIdentifier.getValue())
                    .valueDerB64(valueDerB64)
                    .critical(ExtensionContext.authorityKeyIdentifier.isCritical())
                    .name(ExtensionContext.authorityKeyIdentifier.getDisplayName())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Add an extension from JSON to the model.
     */
    private static void addExtensionFromJson(
            PlatformCertificateInformationModel platformInfo,
            ExtensionContext context,
            Encodable value) {

        if (value == null) return;

        try {
            String valueDerB64 = Base64.toBase64String(value.getEncoded());
            ExtensionInfo extInfo = ExtensionInfo.builder()
                    .oid(context.getValue())
                    .valueDerB64(valueDerB64)
                    .critical(context.isCritical())
                    .name(context.getDisplayName())
                    .build();
            platformInfo.putExtension(extInfo);
        } catch (Exception ignored) {
            // Skip malformed extension
        }
    }
}
