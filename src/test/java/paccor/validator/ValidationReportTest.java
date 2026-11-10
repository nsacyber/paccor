package paccor.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationReportTest {
    @Test
    void existingConstructorAndBuilderPreserveNonEnforcingDefault() {
        Assertions.assertTrue(new ValidationReport(true, true, true, true, true).ok());
        Assertions.assertTrue(ValidationReport.builder().signatureOk(true).trustOk(true).crlOk(true)
                .specificationOk(true).componentsOk(true).build().ok());
    }

    @Test
    void enforcedValidityFailureAffectsOverallResult() {
        Assertions.assertFalse(ValidationReport.builder().signatureOk(true).trustOk(true).crlOk(true)
                .specificationOk(true).componentsOk(true).validityOk(false).build().ok());
    }
}
