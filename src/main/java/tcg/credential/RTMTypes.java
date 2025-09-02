package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * RTMTypes ::= BIT STRING {
 *      static (0),
 *      dynamic (1),
 *      nonHost (2),
 *      virtual (3),
 *      hardwareStatic (4),
 *      bMC (5)
 * }
 * </pre>
 */
public class RTMTypes extends ASN1BitStringEnumBase<RTMTypes.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, RTMTypes> FACTORY = factory(Enumerated.class, RTMTypes::new);
    /**
     * RTMTypes options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * <pre>
         * static: the platform implements the RTM as part of the early platform firmware; also called a Static Root of Trust for Measurement (SRTM).
         * </pre>
         */
        Static(0),
        /**
         * <pre>
         * dynamic: the platform implements the RTM after the platform firmware executed; also called a Dynamic Root of Trust for Measurement (DRTM).
         * </pre>
         */
        dynamic(1),
        /**
         * <pre>
         * nonHost: the platform implements the RTM outside of the CPU or SoC, such as in an independent platform controller.
         * </pre>
         */
        nonHost(2),
        /**
         * <pre>
         * virtual: the platform implements a virtualized RTM, for example in a Virtual Machine.
         * </pre>
         */
        virtual(3),
        /**
         * <pre>
         * hardwareStatic: the platform implements a hardware-based RTM.
         * </pre>
         */
        hardwareStatic(4),
        /**
         * <pre>
         * bMC: the platform implements the RTM in the Baseboard Management Controller.
         * </pre>
         */
        bMC(5);

        private final int value;
    }

    /**
     * Convert data into a RTMTypes object.
     * @param obj Could be any type that can be transformed into ASN1BitString
     * @return RTMTypes
     */
    public static RTMTypes getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
    }

    /**
     * New RTMTypes, looking up the given value in the enum class.
     * @param value value to look up
     */
    public RTMTypes(int value) {
        super(value, Enumerated.class);
    }
}
