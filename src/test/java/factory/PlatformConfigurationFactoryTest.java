package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.IOException;
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
        final String wlanMac = "97:77:34:CD:85:EE";
        final String bluetoothMac = "55:78:3F:CB:A2:33";
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
        Assert.assertEquals(component.getComponentManufacturer().getString(), manufacturer);
        Assert.assertEquals(component.getComponentModel().getString(), model);
        Assert.assertEquals(component.getComponentSerial().getString(), serial);
        Assert.assertEquals(component.getComponentRevision().getString(), revision);
        Assert.assertEquals(component.getComponentManufacturerId().getId(), manufacturerId);
        ComponentAddress[] addressArray = component.getComponentAddress();
        Assert.assertEquals(addressArray.length, 4);
        Assert.assertEquals(addressArray[0].getAddressType(), ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.getOid());
        Assert.assertEquals(addressArray[0].getAddressValue().getString(), ethernetMac);
        Assert.assertEquals(addressArray[1].getAddressType(), ComponentIdentifierFactory.ComponentAddressType.WLANMAC.getOid());
        Assert.assertEquals(addressArray[1].getAddressValue().getString(), wlanMac);
        Assert.assertEquals(addressArray[2].getAddressType(), ComponentIdentifierFactory.ComponentAddressType.BLUETOOTHMAC.getOid());
        Assert.assertEquals(addressArray[2].getAddressValue().getString(), bluetoothMac);
        Assert.assertEquals(addressArray[3].getAddressType(), ComponentIdentifierFactory.ComponentAddressType.ETHERNETMAC.getOid());
        Assert.assertEquals(addressArray[3].getAddressValue().getString(), bluetoothMac);
    }
}
