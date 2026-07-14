package paccor.cli;

import paccor.cert.CertKind;
import paccor.cert.PlatformCertificate;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import paccor.cli.pv.ReadableFileConverter;
import paccor.model.PlatformCertificateInformationModel;
import paccor.normalization.PlatformConfigurationNormalizer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.TBBSecurityAssertions;
import paccor.tcg.credential.TCGPlatformSpecification;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.URIReference;

@Command(name = "view", mixinStandardHelpOptions = true, description = "Display a summary of a platform certificate")
public class ViewCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin private CommonOptions common;

    @Option(
            names = {"-X", "--x509v2AttrCert", "--pkcPlatformCert", "--certificate"},
            description = "Platform certificate file",
            required = true,
            converter = ReadableFileConverter.class)
    private File platformCertFile;

    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public Integer call() {
        PlatformCertificate certificate = PlatformCertificate.load(platformCertFile);
        if (certificate == null) {
            common.printError("Could not read platform certificate provided.");
            return ClientExitCodes.USAGE_ERROR.code();
        }

        PlatformCertificateInformationModel info = PlatformCertificateInformationModel.from(certificate);

        if (info == null) {
            println("Could not read platform certificate information.");
            return ClientExitCodes.USAGE_ERROR.code();
        }

        println("Certificate File: " + platformCertFile.getPath());
        println("Certificate Kind: " + certificate.certKind());
        println("Certificate Type: " + valueOrUnknown(certificate.getCertType()));
        println("Certificate Spec Version: " + valueOrUnknown(certificate.resolvedSpecVersion()));
        println("Declared TCG Credential Spec: " + info.describeCredSpec());
        println((certificate.certKind() == CertKind.AC ? "Holder: " : "Subject: ") + info.describeSubject());
        println("Issuer: " + info.describeIssuer());
        println("Serial: " + info.getCertSerialNumber());

        TCGPlatformSpecification platformSpec = info.getTcgPlatformSpecification();
        if (platformSpec != null) {
            println("TCG Platform Spec: " + platformSpec.getVersion().describe());
        }

        TBBSecurityAssertions assertions = info.getTbbSecurityAssertions();
        if (assertions != null) {
            println("TBB Security Assertions: present");
        }

        URIReference configUri = certificate.platformConfigUri();
        if (configUri != null) {
            println("Platform Config URI: " + configUri.getUniformResourceIdentifier().getString());
        } else if (info.getPlatformConfigUri() != null) {
            println("Platform Config URI: " + info.getPlatformConfigUri());
        }

        PlatformConfigurationV3 configuration = info.getPlatformConfiguration();
        List<TraitMap> components = PlatformConfigurationNormalizer.componentsForValidation(configuration);
        println("Platform Components: " + components.size());
        TraitMap platformFacts = info.getPlatformTraits();
        if (platformFacts != null && !platformFacts.isEmpty()) {
            println("Platform Facts: " + summarizePlatformTraits(platformFacts));
        } else if (!components.isEmpty()) {
            println("Platform Facts: unavailable");
        }

        TraitMap previous = info.getPreviousPlatformCertificates();
        println("Previous Platform Certificates: " + countTraits(previous));

        TraitMap anchors = info.getCryptographicAnchors();
        println("Cryptographic Anchors: " + countTraits(anchors));

        TraitMap ownership = info.getPlatformOwnership();
        println("Platform Ownership: " + countTraits(ownership));

        TraitMap manufacturingAssertions = info.getManufacturingAssertions();
        println("Manufacturing Assertions: " + countTraits(manufacturingAssertions));

        return ClientExitCodes.SUCCESS.code();
    }

    private void println(String text) {
        if (!common.quiet) {
            System.out.println(text);
        }
    }

    private static String valueOrUnknown(Object value) {
        return value != null ? value.toString() : "unknown";
    }

    private static int countTraits(TraitMap traits) {
        return traits == null ? 0 : traits.size();
    }

    private static String summarizePlatformTraits(TraitMap traits) {
        TraitCollection collection = TraitCollection.from(traits);
        return "manufacturer=" + collection.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer).orElse("?")
                + ", model=" + collection.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatPlatformModel).orElse("?")
                + ", version=" + collection.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatPlatformVersion).orElse("?")
                + ", serial=" + collection.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatPlatformSerial).orElse("?")
                + ", manufacturerId=" + collection.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier).orElse("?");
    }
}
