package paccor.validator;

import lombok.Builder;

/** Aggregates the independent validation results produced by the validate command. */
@Builder
public record ValidationReport(
        boolean signatureOk,
        boolean trustOk,
        boolean crlOk,
        boolean specificationOk,
        boolean componentsOk,
        boolean validityOk) {
    /**
     * Retains the default, non-enforcing policy for existing library callers.
     */
    public ValidationReport(boolean signatureOk, boolean trustOk, boolean crlOk,
                            boolean specificationOk, boolean componentsOk) {
        this(signatureOk, trustOk, crlOk, specificationOk, componentsOk, true);
    }

    public boolean ok() {
        return signatureOk && trustOk && crlOk && specificationOk && componentsOk && validityOk;
    }
}
