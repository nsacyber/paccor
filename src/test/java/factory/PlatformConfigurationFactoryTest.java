package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import tcg.credential.ComponentAddress;
import tcg.credential.ComponentIdentifier;
import tcg.credential.PlatformConfiguration;

public class PlatformConfigurationFactoryTest {
    
    @Test
    public void testFull() throws IOException {
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
        final String json = "["
//        " {"
//        + "    \"" + PlatformConfigurationFactory.Json.COMPONENTS.name() + "\": ["
        + "        {"
        + "            \"" + ComponentIdentifierFactory.Json.MANUFACTURER.name() + "\": \"" + manufacturer + "\","
        + "            \"" + ComponentIdentifierFactory.Json.MODEL.name() + "\": \"" + model + "\","
        + "            \"" + ComponentIdentifierFactory.Json.SERIAL.name() + "\": \"" + serial + "\","
        + "            \"" + ComponentIdentifierFactory.Json.REVISION.name() + "\": \"" + revision + "\","
        + "            \"" + ComponentIdentifierFactory.Json.MANUFACTURERID.name() + "\": \"" + manufacturerId + "\","
        + "            \"" + ComponentIdentifierFactory.Json.ADDRESSES.name() + "\":"
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
        + "                ]"
        + "        }]";
//        + "    ]"
//        + "}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode components = mapper.readTree(json);
        
        PlatformConfiguration pc = PlatformConfigurationFactory.create().addComponentsFromJsonNode(components).build();
        ComponentIdentifier[] array = pc.getComponentIdentifier();
        Assert.assertEquals(array.length, 1);
        ComponentIdentifier component = array[0];
        Assert.assertEquals(manufacturer, component.getComponentManufacturer().getString());
        Assert.assertEquals(model, component.getComponentModel().getString());
        Assert.assertEquals(serial, component.getComponentSerial().getString());
        Assert.assertEquals(revision, component.getComponentRevision().getString());
        Assert.assertEquals(manufacturerId, component.getComponentManufacturerId().getId());
        ComponentAddress[] addressArray = component.getComponentAddress();
        Assert.assertEquals(4, addressArray.length);
        Assert.assertEquals(ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.getOid(), addressArray[0].getAddressType());
        Assert.assertEquals(ethernetMacNormalized, addressArray[0].getAddressValue().getString());
        Assert.assertEquals(ComponentIdentifierFactory.ComponentAddressType.WLANMAC.getOid(), addressArray[1].getAddressType());
        Assert.assertEquals(wlanMacNormalized, addressArray[1].getAddressValue().getString());
        Assert.assertEquals(ComponentIdentifierFactory.ComponentAddressType.BLUETOOTHMAC.getOid(), addressArray[2].getAddressType());
        Assert.assertEquals(bluetoothMacNormalized, addressArray[2].getAddressValue().getString());
        Assert.assertEquals(ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.getOid(), addressArray[3].getAddressType());
        Assert.assertEquals(bluetoothMacNormalized, addressArray[3].getAddressValue().getString());
    }
}
