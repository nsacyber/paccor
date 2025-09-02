package factory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigInteger;

import tcg.credential.AttributeStatus;
import tcg.credential.ComponentAddress;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.TCGObjectIdentifier;

public class PlatformConfigurationFactoryV2Test {
    
    @Test
    public void testFull() throws IOException {
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
        final String json = "["
//        " {"
//        + "    \"" + PlatformConfigurationFactoryV2.Json.COMPONENTS.name() + "\": ["
        + "        {"
        + "            \"" + ComponentIdentifierV2Factory.Json.COMPONENTCLASS.name() + "\": {"
        + "                \"" + ComponentIdentifierV2Factory.Json.COMPONENTCLASSREGISTRY.name() + "\": \"" + componentClassRegistry + "\","
        + "                \"" + ComponentIdentifierV2Factory.Json.COMPONENTCLASSVALUE.name() + "\": \"" + componentClassValue + "\""
        + "            },"
        + "            \"" + ComponentIdentifierV2Factory.Json.MANUFACTURER.name() + "\": \"" + manufacturer + "\","
        + "            \"" + ComponentIdentifierV2Factory.Json.MODEL.name() + "\": \"" + model + "\","
        + "            \"" + ComponentIdentifierV2Factory.Json.SERIAL.name() + "\": \"" + serial + "\","
        + "            \"" + ComponentIdentifierV2Factory.Json.REVISION.name() + "\": \"" + revision + "\","
        + "            \"" + ComponentIdentifierV2Factory.Json.MANUFACTURERID.name() + "\": \"" + manufacturerId + "\","
        + "            \"" + ComponentIdentifierV2Factory.Json.FIELDREPLACEABLE.name() + "\": \"" + fieldReplaceable + "\","
        + "            \"" + ComponentIdentifierV2Factory.Json.ADDRESSES.name() + "\":"
        + "                ["
        + "                    {"
        + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.name() + "\":"
        +                        " \"" + ethernetMac + "\""
        + "                    },"
        + "                    {"
        + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.WLANMAC.name() + "\":"
        +                        " \"" + wlanMac + "\""
        + "                    },"
        + "                    {"
        + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.BLUETOOTHMAC.name() + "\":"
        +                        " \"" + bluetoothMac + "\""
        + "                    },"
        + "                    {"
        + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.name() + "\":"
        +                        " \"" + bluetoothMac + "\""
        + "                    }" // two ETHERNET MAC addresses on this component
        + "                ],"
        + "            \"" + ComponentIdentifierV2Factory.Json.PLATFORMCERT.name() + "\": {"
        + "                \"" + ComponentIdentifierV2Factory.Json.ATTRIBUTECERTIDENTIFIER.name() + "\": {"
        + "                    \"" + ComponentIdentifierV2Factory.Json.HASHALGORITHM.name() + "\": \"" + attributeCertHashAlg + "\","
        + "                    \"" + ComponentIdentifierV2Factory.Json.HASH.name() + "\": \"" + attributeCertHash + "\""
        + "                },"
        + "                \"" + ComponentIdentifierV2Factory.Json.GENERICCERTIDENTIFIER.name() + "\": {"
        + "                    \"" + ComponentIdentifierV2Factory.Json.ISSUER.name() + "\": \"" + genCertIssuer + "\","
        + "                    \"" + ComponentIdentifierV2Factory.Json.SERIAL.name() + "\": \"" + genCertSerial + "\""
        + "                }"
        + "            },"
        + "            \"" + ComponentIdentifierV2Factory.Json.PLATFORMCERTURI.name() + "\": {"
        + "                \"" + URIReferenceFactory.Json.UNIFORMRESOURCEIDENTIFIER.name() + "\": \"" + certUri + "\","
        + "                \"" + URIReferenceFactory.Json.HASHALGORITHM.name() + "\": \"" + certUriHashAlg + "\","
        + "                \"" + URIReferenceFactory.Json.HASHVALUE.name() + "\": \"" + certUriHash + "\""
        + "            },"
        + "            \"" + ComponentIdentifierV2Factory.Json.STATUS.name() + "\": \"" + status + "\""
        + "        }]";
//        + "    ]"
//        + "}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode components = mapper.readTree(json);
        
        PlatformConfigurationV2 pc = PlatformConfigurationV2Factory.create().addComponentsFromJsonNode(components).build();
        List<ComponentIdentifierV2> list = pc.getComponentIdentifiers();
        Assertions.assertEquals(1, list.size());
        ComponentIdentifierV2 component = list.get(0);
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
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(0).getAddressType());
        Assertions.assertEquals(ethernetMacNormalized, addressList.get(0).getAddressValue().getString());
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.WLANMAC.getOid(), addressList.get(1).getAddressType());
        Assertions.assertEquals(wlanMacNormalized, addressList.get(1).getAddressValue().getString());
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.BLUETOOTHMAC.getOid(), addressList.get(2).getAddressType());
        Assertions.assertEquals(bluetoothMacNormalized, addressList.get(2).getAddressValue().getString());
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(3).getAddressType());
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
    public void testJacksonized() throws IOException {
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
        final String json = ""
        + " {"
        + "    \"" + PlatformConfigurationV2Factory.Json.COMPONENTS.name() + "\": ["
                + "        {"
                + "            \"" + ComponentIdentifierV2Factory.Json.COMPONENTCLASS.name() + "\": {"
                + "                \"" + ComponentIdentifierV2Factory.Json.COMPONENTCLASSREGISTRY.name() + "\": \"" + componentClassRegistry + "\","
                + "                \"" + ComponentIdentifierV2Factory.Json.COMPONENTCLASSVALUE.name() + "\": \"" + componentClassValue + "\""
                + "            },"
                + "            \"" + ComponentIdentifierV2Factory.Json.MANUFACTURER.name() + "\": \"" + manufacturer + "\","
                + "            \"" + ComponentIdentifierV2Factory.Json.MODEL.name() + "\": \"" + model + "\","
                + "            \"" + ComponentIdentifierV2Factory.Json.SERIAL.name() + "\": \"" + serial + "\","
                + "            \"" + ComponentIdentifierV2Factory.Json.REVISION.name() + "\": \"" + revision + "\","
                + "            \"" + ComponentIdentifierV2Factory.Json.MANUFACTURERID.name() + "\": \"" + manufacturerId + "\","
                + "            \"" + ComponentIdentifierV2Factory.Json.FIELDREPLACEABLE.name() + "\": \"" + fieldReplaceable + "\","
                + "            \"" + ComponentIdentifierV2Factory.Json.ADDRESSES.name() + "\":"
                + "                ["
                + "                    {"
                + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.name() + "\":"
                +                        " \"" + ethernetMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.WLANMAC.name() + "\":"
                +                        " \"" + wlanMac + "\""
                + "                    },"
                + "                    {"
                + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.BLUETOOTHMAC.name() + "\":"
                +                        " \"" + bluetoothMac + "\""
                + "                    },"
                + "                    {"
//                + "                        \"" + ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.name() + "\":"
//                +                        " \"" + bluetoothMac + "\""
                + "                        \"ADDRESSTYPE\": \"" + ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.getOid() + "\","
                +                        " \"ADDRESSVALUE\": \"" + bluetoothMac + "\""
                + "                    }" // two ETHERNET MAC addresses on this component
                + "                ],"
                + "            \"" + ComponentIdentifierV2Factory.Json.PLATFORMCERT.name() + "\": {"
                + "                \"" + ComponentIdentifierV2Factory.Json.HASHEDCERTIDENTIFIER.name() + "\": {"
                + "                    \"" + ComponentIdentifierV2Factory.Json.HASHALGORITHM.name() + "\": \"" + attributeCertHashAlg + "\","
                + "                    \"" + ComponentIdentifierV2Factory.Json.HASH.name() + "\": \"" + attributeCertHash + "\""
                + "                },"
                + "                \"" + ComponentIdentifierV2Factory.Json.GENERICCERTIDENTIFIER.name() + "\": {"
                + "                    \"" + ComponentIdentifierV2Factory.Json.ISSUER.name() + "\": \"" + genCertIssuer + "\","
                + "                    \"" + ComponentIdentifierV2Factory.Json.SERIAL.name() + "\": \"" + genCertSerial + "\""
                + "                }"
                + "            },"
                + "            \"" + ComponentIdentifierV2Factory.Json.PLATFORMCERTURI.name() + "\": {"
                + "                \"" + URIReferenceFactory.Json.UNIFORMRESOURCEIDENTIFIER.name() + "\": \"" + certUri + "\","
                + "                \"" + URIReferenceFactory.Json.HASHALGORITHM.name() + "\": \"" + certUriHashAlg + "\","
                + "                \"" + URIReferenceFactory.Json.HASHVALUE.name() + "\": \"" + certUriHash + "\""
                + "            },"
                + "            \"" + ComponentIdentifierV2Factory.Json.STATUS.name() + "\": \"" + status + "\""
                + "        }"
        + "    ]"
        + "}";
        JsonMapper.Builder mapperBuilder = JsonMapper.builder();
        ObjectMapper mapper = mapperBuilder.build();

        //PlatformConfigurationV2.PlatformConfigurationV2Builder b2 = PlatformConfigurationV2.builder();
        PlatformConfigurationV2 pc = mapper.readValue(json, PlatformConfigurationV2.class);

        //PlatformConfigurationV2 pc = PlatformConfigurationV2Factory.create().addComponentsFromJsonNode(components).build();
        List<ComponentIdentifierV2> list = pc.getComponentIdentifiers();
        Assertions.assertEquals(1, list.size());
        ComponentIdentifierV2 component = list.get(0);
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
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(0).getAddressType());
        Assertions.assertEquals(ethernetMacNormalized, addressList.get(0).getAddressValue().getString());
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.WLANMAC.getOid(), addressList.get(1).getAddressType());
        Assertions.assertEquals(wlanMacNormalized, addressList.get(1).getAddressValue().getString());
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.BLUETOOTHMAC.getOid(), addressList.get(2).getAddressType());
        Assertions.assertEquals(bluetoothMacNormalized, addressList.get(2).getAddressValue().getString());
        Assertions.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.ETHERNETMAC.getOid(), addressList.get(3).getAddressType());
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
}
