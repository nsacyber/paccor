package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * PlatformFirmwareUpdateCompliance ::= BIT STRING {
 *      sp800-147 (0),
 *      sp800-147B (1),
 *      sp800-193 (2)
 * }
 * </pre>
 */
public class PlatformFirmwareUpdateCompliance extends ASN1BitStringEnumBase<PlatformFirmwareUpdateCompliance.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, PlatformFirmwareUpdateCompliance> FACTORY = factory(Enumerated.class, PlatformFirmwareUpdateCompliance::new);
    /**
     * PlatformFirmwareUpdateCompliance options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * <pre>
         * sp800-147: Platform firmware update complies with SP800-147.
         * </pre>
         */
        sp800_147(0),

        /**
         * <pre>
         * sp800-147B: Platform firmware update complies with SP800-147B; this option only applies to Server platforms.
         * </pre>
         */
        sp800_147B(1),

        /**
         * <pre>
         * sp800-193: Platform firmware update complies with SP800-193.
         * </pre>
         */
        sp800_193(2);

        private final int value;
    }

    /**
     * Convert data into a PlatformFirmwareUpdateCompliance object.
     * @param obj Could be any type that can be transformed into ASN1BitString.
     * @return PlatformFirmwareUpdateCompliance
     */
    public static PlatformFirmwareUpdateCompliance getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
    }

    /**
     * New PlatformFirmwareUpdateCompliance, looking up the given value in the enum class.
     * @param value value to look up
     */
    public PlatformFirmwareUpdateCompliance(int value) {
        super(value, Enumerated.class);
    }
}
