package json;

import cert.CertSpecVersion;
import cert.SubjectAlternativeNameHelper;
import exception.JsonException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.List;
import json.schema.ComponentSchema;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.AttributeStatus;
import tcg.credential.ComponentAddress;
import tcg.credential.ComponentAddressType;
import tcg.credential.ComponentIdentifier;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.ManufacturerId;
import tcg.credential.PENTrait;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TraitCollection;
import tcg.credential.TraitMap;
import tcg.credential.UTF8StringTrait;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class HardwareManifestJsonHelperTest {
    private static final String COMP_JSON_1 = "src/test/resources/tutorials/v2/components.json";
    private static final String COMP_JSON_1_WITH_TRAITS = "src/test/resources/tutorials/v2/componentswithtraits.json";
    private static final String BARE_BONES_COMPONENTS_JSON =
            "src/test/resources/bare-bones-config/base-bare-bones-componentlist.json";
    private static final String ADVANCED_V3_COMPONENTS_JSON =
            "src/test/resources/sample_testgen1/localhost-componentlistv3adv.json";

    @Test
    public void testRead() throws JsonException {
        File comp1File = Paths.get(COMP_JSON_1).toFile();
        Assertions.assertTrue(comp1File.exists(), "Test file should exist");
        File comp1WithTraitsFile = Paths.get(COMP_JSON_1_WITH_TRAITS).toFile();
        Assertions.assertTrue(comp1WithTraitsFile.exists(), "Test file should exist");


        HardwareManifestJsonHelper comp1 = HardwareManifestJsonHelper.readComponents(comp1File);
        HardwareManifestJsonHelper comp1WithTraits = HardwareManifestJsonHelper.readComponents(comp1WithTraitsFile);

        Assertions.assertNotNull(comp1);
        Assertions.assertNotNull(comp1.pcV2());
        Assertions.assertNotNull(comp1WithTraits);
        Assertions.assertNotNull(comp1.pcV3());

        Assertions.assertEquals(comp1.pcV2().getComponentIdentifiers().size(), comp1WithTraits.pcV3().getPlatformComponents().size());
    }

    @Test
    public void testContainedComponentIdentifier1() {
        final String manufacturer = "Good Drive";
        final String model = "Event Horizon III";
        final String serial = "AEF85CC69";
        final String revision = "1.2.0.3";
        final String manufacturerId = "2.113.25.98";
        final String fieldReplaceable = "false";
        final String ethernetMac = "CD:85:EE:97:77:34";
        final String ethernetMacNormalized = "CD85EE977734";
        final String wlanMac = "97:77:34:CD:85:EE";
        final String wlanMacNormalized = "977734CD85EE";
        final String bluetoothMac = "55:78:3F:CB:A2:33";
        final String bluetoothMacNormalized = "55783FCBA233";
        final String json = " "
                + "        {"
                + "            \"" + ComponentSchema.MANUFACTURER + "\": \"" + manufacturer + "\","
                + "            \"" + ComponentSchema.MODEL + "\": \"" + model + "\","
                + "            \"" + ComponentSchema.SERIAL + "\": \"" + serial + "\","
                + "            \"" + ComponentSchema.REVISION + "\": \"" + revision + "\","
                + "            \"" + ComponentSchema.MANUFACTURER_ID + "\": \"" + manufacturerId + "\","
                + "            \"" + ComponentSchema.ADDRESSES + "\":"
                + "                ["
                + "                    {"
                + "                        \"" + ComponentAddressType.ETHERNETMAC.name() + "\":"
                +                        " \"" + ethernetMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentAddressType.WLANMAC.name() + "\":"
                +                        " \"" + wlanMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentAddressType.BLUETOOTHMAC.name() + "\":"
                +                        " \"" + bluetoothMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentAddressType.ETHERNETMAC.name() + "\":"
                +                        " \"" + bluetoothMac + "\""
                + "                    }" // two ETHERNET MAC addresses on this component
                + "                ]"
                + "        }";

        ComponentIdentifier component = ObjectMapperFactory.fromJsonSafe(json, ComponentIdentifier.class);
        Assertions.assertEquals(manufacturer, component.getComponentManufacturer().getString());
        Assertions.assertEquals(model, component.getComponentModel().getString());
        Assertions.assertEquals(serial, component.getComponentSerial().getString());
        Assertions.assertEquals(revision, component.getComponentRevision().getString());
        Assertions.assertEquals(manufacturerId, component.getComponentManufacturerId().getId());
        List<ComponentAddress> addressList = component.getComponentAddresses();
        Assertions.assertEquals(4, addressList.size());
        Assertions.assertEquals(ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(0).getAddressType());
        Assertions.assertEquals(ethernetMacNormalized, addressList.get(0).getAddressValue().getString());
        Assertions.assertEquals(ComponentAddressType.WLANMAC.getOid(), addressList.get(1).getAddressType());
        Assertions.assertEquals(wlanMacNormalized, addressList.get(1).getAddressValue().getString());
        Assertions.assertEquals(ComponentAddressType.BLUETOOTHMAC.getOid(), addressList.get(2).getAddressType());
        Assertions.assertEquals(bluetoothMacNormalized, addressList.get(2).getAddressValue().getString());
        Assertions.assertEquals(ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(3).getAddressType());
        Assertions.assertEquals(bluetoothMacNormalized, addressList.get(3).getAddressValue().getString());
    }

    @Test
    public void testContainedComponentIdentifierV2_1() throws IOException {
        final String componentClassRegistry = TCGObjectIdentifier.tcgRegistryComponentClassTcg.getId();
        final String componentClassValue = "10349617";
        final String manufacturer = "Good Drive";
        final String model = "Event Horizon III";
        final String serial = "AEF85CC69";
        final String revision = "1.2.0.3";
        final String manufacturerId = "2.113.25.98";
        final String fieldReplaceable = "false";
        final String ethernetMac = "CD:85:EE:97:77:34";
        final String ethernetMacNormalized = "CD85EE977734";
        final String wlanMac = "97:77:34:CD:85:EE";
        final String wlanMacNormalized = "977734CD85EE";
        final String bluetoothMac = "55:78:3F:CB:A2:33";
        final String bluetoothMacNormalized = "55783FCBA233";
        final String attributeCertHashAlg = "2.16.840.1.101.3.4.2.1";
        final String attributeCertHash = "FsMOcUPnfEjmF+vn+6sCr0UDqLmETPolZGx79QtH2CY=";
        final String genCertIssuer = "CN=MyApp ACES CA 2, OU=MyApp Public Sector, O=MyApp, C=US";
        final String genCertSerial = "41563248547896461532";
        final String certUri = "./enterprise-numbers";
        final String certUriHashAlg = "2.16.840.1.101.3.4.2.1";
        final String certUriHash = "ERuruGz0beU6AjqOaLKX3RFRNLp8s88htnelUexPHHY=";
        final String status = "added";
        final String json = " "
                + "        {"
                + "            \"" + ComponentSchema.COMPONENT_CLASS + "\": {"
                + "                \"" + ComponentSchema.COMPONENT_CLASS_REGISTRY + "\": \"" + componentClassRegistry + "\","
                + "                \"" + ComponentSchema.COMPONENT_CLASS_VALUE + "\": \"" + componentClassValue + "\""
                + "            },"
                + "            \"" + ComponentSchema.MANUFACTURER + "\": \"" + manufacturer + "\","
                + "            \"" + ComponentSchema.MODEL + "\": \"" + model + "\","
                + "            \"" + ComponentSchema.SERIAL + "\": \"" + serial + "\","
                + "            \"" + ComponentSchema.REVISION + "\": \"" + revision + "\","
                + "            \"" + ComponentSchema.MANUFACTURER_ID + "\": \"" + manufacturerId + "\","
                + "            \"" + ComponentSchema.FIELD_REPLACEABLE + "\": \"" + fieldReplaceable + "\","
                + "            \"" + ComponentSchema.ADDRESSES + "\":"
                + "                ["
                + "                    {"
                + "                        \"" + ComponentAddressType.ETHERNETMAC.name() + "\":"
                +                        " \"" + ethernetMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentAddressType.WLANMAC.name() + "\":"
                +                        " \"" + wlanMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentAddressType.BLUETOOTHMAC.name() + "\":"
                +                        " \"" + bluetoothMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentAddressType.ETHERNETMAC.name() + "\":"
                +                        " \"" + bluetoothMac + "\""
                + "                    }" // two ETHERNET MAC addresses on this component
                + "                ],"
                + "            \"" + ComponentSchema.PLATFORM_CERT + "\": {"
                + "                \"" + ComponentSchema.ATTRIBUTE_CERT_IDENTIFIER + "\": {"
                + "                    \"" + ComponentSchema.HASH_ALGORITHM + "\": \"" + attributeCertHashAlg + "\","
                + "                    \"" + ComponentSchema.HASH + "\": \"" + attributeCertHash + "\""
                + "                },"
                + "                \"" + ComponentSchema.GENERIC_CERT_IDENTIFIER + "\": {"
                + "                    \"" + ComponentSchema.ISSUER + "\": \"" + genCertIssuer + "\","
                + "                    \"" + ComponentSchema.SERIAL + "\": \"" + genCertSerial + "\""
                + "                }"
                + "            },"
                + "            \"" + ComponentSchema.PLATFORM_CERT_URI + "\": {"
                + "                \"" + ComponentSchema.UNIFORM_RESOURCE_IDENTIFIER + "\": \"" + certUri + "\","
                + "                \"" + ComponentSchema.HASH_ALGORITHM + "\": \"" + certUriHashAlg + "\","
                + "                \"" + ComponentSchema.HASH_VALUE + "\": \"" + certUriHash + "\""
                + "            },"
                + "            \"" + ComponentSchema.STATUS + "\": \"" + status + "\""
                + "        }";

        ComponentIdentifierV2 component = ObjectMapperFactory.fromJsonSafe(json, ComponentIdentifierV2.class);
        Assertions.assertNotNull(component.getComponentClass());
        Assertions.assertEquals(componentClassRegistry,component.getComponentClass().getComponentClassRegistry().getId());
        Assertions.assertEquals(new BigInteger(componentClassValue, 16), new BigInteger(1, component.getComponentClass().getComponentClassValue().getOctets()));
        Assertions.assertEquals(manufacturer,component.getComponentManufacturer().getString());
        Assertions.assertEquals(model, component.getComponentModel().getString());
        Assertions.assertEquals(serial, component.getComponentSerial().getString());
        Assertions.assertEquals(revision, component.getComponentRevision().getString());
        Assertions.assertEquals(manufacturerId, component.getComponentManufacturerId().getId());
        List<ComponentAddress> addressList = component.getComponentAddresses();
        Assertions.assertEquals(4, addressList.size());
        Assertions.assertEquals(ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(0).getAddressType());
        Assertions.assertEquals(ethernetMacNormalized, addressList.get(0).getAddressValue().getString());
        Assertions.assertEquals(ComponentAddressType.WLANMAC.getOid(), addressList.get(1).getAddressType());
        Assertions.assertEquals(wlanMacNormalized, addressList.get(1).getAddressValue().getString());
        Assertions.assertEquals(ComponentAddressType.BLUETOOTHMAC.getOid(), addressList.get(2).getAddressType());
        Assertions.assertEquals(bluetoothMacNormalized, addressList.get(2).getAddressValue().getString());
        Assertions.assertEquals(ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(3).getAddressType());
        Assertions.assertEquals(bluetoothMacNormalized, addressList.get(3).getAddressValue().getString());
        Assertions.assertEquals(ASN1Boolean.FALSE, component.getFieldReplaceable());
        Assertions.assertNotNull(component.getComponentPlatformCert());
        Assertions.assertNotNull(component.getComponentPlatformCert().getHashedCertIdentifier());
        Assertions.assertNotNull(component.getComponentPlatformCert().getGenericCertIdentifier());
        Assertions.assertEquals(attributeCertHashAlg, component.getComponentPlatformCert().getHashedCertIdentifier().getHashAlgorithm().getAlgorithm().getId());
        Assertions.assertEquals(attributeCertHash, Base64.toBase64String(component.getComponentPlatformCert().getHashedCertIdentifier().getHashOverSignatureValue().getOctets()));
        Assertions.assertEquals(new GeneralNames(new GeneralName(new X500Name(genCertIssuer))), component.getComponentPlatformCert().getGenericCertIdentifier().getIssuer());
        Assertions.assertEquals(new BigInteger(genCertSerial), component.getComponentPlatformCert().getGenericCertIdentifier().getSerial().getValue());
        Assertions.assertNotNull(component.getComponentPlatformCertUri());
        Assertions.assertEquals(certUri, component.getComponentPlatformCertUri().getUniformResourceIdentifier().getString());
        Assertions.assertEquals(certUriHashAlg, component.getComponentPlatformCertUri().getHashAlgorithm().getAlgorithm().getId());
        Assertions.assertArrayEquals(certUriHash.getBytes(), Base64.encode(component.getComponentPlatformCertUri().getHashValue().getBytes()));
        Assertions.assertNotNull(component.getStatus());
        Assertions.assertEquals(AttributeStatus.getInstance(status), component.getStatus());
    }

    @Test
    public void testAllAddRdnOverloads() {
        SubjectAlternativeNameHelper.Builder builder = SubjectAlternativeNameHelper.builder()
                .addRdn(new RDN(TCGObjectIdentifier.tcgAtPlatformModel, new DERUTF8String("Sample Model")))
                .addRdn(TCGObjectIdentifier.tcgAtPlatformManufacturerStr, new DERUTF8String("Sample Manufacturer"))
                .addRdn(TCGObjectIdentifier.tcgAtPlatformManufacturerId,
                        new ManufacturerId(new ASN1ObjectIdentifier("1.3.6.1.4.1.32473")));

        GeneralNames names = builder.buildLegacy();

        Assertions.assertNotNull(names);
        Assertions.assertEquals(1, names.getNames().length);
        Assertions.assertEquals(GeneralName.directoryName, names.getNames()[0].getTagNo());

        X500Name directoryName = X500Name.getInstance(names.getNames()[0].getName());
        Assertions.assertEquals("Sample Model",
                directoryName.getRDNs(TCGObjectIdentifier.tcgAtPlatformModel)[0].getFirst().getValue().toString());
        Assertions.assertEquals("Sample Manufacturer",
                directoryName.getRDNs(TCGObjectIdentifier.tcgAtPlatformManufacturerStr)[0].getFirst().getValue().toString());
        Assertions.assertEquals(1, directoryName.getRDNs(TCGObjectIdentifier.tcgAtPlatformManufacturerId).length);
    }

    @Test
    public void fromJsonNodeBuildsLegacySanAndExposesConvertedTraits() {
        JsonNode node = new ObjectMapper().readTree("""
                {
                  "PLATFORMMANUFACTURERSTR": "Dell Inc.",
                  "PLATFORMMODEL": "OptiPlex 7040",
                  "PLATFORMVERSION": "Not Specified",
                  "PLATFORMSERIAL": "306VRD2",
                  "PLATFORMMANUFACTURERID": "1.3.6.1.4.1.674"
                }
                """);

        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(node);
        Assertions.assertEquals(5, traits.flattenTraits().size());
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformManufacturer.equals(trait.getTraitCategory())
                        && "Dell Inc.".equals(((UTF8StringTrait) trait).getTraitValue().getString())));
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformModel.equals(trait.getTraitCategory())
                        && "OptiPlex 7040".equals(((UTF8StringTrait) trait).getTraitValue().getString())));
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformVersion.equals(trait.getTraitCategory())
                        && "Not Specified".equals(((UTF8StringTrait) trait).getTraitValue().getString())));
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformSerial.equals(trait.getTraitCategory())
                        && "306VRD2".equals(((UTF8StringTrait) trait).getTraitValue().getString())));
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof PENTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier.equals(trait.getTraitCategory())
                        && "1.3.6.1.4.1.674".equals(((PENTrait) trait).getTraitValue().getId())));

        GeneralNames names = SubjectAlternativeNameHelper.buildLegacy(traits);
        Assertions.assertNotNull(names);
        Assertions.assertEquals(GeneralName.directoryName, names.getNames()[0].getTagNo());

        X500Name directoryName = X500Name.getInstance(names.getNames()[0].getName());
        Assertions.assertEquals("OptiPlex 7040",
                directoryName.getRDNs(TCGObjectIdentifier.tcgAtPlatformModel)[0].getFirst().getValue().toString());
        Assertions.assertEquals("Dell Inc.",
                directoryName.getRDNs(TCGObjectIdentifier.tcgAtPlatformManufacturerStr)[0].getFirst().getValue().toString());
        Assertions.assertEquals("Not Specified",
                directoryName.getRDNs(TCGObjectIdentifier.tcgAtPlatformVersion)[0].getFirst().getValue().toString());
        Assertions.assertEquals("306VRD2",
                directoryName.getRDNs(TCGObjectIdentifier.tcgAtPlatformSerial)[0].getFirst().getValue().toString());
    }

    @Test
    public void fromJsonFileBuildsPlatformIdentifierSan() {
        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(new File(BARE_BONES_COMPONENTS_JSON));

        Assertions.assertNotNull(traits);
        Assertions.assertEquals(5, traits.flattenTraits().size());

        GeneralNames names = SubjectAlternativeNameHelper.buildPlatformIdentifier(traits);
        Assertions.assertNotNull(names);
        Assertions.assertEquals(1, names.getNames().length);
        Assertions.assertEquals(GeneralName.otherName, names.getNames()[0].getTagNo());

        ASN1Sequence otherName = ASN1Sequence.getInstance(names.getNames()[0].getName());
        Assertions.assertEquals(TCGObjectIdentifier.tcgAtPlatformIdentifier,
                ASN1ObjectIdentifier.getInstance(otherName.getObjectAt(0)));

        ASN1TaggedObject value = ASN1TaggedObject.getInstance(otherName.getObjectAt(1));
        TraitMap platformIdentifier = TraitMap.getInstance(ASN1Sequence.getInstance(value, true));
        Assertions.assertEquals(5, platformIdentifier.flattenTraits().size());
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformManufacturer.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformModel.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformVersion.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformSerial.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof PENTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier.equals(trait.getTraitCategory())));
    }

    @Test
    public void fromJsonFileBuildsPlatformIdentifierSanForTraitsManifest() {
        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(new File(ADVANCED_V3_COMPONENTS_JSON));

        Assertions.assertNotNull(traits);
        Assertions.assertEquals(4, traits.flattenTraits().size());
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformManufacturer.equals(trait.getTraitCategory())));
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformModel.equals(trait.getTraitCategory())));
        Assertions.assertTrue(traits.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformVersion.equals(trait.getTraitCategory())));

        GeneralNames names = SubjectAlternativeNameHelper.buildPlatformIdentifier(traits);
        Assertions.assertNotNull(names);
        Assertions.assertEquals(GeneralName.otherName, names.getNames()[0].getTagNo());
    }

    @Test
    public void extractPlatformTraitsRoundTripsLegacySanForV11() {
        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(new ObjectMapper().readTree("""
                        {
                          "PLATFORMMANUFACTURERSTR": "Dell Inc.",
                          "PLATFORMMODEL": "OptiPlex 7040",
                          "PLATFORMVERSION": "1.2.3",
                          "PLATFORMSERIAL": "ABC123",
                          "PLATFORMMANUFACTURERID": "1.3.6.1.4.1.674"
                        }
                        """));

        GeneralNames names = SubjectAlternativeNameHelper.buildLegacy(traits);
        TraitMap extracted = SubjectAlternativeNameHelper.extractPlatformTraits(names, CertSpecVersion.V1_1);
        TraitCollection list = TraitCollection.from(extracted);
        Assertions.assertNotNull(list);
        Assertions.assertEquals(5, list.size());
        Assertions.assertEquals("Dell Inc.",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("OptiPlex 7040",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformModel)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("1.2.3",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformVersion)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("ABC123",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformSerial)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("1.3.6.1.4.1.674",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier)
                        .orElseThrow()
                        .getTraitValue().toString());
    }

    @Test
    public void extractPlatformTraitsRoundTripsPlatformIdentifierSanForV20() {
        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(new ObjectMapper().readTree("""
                        {
                          "PLATFORMMANUFACTURERSTR": "Lenovo",
                          "PLATFORMMODEL": "ThinkSystem",
                          "PLATFORMVERSION": "2.0",
                          "PLATFORMSERIAL": "XYZ789",
                          "PLATFORMMANUFACTURERID": "1.3.6.1.4.1.19046"
                        }
                        """));

        GeneralNames names = SubjectAlternativeNameHelper.buildPlatformIdentifier(traits);
        TraitMap extracted = SubjectAlternativeNameHelper.extractPlatformTraits(names, CertSpecVersion.V2_0);

        Assertions.assertNotNull(extracted);
        TraitCollection list = TraitCollection.from(extracted);
        Assertions.assertNotNull(list);
        Assertions.assertEquals(5, list.size());
        Assertions.assertEquals("Lenovo",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("ThinkSystem",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformModel)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("2.0",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformVersion)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("XYZ789",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformSerial)
                        .orElseThrow()
                        .getTraitValue().toString());
        Assertions.assertEquals("1.3.6.1.4.1.19046",
                list.firstWithCategory(TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier)
                        .orElseThrow()
                        .getTraitValue().toString());
    }

    @Test
    public void extractPlatformTraitsFallsBackAcrossSanEncodings() {
        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(new ObjectMapper().readTree("""
                        {
                          "PLATFORMMANUFACTURERSTR": "HP",
                          "PLATFORMMODEL": "ProLiant",
                          "PLATFORMVERSION": "3.1",
                          "PLATFORMSERIAL": "SER555"
                        }
                        """));

        GeneralNames legacySan = SubjectAlternativeNameHelper.buildLegacy(traits);
        GeneralNames platformIdentifierSan = SubjectAlternativeNameHelper.buildPlatformIdentifier(traits);

        TraitMap fromLegacyWithV20 = SubjectAlternativeNameHelper.extractPlatformTraits(legacySan, CertSpecVersion.V2_0);
        TraitMap fromPlatformIdentifierWithV11 = SubjectAlternativeNameHelper.extractPlatformTraits(platformIdentifierSan, CertSpecVersion.V1_1);

        Assertions.assertNotNull(fromLegacyWithV20);
        Assertions.assertNotNull(fromPlatformIdentifierWithV11);
        Assertions.assertEquals(traits, fromLegacyWithV20);
        Assertions.assertEquals(traits, fromPlatformIdentifierWithV11);
    }
}
