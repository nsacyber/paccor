package paccor.tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class PlatformFirmwareSignatureVerificationTraitTest {
    public static final PlatformFirmwareSignatureVerification.Enumerated PLATFORM_FW_SIG_VERIFICATION_1_ENUM = PlatformFirmwareSignatureVerification.Enumerated.hardwareSRTM;
    public static final String PLATFORM_FW_SIG_VERIFICATION_1_TRAIT_DESC = "PlatformFirmwareSignatureVerification Trait Test 1";
    public static final PlatformFirmwareSignatureVerification PLATFORM_FW_SIG_VERIFICATION_1 = new PlatformFirmwareSignatureVerification(PLATFORM_FW_SIG_VERIFICATION_1_ENUM.getValue());

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test PlatformFirmwareSignatureVerificationTrait
     */
    public static final PlatformFirmwareSignatureVerificationTrait samplePlatformFirmwareSignatureVerificationTrait1() {
        return PlatformFirmwareSignatureVerificationTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(PlatformFirmwareSignatureVerificationTraitTest.PLATFORM_FW_SIG_VERIFICATION_1_TRAIT_DESC))
                .traitValue(PlatformFirmwareSignatureVerificationTraitTest.PLATFORM_FW_SIG_VERIFICATION_1)
                .build();
    }
}
