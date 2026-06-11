package paccor.cert;

import paccor.cli.CliHelper;
import paccor.cli.CliHelper.x509type;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import paccor.model.HolderInfo;
import paccor.model.NameInfo;
import paccor.model.SubjectInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import paccor.tcg.credential.TCGObjectIdentifier;

/**
 * Resolves certificate-related information from various sources (files, envelopes, etc.).
 * This class handles:
 * - Certificate profile inference (determining AC or PKC based on holder cert)
 * - Holder resolution for Attribute Certificates
 * - Subject resolution for Public Key Certificates
 * - Issuer resolution
 */
public class CertificateResolver {

    /**
     * Infer the certificate profile from a holder/subject certificate file.
     * Returns null if the file doesn't exist or can't be read.
     *
     * @param holderCertFile Certificate file (can be AC, EK cert, or other X.509)
     * @param defaultSpecVersion Default spec version to use if not determinable
     * @return Inferred CertificateProfile, or null if not determinable
     */
    public static CertificateProfile inferProfile(File holderCertFile, CertSpecVersion defaultSpecVersion) {
        if (holderCertFile == null) {
            return null;
        }

        CertSpecVersion specVersion = defaultSpecVersion != null ? defaultSpecVersion : CertSpecVersion.V2_0;
        CertKind outputType = inferKind(holderCertFile);

        if (outputType == null) {
            return null;
        }

        return CertificateProfile.of(specVersion, outputType);
    }

    /**
     * Resolve the kind of certificate with fallbacks: explicit > inferred > envelope default > AC.
     *
     * @param certKind Explicit kind from the command line (optional)
     * @param holderCertFile Certificate file to infer from (optional)
     * @param env Existing envelope (optional)
     * @return Resolved output type
     */
    public static CertKind resolveKind(CertKind certKind, File holderCertFile, TbsEnvelope env) {
        CertKind resolved = Optional.ofNullable(certKind)
                .orElseGet(() -> inferKind(holderCertFile));
        if (resolved == null) {
            resolved = Optional.ofNullable(env != null ? env.getType() : null).orElse(CertKind.AC);
        }
        return resolved;
    }

    /**
     * Infer the kind of certificate (AC or PKC) from the holder certificate file.
     * - If holderCert is an AC, returns AC
     * - If holderCert is an X.509 with TCG EK EKU, returns AC
     * - If holderCert is a regular X.509, returns PKC
     *
     * @param holderCert Certificate file
     * @return AC or PKC, or null if not determinable
     */
    public static CertKind inferKind(File holderCert) {
        if (holderCert == null) {
            return null;
        }

        // Try to load as AC
        X509AttributeCertificateHolder ac = loadACSafe(holderCert);
        if (ac != null) {
            return CertKind.AC;
        }

        // Try to load as X.509
        X509CertificateHolder x509 = loadX509Safe(holderCert);
        if (x509 != null) {
            // Check for TCG EK extended key usage
            if (hasTcgEkExtendedKeyUsage(x509)) {
                return CertKind.AC;
            }
            return CertKind.PKC;
        }

        return null;
    }

    /**
     * Check if a key certificate has TCG EK extended key usage.
     *
     * @param cert X.509 certificate
     * @return true if cert has TCG EK EKU
     */
    public static final boolean hasTcgEkExtendedKeyUsage(X509CertificateHolder cert) {
        return Optional.ofNullable(cert.getExtensions())
                .filter(extensions -> extensions.getExtension(Extension.extendedKeyUsage) != null)
                .map(extensions ->
                        ExtendedKeyUsage.getInstance(
                                extensions.getExtensionParsedValue(Extension.extendedKeyUsage)))
                .map(ExtendedKeyUsage::getUsages)
                .stream()
                .flatMap(Arrays::stream)
                .anyMatch(item -> item.getId().equals(TCGObjectIdentifier.tcgKpEkCertificate.getId()));
    }

    /**
     * Resolve holder information from certificate files for Attribute Certificates.
     * @param ekCertFile Endorsement Key certificate file (X.509)
     * @param acCertFile Attribute certificate file
     * @return HolderInfo with ASN.1 Holder, or null if not resolvable
     */
    public static HolderInfo resolveHolder(File ekCertFile, File acCertFile) {
        X509CertificateHolder ekCert = loadX509Safe(ekCertFile);
        X509AttributeCertificateHolder acCert = loadACSafe(acCertFile);
        return resolveHolder(ekCert, acCert);
    }

    /**
     * Resolve holder information from certificate files for Attribute Certificates.
     * @param ekCert Endorsement Key certificate
     * @param acCert Attribute certificate
     * @return HolderInfo with ASN.1 Holder, or null if not resolvable
     */
    public static HolderInfo resolveHolder(
            X509CertificateHolder ekCert,
            X509AttributeCertificateHolder acCert) {
        Holder holderAsn1 = resolveHolderAsn1(ekCert, acCert);
        return toHolderInfo(holderAsn1);
    }

    /**
     * Resolve holder information from a PlatformCertificate.
     * @param certificate PlatformCertificate
     * @return HolderInfo with ASN.1 Holder, or null if not resolvable
     */
    public static HolderInfo resolveHolder(PlatformCertificate certificate) {
        if (certificate == null || !certificate.isAttributeCertificate()) {
            return null;
        }
        try {
            return toHolderInfo(certificate.getAttributeCertificate().toASN1Structure().getAcinfo().getHolder());
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Resolve ASN.1 Holder from EK cert or AC cert.
     * @param ekCert Endorsement Key certificate
     * @param acCert Attribute certificate
     * @return HolderInfo with ASN.1 Holder, or null if not resolvable
     */
    static Holder resolveHolderAsn1(X509CertificateHolder ekCert, X509AttributeCertificateHolder acCert) {
        return Optional.ofNullable(resolveHolderFromEkCert(ekCert))
                .orElseGet(() -> resolveHolderFromAttributeCertificate(acCert));
    }

    private static Holder resolveHolderFromEkCert(X509CertificateHolder ekCert) {
        if (ekCert == null) {
            return null;
        }
        try {
            GeneralNames issuerNames = toGeneralNames(ekCert.getIssuer());
            return new Holder(new IssuerSerial(issuerNames, ekCert.getSerialNumber()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Holder resolveHolderFromAttributeCertificate(X509AttributeCertificateHolder acCert) {
        if (acCert == null) {
            return null;
        }
        try {
            AttributeCertificateHolder ach = acCert.getHolder();
            return Optional.ofNullable(resolveHolderFromEntityNames(ach))
                    .orElseGet(() -> resolveHolderFromIssuerSerial(ach));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Holder resolveHolderFromEntityNames(AttributeCertificateHolder ach) {
        X500Name[] entityNames = ach.getEntityNames();
        if (entityNames == null || entityNames.length == 0) {
            return null;
        }
        return new Holder(new GeneralNames(toGeneralNamesArray(entityNames)));
    }

    private static Holder resolveHolderFromIssuerSerial(AttributeCertificateHolder ach) {
        if (ach.getSerialNumber() == null) {
            return null;
        }
        X500Name[] issuers = ach.getIssuer();
        if (issuers == null || issuers.length == 0) {
            return null;
        }
        return new Holder(new IssuerSerial(toGeneralNames(issuers[0]), ach.getSerialNumber()));
    }

    private static GeneralNames toGeneralNames(X500Name name) {
        return new GeneralNames(new GeneralName(GeneralName.directoryName, name));
    }

    private static GeneralName[] toGeneralNamesArray(X500Name[] names) {
        GeneralName[] generalNames = new GeneralName[names.length];
        for (int i = 0; i < names.length; i++) {
            generalNames[i] = new GeneralName(GeneralName.directoryName, names[i]);
        }
        return generalNames;
    }

    /**
     * Resolve subject information from X.509 certificate for Public Key Certificates.
     *
     * @param subjectCertFile X.509 certificate file containing subject information
     * @return SubjectInfo with subject DN and SPKI, or null if not resolvable
     */
    public static SubjectInfo resolveSubject(File subjectCertFile) {
        X509CertificateHolder cert = loadX509Safe(subjectCertFile);
        return resolveSubject(cert);
    }

    /**
     * Resolve subject information from Public Key Certificates.
     * @param cert Public Key certificate
     * @return SubjectInfo with subject DN and SPKI, or null if not resolvable
     */
    public static SubjectInfo resolveSubject(X509CertificateHolder cert) {
        if (cert == null) {
            return null;
        }

        String subjectNameDerB64 = x500ToDerB64(cert.getSubject());
        String spkiDerB64 = null;
        try {
            spkiDerB64 = Base64.toBase64String(cert.getSubjectPublicKeyInfo().getEncoded());
        } catch (Exception ignored) {}

        return SubjectInfo.builder()
                .nameInfo(NameInfo.builder().nameDerB64(subjectNameDerB64).build())
                .subjectPublicKeyInfoDerB64(spkiDerB64)
                .build();
    }

    /**
     * Resolve subject information from a PlatformCertificate.
     * @param certificate PlatformCertificate
     * @return SubjectInfo with subject DN and SPKI, or null if not resolvable
     */
    public static SubjectInfo resolveSubject(PlatformCertificate certificate) {
        if (certificate == null || !certificate.isPublicKeyCertificate()) {
            return null;
        }
        return resolveSubject(certificate.getPublicKeyCertificate());
    }

    /**
     * Resolve issuer information from an issuer certificate.
     * @param issuerCertFile Issuer X.509 certificate file
     * @return NameInfo with issuer DN, or null if not resolvable
     */
    public static NameInfo resolveIssuer(File issuerCertFile) {
        X509CertificateHolder cert = loadX509Safe(issuerCertFile);
        return resolveIssuer(cert);
    }

    /**
     * Resolve issuer information from an issuer certificate.
     * @param cert Issuer key certificate
     * @return NameInfo with issuer DN, or null if not resolvable
     */
    public static NameInfo resolveIssuer(X509CertificateHolder cert) {
        if (cert == null) {
            return null;
        }

        String issuerNameDerB64 = x500ToDerB64(cert.getSubject());
        return NameInfo.builder()
                .nameDerB64(issuerNameDerB64)
                .build();
    }

    /**
     * Resolve issuer information from an Attribute Certificate.
     * @param cert Attribute Certificate
     * @return NameInfo with issuer DN, or null if not resolvable
     */
    public static NameInfo resolveIssuer(X509AttributeCertificateHolder cert) {
        if (cert == null) {
            return null;
        }
        try {
            X500Name[] names = cert.getIssuer().getNames();
            if (names == null || names.length == 0) {
                return null;
            }
            return NameInfo.builder()
                    .nameDerB64(x500ToDerB64(names[0]))
                    .build();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Resolve issuer information from a PlatformCertificate.
     * @param certificate PlatformCertificate
     * @return NameInfo with issuer DN, or null if not resolvable
     */
    public static NameInfo resolveIssuer(PlatformCertificate certificate) {
        if (certificate == null) {
            return null;
        }
        if (certificate.isPublicKeyCertificate()) {
            return resolveIssuer(certificate.getPublicKeyCertificate());
        }
        if (certificate.isAttributeCertificate()) {
            return resolveIssuer(certificate.getAttributeCertificate());
        }
        return null;
    }

    // ========== Helper Methods ==========

    private static X509CertificateHolder loadX509Safe(File file) {
        if (file == null || !file.exists()) return null;
        try {
            return CliHelper.loadCert(file.getPath(), x509type.CERTIFICATE);
        } catch (Exception ignored) {}
        return null;
    }

    private static X509AttributeCertificateHolder loadACSafe(File file) {
        if (file == null || !file.exists()) return null;
        try {
            return CliHelper.loadCert(file.getPath(), x509type.ATTRIBUTE_CERTIFICATE);
        } catch (Exception ignored) {}
        return null;
    }

    private static String x500ToDerB64(X500Name name) {
        try {
            return Base64.toBase64String(name.getEncoded());
        } catch (Exception e) {
            return null;
        }
    }

    private static HolderInfo toHolderInfo(Holder holderAsn1) {
        if (holderAsn1 == null) {
            return null;
        }
        String holderDerB64 = null;
        try {
            holderDerB64 = Base64.toBase64String(holderAsn1.getEncoded("DER"));
        } catch (Exception ignored) {}

        return HolderInfo.builder()
                .holder(holderAsn1)
                .holderDerB64(holderDerB64)
                .build();
    }
}
