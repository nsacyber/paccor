package validator;

import cert.CertSpecVersion;
import java.util.List;
import java.util.Optional;
import tcg.credential.TCGSpecificationVersion;

public record SpecificationValidationReport(
        boolean ok,
        CertSpecVersion actualSpecVersion,
        CertSpecVersion expectedSpecVersion,
        List<String> issues) {
    public SpecificationValidationReport {
        issues = List.copyOf(Optional.ofNullable(issues).orElse(List.of()));
    }

    public String detail() {
        if (issues.isEmpty()) {
            return "";
        }
        return String.join(System.lineSeparator(), issues);
    }
}
