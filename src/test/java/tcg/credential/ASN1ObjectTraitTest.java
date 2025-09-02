package tcg.credential;

import java.sql.Date;
import java.time.LocalDate;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ASN1ObjectTraitTest {
    public static final String OBJ_TRAIT_DESC = "Date Trait Test 1";
    public static final LocalDate LOCAL_DATE = LocalDate.now();
    public static final ASN1GeneralizedTime ASN1_DATE = new ASN1GeneralizedTime(Date.valueOf(LOCAL_DATE));

    public static final ASN1ObjectTrait sampleASN1ObjectTrait1() {
        return ASN1ObjectTrait.builder()
                .traitId(TCGObjectIdentifier.tcgAlgorithmNull)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformSerial)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(OBJ_TRAIT_DESC))
                .traitValue(ASN1_DATE)
                .build();
    }

}
