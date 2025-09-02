package tcg.credential;

import java.sql.Date;
import java.time.LocalDate;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;

public class CommonCriteriaTraitTest {
    public static final String CC_1_MEASURES_VERSION = "3.1";
    public static final EvaluationAssuranceLevel CC_1_MEASURES_EAL = EvaluationAssuranceLevel.getInstance("level7");
    public static final EvaluationStatus CC_1_MEASURES_ES = new EvaluationStatus(EvaluationStatus.Enumerated.designedToMeet.getValue());
    public static final StrengthOfFunction CC_1_MEASURES_SOF = StrengthOfFunction.getInstance(StrengthOfFunction.Enumerated.medium.name());
    public static final URIReference CC_1_MEASURE_TARGET_URI = URITraitTest.URI_1;
    public static final CommonCriteriaMeasures CC_1_MEASURES = CommonCriteriaMeasures.builder().version(new DERIA5String(CC_1_MEASURES_VERSION)).assuranceLevel(CC_1_MEASURES_EAL).evaluationStatus(CC_1_MEASURES_ES).strengthOfFunction(CC_1_MEASURES_SOF).targetUri(CC_1_MEASURE_TARGET_URI).build();
    public static final String CC_1_CERT_NUM = "8941546";
    public static final String CC_1_CERT_AUTHORITY = "ANY";
    public static final String CC_1_EVAL_SCHEME = "ALL";
    public static final LocalDate LOCAL_DATE = LocalDate.now();
    public static final ASN1GeneralizedTime CC_1_CERT_EXPIRY_DATE = new ASN1GeneralizedTime(Date.valueOf(LOCAL_DATE));
    public static final String CC_1_TRAIT_DESC = "Common Criteria Trait Test 1";
    public static final CommonCriteriaEvaluation CC_1 = CommonCriteriaEvaluation.builder().cCMeasures(CC_1_MEASURES).cCCertificateNumber(new DERUTF8String(CC_1_CERT_NUM)).cCCertificateAuthority(new DERUTF8String(CC_1_CERT_AUTHORITY)).evaluationScheme(new DERUTF8String(CC_1_EVAL_SCHEME)).cCCertificateExpiryDate(CC_1_CERT_EXPIRY_DATE).build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test CommonCriteriaTrait
     */
    public static final CommonCriteriaTrait sampleCommonCriteriaTrait1() {
        return CommonCriteriaTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(CommonCriteriaTraitTest.CC_1_TRAIT_DESC))
                .traitValue(CommonCriteriaTraitTest.CC_1)
                .build();
    }
}
