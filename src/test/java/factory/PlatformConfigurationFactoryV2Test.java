package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Test;
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
        ComponentIdentifierV2[] array = pc.getComponentIdentifier();
        Assert.assertEquals(1, array.length);
        ComponentIdentifierV2 component = array[0];
        Assert.assertNotNull(component.getComponentClass());
        Assert.assertEquals(componentClassRegistry,component.getComponentClass().getComponentClassRegistry().getId());
        Assert.assertEquals(new BigInteger(componentClassValue, 16), new BigInteger(1, component.getComponentClass().getComponentClassValue().getOctets()));
        Assert.assertEquals(manufacturer,component.getComponentManufacturer().getString());
        Assert.assertEquals(model, component.getComponentModel().getString());
        Assert.assertEquals(serial, component.getComponentSerial().getString());
        Assert.assertEquals(revision, component.getComponentRevision().getString());
        Assert.assertEquals(manufacturerId, component.getComponentManufacturerId().getId());
        ComponentAddress[] addressArray = component.getComponentAddress();
        Assert.assertEquals(4, addressArray.length);
        Assert.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.ETHERNETMAC.getOid(), addressArray[0].getAddressType());
        Assert.assertEquals(ethernetMacNormalized, addressArray[0].getAddressValue().getString());
        Assert.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.WLANMAC.getOid(), addressArray[1].getAddressType());
        Assert.assertEquals(wlanMacNormalized, addressArray[1].getAddressValue().getString());
        Assert.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.BLUETOOTHMAC.getOid(), addressArray[2].getAddressType());
        Assert.assertEquals(bluetoothMacNormalized, addressArray[2].getAddressValue().getString());
        Assert.assertEquals(ComponentIdentifierV2Factory.ComponentAddressType.ETHERNETMAC.getOid(), addressArray[3].getAddressType());
        Assert.assertEquals(bluetoothMacNormalized, addressArray[3].getAddressValue().getString());
        Assert.assertEquals(ASN1Boolean.FALSE, component.getFieldReplaceable());
        Assert.assertNotNull(component.getComponentPlatformCert());
        Assert.assertNotNull(component.getComponentPlatformCert().getAttributeCertIdentifier());
        Assert.assertNotNull(component.getComponentPlatformCert().getGenericCertIdentifier());
        Assert.assertEquals(attributeCertHashAlg, component.getComponentPlatformCert().getAttributeCertIdentifier().getHashAlgorithm().getAlgorithm().getId());
        Assert.assertEquals(attributeCertHash, Base64.toBase64String(component.getComponentPlatformCert().getAttributeCertIdentifier().getHashOverSignatureValue().getOctets()));
        Assert.assertEquals(new GeneralNames(new GeneralName(new X500Name(genCertIssuer))), component.getComponentPlatformCert().getGenericCertIdentifier().getIssuer());
        Assert.assertEquals(new BigInteger(genCertSerial), component.getComponentPlatformCert().getGenericCertIdentifier().getSerial().getValue());
        Assert.assertNotNull(component.getComponentPlatformCertUri());
        Assert.assertEquals(certUri, component.getComponentPlatformCertUri().getUniformResourceIdentifier().getString());
        Assert.assertEquals(certUriHashAlg, component.getComponentPlatformCertUri().getHashAlgorithm().getAlgorithm().getId());
        Assert.assertArrayEquals(certUriHash.getBytes(), Base64.encode(component.getComponentPlatformCertUri().getHashValue().getBytes()));
        Assert.assertNotNull(component.getStatus());
        Assert.assertEquals(new AttributeStatus(status), component.getStatus());
    }
}
