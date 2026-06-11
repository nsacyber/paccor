package paccor.tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;

public class ComponentIdentifierV11TraitTest {
    public static final ComponentClass CI_1_COMPONENT_CLASS = ComponentClassTraitTest.COMPONENT_CLASS_2;
    public static final String CI_1_MANUFACTURER = "Sample Chassis Manufacturer";
    public static final String CI_1_MODEL = "Sample Chassis Model";
    public static final String CI_1_SERIAL = "Sample Chassis Serial Number";
    public static final String CI_1_REVISION = "Sample Chassis Revision";
    public static final String CI_1_MANUFACTURER_ID = "1.3.6.1.4.1.32473";
    public static final boolean CI_1_FIELD_REPLACEABLE = true;
    public static final CertificateIdentifier CI_2_CERTIFICATE_IDENTIFIER = CertificateIdentifierTraitTest.CI_2;
    public static final URIReference CI_2_PLATFORM_CERT_URI = URITraitTest.URI_2;
    public static final String CI_V11_1_TRAIT_DESC = "Component Identifier V11 Trait Test 1";
    public static final ComponentIdentifierV2 CI_1 = ComponentIdentifierV2.builder()
            .componentClass(CI_1_COMPONENT_CLASS)
            .componentManufacturer(new DERUTF8String(CI_1_MANUFACTURER))
            .componentModel(new DERUTF8String(CI_1_MODEL))
            .componentSerial(new DERUTF8String(CI_1_SERIAL))
            .componentRevision(new DERUTF8String(CI_1_REVISION))
            .componentManufacturerId(new ASN1ObjectIdentifier(CI_1_MANUFACTURER_ID))
            .fieldReplaceable(ASN1Boolean.getInstance(CI_1_FIELD_REPLACEABLE))
            .componentPlatformCert(CI_2_CERTIFICATE_IDENTIFIER)
            .componentPlatformCertUri(CI_2_PLATFORM_CERT_URI)
            .build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test ComponentIdentifierV11Trait
     */
    public static final ComponentIdentifierV11Trait sampleComponentIdentifierV11Trait1() {
        return ComponentIdentifierV11Trait.builder()
                .traitRegistry(ComponentIdentifierV11TraitTest.CI_1.getComponentClass().getComponentClassRegistry())
                .description(new DERUTF8String(ComponentIdentifierV11TraitTest.CI_V11_1_TRAIT_DESC))
                .traitValue(ComponentIdentifierV11TraitTest.CI_1)
                .build();
    }
}
