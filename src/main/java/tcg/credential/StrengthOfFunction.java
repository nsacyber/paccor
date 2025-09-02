package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * StrengthOfFunction ::= ENUMERATED {
 *      basic (0),
 *      medium (1),
 *      high (2) }
 * </pre>
 */
public class StrengthOfFunction extends ASN1EnumeratedEnumBase<StrengthOfFunction.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, StrengthOfFunction> FACTORY = factory(Enumerated.class, StrengthOfFunction::new);

    /**
     * StrengthOfFunction options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * <pre>
         * basic: The function provides adequate protection against casual breach of security by attackers possessing a low attack potential.
         * </pre>
         */
        basic(0),
        /**
         * <pre>
         * medium: The function provides adequate protection against straightforward or intentional breach of security by attackers possessing a moderate attack potential.
         * </pre>
         */
        medium(1),
        /**
         * <pre>
         * high: The function provides adequate protection against a deliberately planned or organized breach of security by attackers possessing a high attack potential.
         * </pre>
         */
        high(2);

        private final int value;
    }

    /**
     * Convert data into a StrengthOfFunction object.
     * @param obj Could be any type that can be transformed into ASN1Enumerated
     * @return StrengthOfFunction
     */
    public static StrengthOfFunction getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
    }

    /**
     * New StrengthOfFunction, looking up the given value in the enum class.
     * @param value value to look up
     */
    public StrengthOfFunction(int value) {
        super(value, Enumerated.class);
    }
}
