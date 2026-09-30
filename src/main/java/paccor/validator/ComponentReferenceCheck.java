package paccor.validator;

import paccor.normalization.CanonicalComponent;

/**
 * Decides whether the certificate references of a paired component are acceptable. Will be expanded.
 */
@FunctionalInterface
public interface ComponentReferenceCheck {

    /**
     * References the platform reports must match the certificate's. References the platform does not
     * report are accepted, since collectors usually cannot see them.
     */
    ComponentReferenceCheck REPORTED_MUST_MATCH = (reported, certified) ->
            CanonicalComponent.includes(certified.references(), reported.references());

    /**
     * @param reported the component as the platform reports it
     * @param certified the certificate component it is paired with
     * @return true if the references are acceptable
     */
    boolean accepts(CanonicalComponent reported, CanonicalComponent certified);
}
