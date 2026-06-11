package paccor.tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERUTF8String;

public class ComponentClassTraitTest {
    public static final String COMPONENT_CLASS_1_TRAIT_DESC = "ComponentClass Trait Test 1";
    public static final ASN1ObjectIdentifier COMPONENT_CLASS_1_REGISTRY = TCGObjectIdentifier.tcgRegistryComponentClassDmtf;
    public static final ASN1OctetString COMPONENT_CLASS_1_VALUE = ASN1Utils.resizeOctets(4, "01020304");
    public static final ComponentClass COMPONENT_CLASS_1 = ComponentClass.builder().componentClassRegistry(COMPONENT_CLASS_1_REGISTRY).componentClassValue(COMPONENT_CLASS_1_VALUE).build();

    public static final String COMPONENT_CLASS_2_TRAIT_DESC = "ComponentClass Trait Test 2";
    public static final ASN1ObjectIdentifier COMPONENT_CLASS_2_REGISTRY = TCGObjectIdentifier.tcgRegistryComponentClassTcg;
    public static final ASN1OctetString COMPONENT_CLASS_2_VALUE = ASN1Utils.resizeOctets(4, "00020002");
    public static final ComponentClass COMPONENT_CLASS_2 = ComponentClass.builder().componentClassRegistry(COMPONENT_CLASS_2_REGISTRY).componentClassValue(COMPONENT_CLASS_2_VALUE).build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test ComponentClassTrait
     */
    public static final ComponentClassTrait sampleComponentClassTrait1() {
        return ComponentClassTrait.builder()
                .traitRegistry(ComponentClassTraitTest.COMPONENT_CLASS_1_REGISTRY)
                .description(new DERUTF8String(ComponentClassTraitTest.COMPONENT_CLASS_1_TRAIT_DESC))
                .traitValue(ComponentClassTraitTest.COMPONENT_CLASS_1.getComponentClassValue())
                .build();
    }

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test ComponentClassTrait
     */
    public static final ComponentClassTrait sampleComponentClassTrait2() {
        return ComponentClassTrait.builder()
                .traitRegistry(ComponentClassTraitTest.COMPONENT_CLASS_2_REGISTRY)
                .description(new DERUTF8String(ComponentClassTraitTest.COMPONENT_CLASS_2_TRAIT_DESC))
                .traitValue(ComponentClassTraitTest.COMPONENT_CLASS_2.getComponentClassValue())
                .build();
    }
}
