package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * PlatformFirmwareCapabilities ::= BIT STRING {
 *      fwSetupAuthLocal (0),
 *      fwSetupAuthRemote (1),
 *      sMMProtection (2),
 *      fwKernelDMAProtection (3)
 * }
 * </pre>
 */
public class PlatformFirmwareCapabilities extends ASN1BitStringEnumBase<PlatformFirmwareCapabilities.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, PlatformFirmwareCapabilities> FACTORY = factory(Enumerated.class, PlatformFirmwareCapabilities::new);
	
    /**
     * PlatformFirmwareCapabilities options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * <pre>
         * fwSetupAuthLocal: The platform supports authentication by a physically present user for platform firmware setup.
         * </pre>
         */
        fwSetupAuthLocal(0),
        /**
         * <pre>
         * fwSetupAuthRemote: The platform supports authentication by a remote entity for platform firmware setup.
         * </pre>
         */
        fwSetupAuthRemote(1),
        /**
         * <pre>
         * sMMProtection: The platform supports Management Mode memory protection, for example SMM protections, provided by the chipset and supported by the platform firmware.
         * </pre>
         */
        sMMProtection(2),
        /**
         * <pre>
         * fwKernelDMAProtection: The platform supports firmware-based protection against DMA attacks. Additional notes in spec.
         * </pre>
         */
        fwKernelDMAProtection(3);

        private final int value;
    }

    /**
     * Convert data into a PlatformFirmwareCapabilities object.
     * @param obj Could be any type that can be transformed into ASN1BitString
     * @return PlatformFirmwareCapabilities
     */
    public static PlatformFirmwareCapabilities getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
	}

    /**
     * New PlatformFirmwareCapabilities, looking up the given value in the enum class.
     * @param value value to look up
     */
    public PlatformFirmwareCapabilities(int value) {
		super(value, Enumerated.class);
	}
}
