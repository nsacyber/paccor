package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * SecurityLevel ::= ENUMERATED {
 *      level1 (1),
 *      level2 (2),
 *      level3 (3),
 *      level4 (4) }
 * </pre>
 */
public class SecurityLevel extends ASN1EnumeratedEnumBase<SecurityLevel.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, SecurityLevel> FACTORY = factory(Enumerated.class, SecurityLevel::new);

    /**
     * SecurityLevel options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * <pre>
         * level1: FIPS security level 1
         * </pre>
         */
        level1(1),
        /**
         * <pre>
         * level2: FIPS security level 2
         * </pre>
         */
        level2(2),
        /**
         * <pre>
         * level3: FIPS security level 3
         * </pre>
         */
        level3(3),
        /**
         * <pre>
         * level4: FIPS security level 4
         * </pre>
         */
        level4(4);

        private final int value;
    }

    /**
     * Convert data into a SecurityLevel object.
     * @param obj Could be any type that can be transformed into ASN1Enumerated
     * @return SecurityLevel
     */
    public static SecurityLevel getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
    }

    /**
     * New SecurityLevel, looking up the given value in the enum class.
     * @param value value to look up
     */
    public SecurityLevel(int value) {
        super(value, Enumerated.class);
    }
}
