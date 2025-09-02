package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * AttributeStatus ::= ENUMERATED {
 *      added (0),
 *      modified (1),
 *      removed (2) }
 * </pre>
 */
public class AttributeStatus extends ASN1EnumeratedEnumBase<AttributeStatus.Enumerated> {
    /**
     * Factory object used to provide conversion context.
     */
    public static final Factory<Enumerated, AttributeStatus> FACTORY = factory(Enumerated.class, AttributeStatus::new);

    /**
     * AttributeStatus options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * Information added
         */
        added(0),
        /**
         * Information modified
         */
        modified(1),
        /**
         * Information removed
         */
        removed(2);

        private final int value;
    }

    /**
     * Convert data into an AttributeStatus object.
     * @param obj Could be any type that can be transformed into ASN1Enumerated
     * @return AttributeStatus
     */
	public static AttributeStatus getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
	}

    /**
     * New AttributeStatus, looking up the given value in the enum class.
     * @param value value to look up
     */
	public AttributeStatus(int value) {
		super(value, Enumerated.class);
	}
}
