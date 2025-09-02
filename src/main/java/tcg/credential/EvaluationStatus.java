package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <pre>
 * EvaluationStatus ::= ENUMERATED {
 *      designedToMeet (0),
 *      evaluationInProgress (1),
 *      evaluationCompleted (2) }
 * </pre>
 */
public class EvaluationStatus extends ASN1EnumeratedEnumBase<EvaluationStatus.Enumerated> {
	/**
	 * Factory object used to provide conversion context.
	 */
	public static final Factory<Enumerated, EvaluationStatus> FACTORY = factory(Enumerated.class, EvaluationStatus::new);

	/**
	 * EvaluationStatus options
	 */
	@AllArgsConstructor
	@Getter
	public enum Enumerated implements EnumWithIntegerValue {
		/**
		 * <pre>
		 * designedToMeet: No Common Criteria Evaluation Status yet. Product is designed to meet Common Criteria.
		 * </pre>
		 */
		designedToMeet(0),
		/**
		 * <pre>
		 * evaluationInProgress: Common Criteria evaluation in progress.
		 * </pre>
		 */
		evaluationInProgress(1),
		/**
		 * <pre>
		 * evaluationCompleted: Common Criteria evaluation successful.
		 * </pre>
		 */
		evaluationCompleted(2);

		private final int value;
	}

	/**
	 * Convert data into a EvaluationStatus object.
	 * @param obj Could be any type that can be transformed into ASN1Enumerated
	 * @return EvaluationStatus
	 */
	public static EvaluationStatus getInstance(Object obj) {
		return ASN1EnumBase.getInstance(obj, FACTORY);
	}

	/**
	 * New EvaluationStatus, looking up the given value in the enum class.
	 * @param value value to look up
	 */
	public EvaluationStatus(int value) {
		super(value, Enumerated.class);
	}
}
