package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <pre>{@code
 * PlatformHardwareCapabilities ::= BIT STRING {
 *      iOMMUSupport (0),
 *      trustedExecutionEnvironment (1),
 *      physicalTamperProtection (2),
 *      physicalTamperDetection (3),
 *      firmwareFlashWP (4),
 *      externalDMASupport (5)
 * }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
public class PlatformHardwareCapabilities extends ASN1BitStringEnumBase<PlatformHardwareCapabilities.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, PlatformHardwareCapabilities> FACTORY = factory(Enumerated.class, PlatformHardwareCapabilities::new);
    /**
     * PlatformHardwareCapabilities options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * <pre>{@code
         * iOMMUSupport: The platform provides an IOMMU to protect the platform from DMA-based attacks.
         * }</pre>
         */
        iOMMUSupport(0),
        /**
         * <pre>{@code
         * trustedExecutionEnvironment: The platform contains a Trusted Execution Environment.
         * }</pre>
         */
        trustedExecutionEnvironment(1),
        /**
         * <pre>{@code
         * physicalTamperProtection: The platform supports a method of physical tamper protection, e.g., a chassis lock.
         * }</pre>
         */
        physicalTamperProtection(2),
        /**
         * <pre>{@code
         * physicalTamperDetection: The platform supports a method of physical tamper detection, e.g., a chassis intrusion switch.
         * }</pre>
         */
        physicalTamperDetection(3),
        /**
         * <pre>{@code
         * firmwareFlashWP: The platform supports firmware flash write protection, for example provided by the chipset or flash part.
         * }</pre>
         */
        firmwareFlashWP(4),
        /**
         * <pre>{@code
         * externalDMASupport: The platform includes external ports capable of DMA, e.g., USB-C or USB 3.0.
         * }</pre>
         */
        externalDMASupport(5);

        private final int value;
    }

    /**
     * Convert data into a PlatformHardwareCapabilities object.
     * @param obj Could be any type that can be transformed into ASN1BitString
     * @return PlatformHardwareCapabilities
     */
    public static PlatformHardwareCapabilities getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
    }

    /**
     * New PlatformHardwareCapabilities, looking up the given value in the enum class.
     * @param value value to look up
     */
    public PlatformHardwareCapabilities(int value) {
        super(value, Enumerated.class);
    }
}
