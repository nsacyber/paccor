package paccor.validator;

import lombok.Builder;

/** Aggregates the independent validation results produced by the validate command. */
@Builder
public record ValidationReport(
        boolean signatureOk,
        boolean trustOk,
        boolean crlOk,
        boolean specificationOk,
        boolean componentsOk) {
    public boolean ok() {
        return signatureOk && trustOk && crlOk && specificationOk && componentsOk;
    }
}
