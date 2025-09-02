package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * -- V1.1 of this specification adds hybrid and physical.
 * -- Hybrid means the measurement root is capable of static AND dynamic
 * -- Physical means that the root is anchored by a physical TPM
 * -- Virtual means the TPM is virtualized (possibly running in a VMM).
 * -- TPMs or RTMs might leverage other lower layer RTMs to virtualize the
 * -- the capabilities of the platform.
 *
 * MeasurementRootType ::= ENUMERATED {
 *      static (0),
 *      dynamic (1),
 *      nonHost (2),
 *      hybrid (3),
 *      physical (4),
 *      virtual (5) }
 * </pre>
 */
public class MeasurementRootType extends ASN1EnumeratedEnumBase<MeasurementRootType.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, MeasurementRootType> FACTORY = factory(Enumerated.class, MeasurementRootType::new);

	/**
	 * MeasurementRootType options
	 */
	@AllArgsConstructor
	@Getter
	public enum Enumerated implements EnumWithIntegerValue {
		/**
		 * static: static RTM
		 */
		Static(0),
		/**
		 * dynamic: dynamic RTM
		 */
		dynamic(1),
		/**
		 * nonHost: nonHost RTM
		 */
		nonHost(2),
		/**
		 * hybrid: capable of static AND dynamic
		 */
		hybrid(3),
		/**
		 * physical: root is anchored by a physical TPM
		 */
		physical(4),
		/**
		 * virtual: TPM is virtualized (possibly running in a VMM)
		 */
		virtual(5);

		private final int value;
	}

	/**
	 * Convert data into a MeasurementRootType object.
	 * @param obj Could be any type that can be transformed into ASN1Enumerated
	 * @return MeasurementRootType
	 */
	public static MeasurementRootType getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
	}

	/**
	 * New MeasurementRootType, looking up the given value in the enum class.
	 * @param value value to look up
	 */
	public MeasurementRootType(int value) {
		super(value, Enumerated.class);
	}
}
