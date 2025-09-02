package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <pre>{@code
 * EvaluationStatus ::= ENUMERATED {
 *      designedToMeet (0),
 *      evaluationInProgress (1),
 *      evaluationCompleted (2) }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
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
		 * <pre>{@code
		 * designedToMeet: No Common Criteria Evaluation Status yet. Product is designed to meet Common Criteria.
		 * }</pre>
		 */
		designedToMeet(0),
		/**
		 * <pre>{@code
		 * evaluationInProgress: Common Criteria evaluation in progress.
		 * }</pre>
		 */
		evaluationInProgress(1),
		/**
		 * <pre>{@code
		 * evaluationCompleted: Common Criteria evaluation successful.
		 * }</pre>
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
