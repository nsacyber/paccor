package paccor.validator;

import paccor.cert.PlatformCertificate;
import paccor.cert.CertSpecVersion;
import java.util.ArrayList;
import java.util.List;
import paccor.tcg.credential.TCGSpecificationVersion;

public final class SpecificationValidator {
    private SpecificationValidator() {}

    public static SpecificationValidationReport validate(PlatformCertificate certificate) {
        if (certificate == null) {
            return validate(null, null);
        }
        CertSpecVersion actual = certificate.actualSpecVersion();
        TCGSpecificationVersion declared = certificate.declaredSpecification();
        return validate(actual, declared);
    }

    private static SpecificationValidationReport validate(CertSpecVersion actual, TCGSpecificationVersion declared) {
        List<String> issues = new ArrayList<>();
        CertSpecVersion expected = CertSpecVersion.fromTcgSpecVersion(declared);

        if (declared == null) {
            issues.add("Certificate is missing required TCG credential specification attribute.");
        }
        if (declared != null && expected == null) {
            issues.add("Declared TCG credential specification " + declared.describe()
                    + " is not mapped to a supported certificate family.");
        }
        if (actual == null) {
            issues.add("Could not infer certificate specification version from platform configuration attributes.");
        }
        if (actual != null && expected != null && actual != expected) {
            issues.add("Certificate encodes " + actual + " semantics, but expected " + expected + ".");
        }

        return new SpecificationValidationReport(issues.isEmpty(), actual, expected, issues);
    }
}
