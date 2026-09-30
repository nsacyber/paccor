package paccor.cli;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.CertGenRequest;
import paccor.cert.CertGenService;
import paccor.cert.CertKind;
import paccor.cert.CertType;
import paccor.cert.PlatformCertificate;
import paccor.cert.TbsEnvelope;
import paccor.cli.pv.BigIntegerConverter;
import paccor.cli.pv.CertKindConverter;
import paccor.cli.pv.CertTypeConverter;
import paccor.cli.pv.DateConverter;
import paccor.cli.pv.OutFileConverter;
import paccor.cli.pv.ReadableFileConverter;
import paccor.json.ObjectMapperFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

/**
 * Generate the PlatformCertificateInformationModel using direct import or JSON data files.
 * Build the to-be-signed envelope from the model.
 */
@Command(name = "certgen", mixinStandardHelpOptions = true, description = "Generate Platform Certificate data")
public class CertGenCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin
    private CommonOptions common;

    // JSON data
    @Option(names = { CliOptionNames.ATTRIBUTES_JSON_SHORT, CliOptionNames.ATTRIBUTES_JSON_LONG }, description = "Attributes JSON file", converter = ReadableFileConverter.class)
    private File attrsJson;

    @Option(names = { CliOptionNames.COMPONENTS_JSON_SHORT, CliOptionNames.COMPONENTS_JSON_LONG }, description = "Hardware manifest components JSON file", converter = ReadableFileConverter.class)
    private File componentsJson;

    @Option(names = { CliOptionNames.EXTENSIONS_JSON_SHORT, CliOptionNames.EXTENSIONS_JSON_LONG }, description = "Extensions JSON file", converter = ReadableFileConverter.class)
    private File extJson;

    @Option(names = CliOptionNames.IN_PLATFORM_MODEL_LONG, description = "Existing model data from JSON", converter = ReadableFileConverter.class)
    private File platformInfoJson;

    @Option(names = CliOptionNames.IN_LONG, description = "Existing to-be-signed data to merge from JSON", converter = ReadableFileConverter.class)
    private File inJson;

    @Option(names = CliOptionNames.PREV_PCERT_LONG, description = "Single previous platform certificate used as the V2.0 chain seed. Use previousPlatformCertificates JSON for additional entries.")
    private String previousPlatformCert;

    // Most relevant certificates. Other certificates may be specified in the JSON.
    @Option(names = { CliOptionNames.ISSUER_CERT_SHORT, CliOptionNames.ISSUER_CERT_LONG }, description = "Issuer certificate file", converter = ReadableFileConverter.class)
    private File issuerCert;

    @Option(names = { CliOptionNames.HOLDER_CERT_SHORT, CliOptionNames.HOLDER_CERT_LONG }, description = "Holder/Subject certificate file", converter = ReadableFileConverter.class)
    private File holderCert;

    @Option(names = CliOptionNames.SUBJECT_KEY_LONG, description = "Subject public key file (DER or PEM SubjectPublicKeyInfo) for PKC output")
    private File subjectKey;

    @Option(names = CliOptionNames.SUBJECT_DN_LONG, description = "Subject distinguished name for PKC output (for example, CN=Platform,O=Example)")
    private String subjectDn;

    // Platform Certificate options required prior to finalization
    @Option(names = { CliOptionNames.CERT_KIND_LONG_ALT, CliOptionNames.CERT_KIND_LONG }, description = "Certificate output kind (AC, PKC)", converter = {CertKindConverter.class})
    private CertKind certKind;

    @Option(names = { CliOptionNames.CERT_TYPE_LONG_ALT, CliOptionNames.CERT_TYPE_LONG }, description = "Platform certificate type (base, delta, rebase)", converter = {CertTypeConverter.class})
    private CertType certType;

    @Option(names = { CliOptionNames.SERIAL_SHORT, CliOptionNames.SERIAL_LONG }, description = "Certificate serial number", converter = {BigIntegerConverter.class})
    private BigInteger serial;

    @Option(names = { CliOptionNames.NOT_BEFORE_SHORT, CliOptionNames.NOT_BEFORE_LONG }, description = DateConverter.DATE_FORMAT, converter = {DateConverter.class})
    private Date notBefore;

    @Option(names = { CliOptionNames.NOT_AFTER_SHORT, CliOptionNames.NOT_AFTER_LONG }, description = DateConverter.DATE_FORMAT, converter = {DateConverter.class})
    private Date notAfter;

    @Option(names = CliOptionNames.SIG_PROFILE_LONG, description = "Signature profile ID")
    private String sigProfile;

    // Output options
    @Option(names = { CliOptionNames.FILE_OUT_SHORT, CliOptionNames.FILE_OUT_LONG }, required = true, description = "Model data and context in JSON. Can be given to the assemble command", converter = OutFileConverter.class)
    private File outJson;

    @Option(names = CliOptionNames.FINALIZE_LONG, description = "Validate model data and context prior to output")
    private boolean finalizeFlag;

    @Option(names = CliOptionNames.OVERWRITE_IN_PLACE_LONG, description = "Allow in-place overwrite when --in equals --out.")
    private boolean overwriteInPlace;

    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public Integer call() throws Exception {
        X509CertificateHolder issuer = issuerCert == null ? null : CliHelper.loadCertSafe(issuerCert, CliHelper.x509type.CERTIFICATE);
        Optional<String> problem = usageProblem(issuer);
        if (problem.isPresent()) {
            common.printError(problem.get());
            return ClientExitCodes.USAGE_ERROR.code();
        }
        try {
            TbsEnvelope envelope = CertGenService.generate(request(issuer));
            ObjectMapperFactory.write(outJson, envelope);
            common.printInfo("Wrote TBS envelope to " + outJson.getAbsolutePath());
            return ClientExitCodes.SUCCESS.code();
        } catch (IllegalArgumentException e) {
            common.printError(e.getMessage());
            return ClientExitCodes.USAGE_ERROR.code();
        }
    }

    private CertGenRequest request(X509CertificateHolder issuer) {
        return CertGenRequest.builder()
                .attributesJson(attrsJson)
                .componentsJson(componentsJson)
                .extensionsJson(extJson)
                .platformModelJson(platformInfoJson)
                .inEnvelope(inJson)
                .previousPlatformCert(previousPlatformCertFile().orElse(null))
                .issuerCertificate(issuer)
                .holderCert(holderCert)
                .subjectKey(subjectKey)
                .subjectDn(subjectDn)
                .certKind(certKind)
                .certType(certType)
                .serial(serial)
                .notBefore(notBefore)
                .notAfter(notAfter)
                .sigProfile(sigProfile)
                .finalizeTbs(finalizeFlag)
                .build();
    }

    private Optional<String> usageProblem(X509CertificateHolder issuer) {
        return Stream.<Supplier<Optional<String>>>of(
                        this::outputPathProblem,
                        this::holderAndSubjectProblem,
                        this::previousPlatformCertProblem,
                        () -> issuerProblem(issuer))
                .map(Supplier::get)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<String> outputPathProblem() {
        boolean overwritesInput = inJson != null && outJson != null && !overwriteInPlace && sameFile(inJson, outJson);
        return overwritesInput
                ? Optional.of("Refusing to overwrite input file. Use " + CliOptionNames.OVERWRITE_IN_PLACE_LONG + " for in-place update.")
                : Optional.empty();
    }

    private static boolean sameFile(File first, File second) {
        try {
            return Files.isSameFile(first.toPath(), second.toPath());
        } catch (Exception e) {
            return first.getAbsolutePath().equals(second.getAbsolutePath());
        }
    }

    private Optional<String> holderAndSubjectProblem() {
        boolean conflict = (subjectKey != null || subjectDn != null) && holderCert != null;
        return conflict
                ? Optional.of(CliOptionNames.SUBJECT_KEY_LONG + "/" + CliOptionNames.SUBJECT_DN_LONG + " and " + CliOptionNames.HOLDER_CERT_LONG
                        + " are mutually exclusive; use " + CliOptionNames.SUBJECT_KEY_LONG + " with " + CliOptionNames.SUBJECT_DN_LONG
                        + " or " + CliOptionNames.IN_PLATFORM_MODEL_LONG + ".")
                : Optional.empty();
    }

    /**
     * --prev-pcert must name exactly one readable platform certificate. Its signature is not checked here;
     * run validate on it first when that matters.
     */
    private Optional<String> previousPlatformCertProblem() {
        boolean requested = previousPlatformCert != null && !previousPlatformCert.isBlank();
        boolean readable = previousPlatformCertFile()
                .map(PlatformCertificate::loadSafe)
                .map(PlatformCertificate::getCertificateIdentifier)
                .isPresent();
        return requested && !readable
                ? Optional.of("certgen ... " + CliOptionNames.PREV_PCERT_LONG + " must name exactly one readable platform certificate: " + previousPlatformCert
                        + ". Use previousPlatformCertificates JSON for additional history.")
                : Optional.empty();
    }

    private Optional<String> issuerProblem(X509CertificateHolder issuer) {
        return issuerCert != null && issuer == null
                ? Optional.of("Could not read the issuer certificate: " + issuerCert)
                : Optional.empty();
    }

    private Optional<File> previousPlatformCertFile() {
        return Optional.ofNullable(previousPlatformCert)
                .filter(value -> !value.isBlank())
                .map(value -> GlobFileResolver.resolve(List.of(value)))
                .filter(files -> files.size() == 1)
                .map(List::getFirst);
    }
}
