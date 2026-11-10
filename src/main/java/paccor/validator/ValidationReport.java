package paccor.validator;

import lombok.Builder;

/**
 * Aggregates the independent results produced by validation.
 *
 * @param signatureOk current certificate's signature verified with the supplied signer
 * @param trustOk issuer path accepted, or trust checking was not requested
 * @param crlOk current certificate passed CRL checking, or CRL checking was not requested
 * @param specificationOk certificate specification checks passed
 * @param componentsOk component matching and previous-certificate materialization passed, or were skipped
 * @param validityOk current certificate and signer windows accepted, or was skipped
 */
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

    /** Keeps omitted validity policy compatible with the five-check report. */
    public static class ValidationReportBuilder {
        private boolean validityOk = true;
    }

    public boolean ok() {
        return signatureOk && trustOk && crlOk && specificationOk && componentsOk && validityOk;
    }
}
