package paccor.validator;

import java.util.List;
import java.util.Optional;

public record ComponentValidationReport(boolean ok, List<String> issues) {
    public ComponentValidationReport {
        issues = List.copyOf(Optional.ofNullable(issues).orElse(List.of()));
    }

    public String detail() {
        if (issues.isEmpty()) {
            return "";
        }
        return String.join(System.lineSeparator(), issues);
    }
}
