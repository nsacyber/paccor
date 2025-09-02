package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * PlatformFirmwareSignatureVerification ::= BIT STRING {
 *      hardwareSRTM (0),
 *      secureBoot (1)
 * }
 * </pre>
 */
public class PlatformFirmwareSignatureVerification extends ASN1BitStringEnumBase<PlatformFirmwareSignatureVerification.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, PlatformFirmwareSignatureVerification> FACTORY = factory(Enumerated.class, PlatformFirmwareSignatureVerification::new);
    /**
     * PlatformFirmwareSignatureVerification options
     */
    @AllArgsConstructor
    @Getter
    public enum Enumerated implements EnumWithIntegerValue {
        /**
         * <pre>
         * hardwareSRTM: An H-CRTM is present and verifies the signature of the next stage of the initial boot block (IBB).
         * </pre>
         */
        hardwareSRTM(0),

        /**
         * <pre>
         * secureBoot: UEFI Secure Boot is present.
         * </pre>
         */
        secureBoot(1);

        private final int value;
    }

    /**
     * Convert data into a PlatformFirmwareSignatureVerification object.
     * @param obj Could be any type that can be transformed into ASN1BitString.
     * @return PlatformFirmwareSignatureVerification
     */
    public static PlatformFirmwareSignatureVerification getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
    }

    /**
     * New PlatformFirmwareSignatureVerification, looking up the given value in the enum class.
     * @param value value to look up
     */
    public PlatformFirmwareSignatureVerification(int value) {
        super(value, Enumerated.class);
    }
}
