package paccor.cert;

import java.io.File;
import java.math.BigInteger;
import java.util.Date;
import lombok.Builder;
import org.bouncycastle.cert.X509CertificateHolder;

/**
 * Inputs for generating a to-be-signed platform certificate envelope.
 * @param attributesJson attributes JSON file, or null
 * @param componentsJson hardware manifest components JSON file, or null
 * @param extensionsJson extensions JSON file, or null
 * @param platformModelJson existing platform model JSON file, or null
 * @param inEnvelope existing envelope to merge from, or null
 * @param previousPlatformCert previous platform certificate seeding a V2.0 chain, or null
 * @param issuerCertificate issuer certificate, or null
 * @param holderCert EK certificate, or the previous platform certificate for a delta or rebase, or null
 * @param subjectKey subject public key file for public key certificates, or null
 * @param subjectDn subject distinguished name for public key certificates, or null
 * @param certKind requested output kind, or null to infer it
 * @param certType requested certificate type, or null to infer it
 * @param serial serial number, or null
 * @param notBefore start of validity, or null
 * @param notAfter end of validity, or null
 * @param sigProfile signature profile name, or null
 * @param finalizeTbs whether to require a complete to-be-signed certificate
 */
@Builder
public record CertGenRequest(
        File attributesJson,
        File componentsJson,
        File extensionsJson,
        File platformModelJson,
        File inEnvelope,
        File previousPlatformCert,
        X509CertificateHolder issuerCertificate,
        File holderCert,
        File subjectKey,
        String subjectDn,
        CertKind certKind,
        CertType certType,
        BigInteger serial,
        Date notBefore,
        Date notAfter,
        String sigProfile,
        boolean finalizeTbs) {}
