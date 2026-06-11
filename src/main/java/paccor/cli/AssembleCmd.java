package paccor.cli;

import paccor.cert.CertKind;
import paccor.cert.CertSigEncoding;
import paccor.cert.CertSpecVersion;
import paccor.cert.TbsEnvelope;
import paccor.cli.pv.CertSigEncodingConverter;
import paccor.exception.PaccorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import paccor.crypto.LocalSignatureStrategy;
import paccor.crypto.Pkcs11SignatureStrategy;
import paccor.crypto.ProvidedSignatureStrategy;
import paccor.crypto.RemoteSignatureStrategy;
import paccor.crypto.SignatureService;
import paccor.crypto.SignatureStrategy;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

/**
 * Command to assemble a Platform Certificate from a TBS envelope and a signature. The signature can be provided or
 * generated using a variety of signing modes.
 */
@Command(name = "assemble", mixinStandardHelpOptions = true, description = "Assemble the Platform Certificate")
public class AssembleCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin
    private CommonOptions common;
    @Option(names = { "-i", "--in", "--tbs" }, required = true, description = "Input to-be-signed data from JSON")
    private File inJson;
    @Option(names = { "-f", "--out" }, required = true)
    private File outFile;
    @Option(names = "--pem", description = "PEM output")
    private boolean pem;
    @Option(names = "--sig-encoding", defaultValue = "der", description = "${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})", converter={CertSigEncodingConverter.class})
    private CertSigEncoding sigEncoding = CertSigEncoding.DER;
    @Option(names = { "-P", "--issuer-cert" }, description = "Certificate containing the public key that signs the input. Required for all signing modes (detached, local, pkcs11, remote).")
    private File issuerCert;
    // Provided signature options
    @Option(names = "--signature", description = "Detached signature (Base64)")
    private String signatureB64;
    // Local options
    @Option(names = { "-k", "--local-key" }, description = "Sign locally with a private key file (PKCS#8, PKCS#1, or PKCS#12)")
    private File localKey;
    @Option(names = "--local-key-password", description = "Password for a PKCS#12 local key")
    private String localKeyPassword;
    @Option(names = "--local-key-password-file", description = "File containing the password for a PKCS#12 local key")
    private File localKeyPasswordFile;
    // PKCS#11 options
    @Option(names = "--pkcs11-module", description = "Path to PKCS#11 module (.so/.dll)")
    private File pkcs11Module;
    @Option(names = "--pkcs11-slot", description = "PKCS#11 slot list index (0 = first token)")
    private Integer pkcs11Slot;
    @Option(names = "--pkcs11-token-label", description = "PKCS#11 token label")
    private String pkcs11TokenLabel;
    @Option(names = "--pkcs11-key-alias", description = "Alias/label of private key on token")
    private String pkcs11KeyAlias;
    @Option(names = "--pkcs11-key-id", description = "Hex ID of private key on token")
    private String pkcs11KeyIdHex;
    @Option(names = "--pkcs11-pin", description = "PIN for the PKCS#11 token (alternatively use PKCS11_PIN env var)")
    private String pkcs11Pin;
    @Option(names = "--pkcs11-pin-file", description = "File containing PIN for the PKCS#11 token")
    private File pkcs11PinFile;
    // Remote signer options
    @Option(names = "--remote-url", description = "Remote signer URL")
    private String remoteUrl;
    @Option(names = "--remote-auth", description = "Remote signer auth descriptor, e.g., bearer:<token> or header:Name=Value")
    private String remoteAuth;
    @Option(names = "--remote-timeout", description = "Remote signer timeout ms", defaultValue = "15000")
    private int remoteTimeoutMs;

    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public Integer call() throws Exception {
        TbsEnvelope env = TbsEnvelope.read(inJson);
        byte[] tbs = env.decode();
        AlgorithmIdentifier algId = env.decodeAlgId();

        // Validate certificate specification version if present
        Integer certSpecErr = validateCertSpecVersion(env);
        if (certSpecErr > 0) {
            return certSpecErr;
        }

        warnIfTbsDataMismatchesType(env);

        String optErr = validateSignatureOptions();
        if (optErr != null) {
            common.printError(optErr);
            return ClientExitCodes.USAGE_ERROR.code();
        }

        SignatureStrategy strategy = decodeSignatureStrategy();
        if (tbs.length == 0 || algId == null || strategy == null) {
            writeStub(env);
            common.printInfo("Wrote assembled stub to " + outFile.getAbsolutePath());
            return ClientExitCodes.SUCCESS.code();
        }

        byte[] sig = strategy.sign(tbs, algId);
        boolean local = strategy.isLocal();

        // sig was created if the program gets to this point.
        if (isSignatureInvalid(tbs, algId, sig, this.issuerCert)) {
            common.printError("Signature verification failed");
            return ClientExitCodes.VALIDATION_FAILED.code();
        }

        byte[] outDer = CliHelper.assembleDer(tbs, algId, sig);
        byte[] out = maybeToPem(outDer, env.getType());
        writeBytes(outFile, out);
        common.printInfo("Wrote assembled credential" + (local ? " (locally signed)" : "") + " to " + outFile.getAbsolutePath());
        return ClientExitCodes.SUCCESS.code();
    }

    private SignatureStrategy decodeSignatureStrategy() {
        SignatureStrategy strategy = null;
        if (signatureB64 != null) {
            strategy = new ProvidedSignatureStrategy(signatureB64, sigEncoding);
        } else if (localKey != null) {
            strategy = new LocalSignatureStrategy(localKey, localKeyPassword, localKeyPasswordFile);
        } else if (pkcs11Module != null) {
            strategy = new Pkcs11SignatureStrategy(pkcs11Module, pkcs11Slot, pkcs11TokenLabel, pkcs11KeyAlias, pkcs11KeyIdHex, pkcs11Pin, pkcs11PinFile);
        } else if (remoteUrl != null) {
            strategy = new RemoteSignatureStrategy(remoteUrl, remoteAuth, remoteTimeoutMs);
        }
        return strategy;
    }

    /**
     *
     * @param env TbsEnvelope object
     * @return Integer to match PicoCli call() method return type.
     */
    private Integer validateCertSpecVersion(TbsEnvelope env) {
        if (env.getCertSpecVersion() == null) {
            // Default to V2_0
            env.setCertSpecVersion(CertSpecVersion.V2_0);
            return 0;
        }

        CertSpecVersion certSpec = env.getCertSpecVersion();
        CertKind type = env.getType();

        // Validate that the output type is supported by the cert spec version
        if (!certSpec.supportsOutputType(type)) {
            common.printError("Certificate specification " + certSpec + " does not support output type " + type);
            common.printError(certSpec.getDescription());
            return ClientExitCodes.VALIDATION_FAILED.code();
        }

        return 0;
    }

    /**
     * Validates the signature options provided.
     *
     * @return A {@code String} containing an error message if validation fails, or {@code null}
     *         if all signature options are valid.
     */
    private String validateSignatureOptions() {
        int modes = (int)Stream.of(
                                signatureB64,
                                localKey,
                                pkcs11Module,
                                remoteUrl)
                        .filter(Objects::nonNull)
                        .count();
        if (modes == 0) {
            return null; // stub path when neither tbs nor algId provided
        }
        if (modes != 1) {
            return "Choose exactly one signing mode: --signature, --local-key, --pkcs11-module, or --remote-url.";
        }
        if (localKey == null && (localKeyPassword != null || localKeyPasswordFile != null)) {
            return "--local-key-password and --local-key-password-file require --local-key.";
        }
        if (localKeyPassword != null && localKeyPasswordFile != null) {
            return "Choose at most one of --local-key-password or --local-key-password-file.";
        }
        if (issuerCert == null) {
            return "--issuer-cert is required for the selected signing mode.";
        }
        if (pkcs11Module != null) {
            return validatePkcs11SignatureOptions();
        }
        return null;
    }

    /**
     * Validates the signature options provided.
     *
     * @return A {@code String} containing an error message if validation fails, or {@code null}
     *         if all signature options are valid.
     */
    private String validatePkcs11SignatureOptions() {
        if (pkcs11Pin == null && pkcs11PinFile == null && System.getenv("PKCS11_PIN") == null) {
            return "PKCS#11 PIN must be provided via --pkcs11-pin, --pkcs11-pin-file, or PKCS11_PIN env.";
        }
        if (pkcs11KeyAlias == null && pkcs11KeyIdHex == null) {
            return "Provide --pkcs11-key-alias or --pkcs11-key-id.";
        }
        return null;
    }

    /**
     * Verifies the validity of a digital signature using the specified issuer certificate and algorithm identifier.
     *
     * @param tbs the data that was signed.
     * @param algId the algorithm identifier specifying the signature algorithm.
     * @param sig the provided digital signature to validate.
     * @param issuerCert the certificate of the issuer used for verifying the signature.
     * @return true if the signature is valid, false otherwise.
     */
    public static final boolean isSignatureValid(byte[] tbs, AlgorithmIdentifier algId, byte[] sig, File issuerCert) {
        return SignatureService.verifyWithCert(issuerCert, algId, tbs, sig);
    }

    /**
     * Determines whether a digital signature is invalid using the specified issuer certificate and algorithm identifier.
     *
     * @param tbs the data that was signed.
     * @param algId the algorithm identifier specifying the signature algorithm.
     * @param sig the provided digital signature to verify.
     * @param issuerCert the certificate of the issuer used for verifying the signature.
     * @return true if the signature is invalid, false otherwise.
     */
    public static final boolean isSignatureInvalid(byte[] tbs, AlgorithmIdentifier algId, byte[] sig, File issuerCert) {
        return !isSignatureValid(tbs, algId, sig, issuerCert);
    }

    private void warnIfTbsDataMismatchesType(TbsEnvelope env) {
        if (env.getTbsDerB64() != null && env.getType() == CertKind.AC && CliHelper.parsesAsPkc(env.getTbsDerB64())) {
            common.printError("WARN: Envelope type AC but TBS looks like PKC TBSCertificate");
        }
        if (env.getTbsDerB64() != null && env.getType() == CertKind.PKC && !CliHelper.parsesAsPkc(env.getTbsDerB64())) {
            common.printError("WARN: Envelope type PKC but TBS looks like AC AttributeCertificateInfo");
        }
    }

    private byte[] maybeToPem(byte[] der, CertKind type) {
        return pem ? CliHelper.bytesToPem(der, type) : der;
    }
    private void writeBytes(File f, byte[] data) throws PaccorException {
        try {
            Files.write(f.toPath(), data);
        } catch (IOException e) {
            throw new PaccorException(ClientExitCodes.RUNTIME_ERROR, "An error was encountered while attempting to write to file (" + outFile.getAbsolutePath() + ")", e);
        }
    }
    private void writeStub(TbsEnvelope env) throws PaccorException {
        try {
            Files.write(outFile.toPath(), (env.getType().name() + "\n").getBytes());
        } catch (IOException e) {
            throw new PaccorException(ClientExitCodes.RUNTIME_ERROR, "An error was encountered while attempting to write to file (" + outFile.getAbsolutePath() + ")", e);
        }
    }
}
