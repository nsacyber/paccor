package paccor.normalization;

import java.util.List;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.normalization.ComponentIdentifierV2Converter;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.tcg.credential.ComponentClassTrait;
import paccor.tcg.credential.ComponentIdentifierV11Trait;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.UTF8StringTrait;

class PlatformConfigurationNormalizerTest {

    @Test
    void normalizeForCertificateOutput_collapsesUniformRegistryComponents() {
        ASN1ObjectIdentifier registry = TCGObjectIdentifier.tcgRegistryComponentClassTcg;
        TraitMap component = TraitMap.builder()
                .trait(ComponentClassTrait.builder()
                        .traitRegistry(registry)
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentClass)
                        .traitValue(new DEROctetString(new byte[]{0, 0, 0, 1}))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(registry)
                        .traitValue(new DERUTF8String("Acme"))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                        .traitRegistry(registry)
                        .traitValue(new DERUTF8String("Model-A"))
                        .build())
                .build();
        PlatformConfigurationV3 configuration = PlatformConfigurationV3.builder()
                .platformComponent(component)
                .build();

        PlatformConfigurationV3 encoded = PlatformConfigurationNormalizer.normalizeForCertificateOutput(configuration);

        Assertions.assertNotNull(encoded);
        Assertions.assertEquals(1, encoded.getPlatformComponents().size());
        TraitMap encodedComponent = encoded.getPlatformComponents().get(0);
        List<?> v11Traits = encodedComponent.get(ComponentIdentifierV11Trait.class);
        Assertions.assertNotNull(v11Traits);
        Assertions.assertEquals(1, v11Traits.size());

        ComponentIdentifierV11Trait v11Trait = (ComponentIdentifierV11Trait) v11Traits.get(0);
        Assertions.assertEquals(registry, v11Trait.getTraitRegistry());
        Assertions.assertEquals(registry, v11Trait.getTraitValue().getComponentClass().getComponentClassRegistry());

        PlatformConfigurationV3 validation = PlatformConfigurationNormalizer.normalizeForValidation(encoded);
        TraitMap validationComponent = validation.getPlatformComponents().get(0);
        Assertions.assertFalse(validationComponent.flattenTraits().stream()
                .anyMatch(ComponentIdentifierV11Trait.class::isInstance));
        Assertions.assertEquals(registry,
                ComponentIdentifierV2Converter.fromTraitMap(validationComponent).getComponentClass().getComponentClassRegistry());
    }

    @Test
    void normalizeForCertificateOutput_preservesMixedRegistryComponents() {
        ASN1ObjectIdentifier tcgRegistry = TCGObjectIdentifier.tcgRegistryComponentClassTcg;
        ASN1ObjectIdentifier dmtfRegistry = TCGObjectIdentifier.tcgRegistryComponentClassDmtf;
        TraitMap component = TraitMap.builder()
                .trait(ComponentClassTrait.builder()
                        .traitRegistry(tcgRegistry)
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentClass)
                        .traitValue(new DEROctetString(new byte[]{0, 0, 0, 2}))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(dmtfRegistry)
                        .traitValue(new DERUTF8String("Acme"))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                        .traitRegistry(tcgRegistry)
                        .traitValue(new DERUTF8String("Model-B"))
                        .build())
                .build();
        PlatformConfigurationV3 configuration = PlatformConfigurationV3.builder()
                .platformComponent(component)
                .build();

        PlatformConfigurationV3 encoded = PlatformConfigurationNormalizer.normalizeForCertificateOutput(configuration);

        Assertions.assertNotNull(encoded);
        TraitMap encodedComponent = encoded.getPlatformComponents().get(0);
        Assertions.assertFalse(encodedComponent.containsKey(ComponentIdentifierV11Trait.class));
        Assertions.assertEquals(component.flattenTraits(), encodedComponent.flattenTraits());
    }
}
