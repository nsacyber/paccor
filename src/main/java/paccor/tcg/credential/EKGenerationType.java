package paccor.tcg.credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <pre>{@code
 * EKGenerationType ::= ENUMERATED {
 *      internal (0),
 *      injected (1),
 *      internalRevocable(2),
 *      injectedRevocable(3) }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
public class EKGenerationType extends ASN1EnumeratedEnumBase<EKGenerationType.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, EKGenerationType> FACTORY = factory(Enumerated.class, EKGenerationType::new);

	/**
	 * EKGenerationType options
	 */
	@AllArgsConstructor
	@Getter
	public enum Enumerated implements EnumWithIntegerValue {
		/**
		 * <pre>{@code
		 * internal: internally generated within the TPM
		 * }</pre>
		 */
		internal(0),
		/**
		 * <pre>{@code
		 * injected: generated externally and then inserted under a controlled environment during manufacturing
		 * }</pre>
		 */
		injected(1),
		/**
		 * <pre>{@code
		 * internalRevocable: internally generated within the TPM and indicates the EK was created consistent with the TPM_CreateRevocableEK command
		 * }</pre>
		 */
		internalRevocable(2),
		/**
		 * <pre>{@code
		 * internalRevocable: generated externally and then inserted under a controlled environment during manufacturing and indicates the EK was created consistent with the TPM_CreateRevocableEK command
		 * }</pre>
		 */
		injectedRevocable(3);

		private final int value;
	}

	/**
	 * Convert data into an EKGenerationType object.
	 * @param obj Could be any type that can be transformed into ASN1Enumerated
	 * @return {@link EKGenerationType}
	 */
	public static EKGenerationType getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
	}

	/**
	 * New EKGenerationType, looking up the given value in the enum class.
	 * @param value value to look up
	 */
	public EKGenerationType(int value) {
		super(value, Enumerated.class);
	}
}
