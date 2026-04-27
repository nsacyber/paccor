package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <pre>{@code
 * EvaluationAssuranceLevel ::= ENUMERATED {
 *      level1 (1),
 *      level2 (2),
 *      level3 (3),
 *      level4 (4),
 *      level5 (5),
 *      level6 (6),
 *      level7 (7) }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
public class EvaluationAssuranceLevel extends ASN1EnumeratedEnumBase<EvaluationAssuranceLevel.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, EvaluationAssuranceLevel> FACTORY = factory(Enumerated.class, EvaluationAssuranceLevel::new);

	/**
	 * EvaluationAssuranceLevel options
	 */
	@AllArgsConstructor
	@Getter
	public enum Enumerated implements EnumWithIntegerValue {
		/**
		 * <pre>{@code
		 * level1: Common Criteria Evaluation Assurance Level 1 (EAL1)
		 * }</pre>
		 */
		level1(1),
		/**
		 * <pre>{@code
		 * level2: Common Criteria Evaluation Assurance Level 2 (EAL2)
		 * }</pre>
		 */
		level2(2),
		/**
		 * <pre>{@code
		 * level3: Common Criteria Evaluation Assurance Level 3 (EAL3)
		 * }</pre>
		 */
		level3(3),
		/**
		 * <pre>{@code
		 * level4: Common Criteria Evaluation Assurance Level 4 (EAL4)
		 * }</pre>
		 */
		level4(4),
		/**
		 * <pre>{@code
		 * level5: Common Criteria Evaluation Assurance Level 5 (EAL5)
		 * }</pre>
		 */
		level5(5),
		/**
		 * <pre>{@code
		 * level6: Common Criteria Evaluation Assurance Level 6 (EAL6)
		 * }</pre>
		 */
		level6(6),
		/**
		 * <pre>{@code
		 * level7: Common Criteria Evaluation Assurance Level 7 (EAL7)
		 * }</pre>
		 */
		level7(7);

		private final int value;
	}

	/**
	 * Convert data into a EvaluationAssuranceLevel object.
	 * @param obj Could be any type that can be transformed into ASN1Enumerated
	 * @return EvaluationAssuranceLevel
	 */
	public static EvaluationAssuranceLevel getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
	}

	/**
	 * New EvaluationAssuranceLevel, looking up the given value in the enum class.
	 * @param value value to look up
	 */
	public EvaluationAssuranceLevel(int value) {
		super(value, Enumerated.class);
	}
}
