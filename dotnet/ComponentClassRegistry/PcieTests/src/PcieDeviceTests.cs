using HardwareManifestProto;
using Pcie;
using PcieLib;

namespace PcieTests;
public class PcieTests {
    public static readonly string RegistryA2Sample1ConfigBase64 = "BAFnNAcEEAB4AggBAAAAAAAAAN4MAADAAAAAAAwAANAAAAAAAeAAAAAAAABvA6ckAAAA32AAAAAAAAAA/wEAAAMA4IB4eHh4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAwAE3ol94Et1kmQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    public static readonly string RegistryA2Sample1VpdBase64 = "ghQAU2FtcGxlIFBDSSBDb21wb25lbnSQjQBQThhTYW1wbGUgVlBEIFBhcnQgTnVtYmVyIDFNThtTYW1wbGUgVlBEIE1hbnVmYWN0dXJlIElEIDFTThpTYW1wbGUgVlBEIFNlcmlhbCBOdW1iZXIgMVYwKVNhbXBsZSBFeHRyYSBEYXRhIGluIFZQRCAgICAgICAgICAgICAgICAgVjIETi9BIFJWASqRRwBWMRhTYW1wbGUgUlcgRGF0YSBpbiBWUEQgICBZQQNOL0FZQhB4eHh4eHh4eHh4eHh4eHh4WUMQeHh4eHh4eHh4eHh4eHh4eHg=";

    // Registry Appendix A Sample 1 Components
    public static readonly string RegistryA1Sample1ClassCodeHex = "010802";
    public static readonly string RegistryA1Sample1VendorIdHex = "0104";
    public static readonly string RegistryA1Sample1SubsystemVendorIdHex = "036F";
    public static readonly string RegistryA1Sample1VpdMnHex = "Sample VPD Manufacture ID 1";
    public static readonly string RegistryA1Sample1DeviceIdHex = "3467";
    public static readonly string RegistryA1Sample1SubsystemIdHex = "24A7";
    public static readonly string RegistryA1Sample1VpdPnHex = "Sample VPD Part Number 1";
    public static readonly string RegistryA1Sample1DsnHex = "9964DD12785FA237";
    public static readonly string RegistryA1Sample1VpdSnHex = "Sample VPD Serial Number 1";
    public static readonly string RegistryA1Sample1RevisionIdHex = "78";

    public static readonly string RegistryA2Sample2ConfigBase64 = "eQrrPAcEEAAAAAQGAACBAAAAAAAAAAAAAAAAAAAAANAAAAAAAAAAAEAAAABAAAAAAAAAAEAAAAAAAAAA/wECABAAAgAAAAAAAAAAAAAAAAABAAAAAQAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAREAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAOAAAAAwAfAAAAAAAAAAAACQAUAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAABzFU//+mvS8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    // Registry Appendix A Sample 2 Components
    public static readonly string RegistryA1Sample2ClassCodeHex = "060400";
    public static readonly string RegistryA1Sample2VendorIdHex = "0A79";
    public static readonly string RegistryA1Sample2SubsystemVendorIdHex = "";
    public static readonly string RegistryA1Sample2VpdMnHex = "";
    public static readonly string RegistryA1Sample2DeviceIdHex = "3CEB";
    public static readonly string RegistryA1Sample2SubsystemIdHex = "";
    public static readonly string RegistryA1Sample2VpdPnHex = "";
    public static readonly string RegistryA1Sample2DsnHex = "2FBDA6FFFF543107";
    public static readonly string RegistryA1Sample2VpdSnHex = "";
    public static readonly string RegistryA1Sample2RevisionIdHex = "00";

    public static readonly string RegistryA2Sample3ConfigBase64 = "X21OKwcEEAAiAAACAAAAAAAAAN4MAADAAAAAAAwAANAAAAAAAeAAAAAAAACCQZeGAAAA32AAAAAAAAAA/wEAAAAAAAAAAAAAAAAAAAAAAAABAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAFeIEABCDg/gAAAAAnQAAAEAASAOGNLAEwKQAAAz1FAEAAAREAAAAAAAAAAAAAAAAAAAAAEwgEAAAEAAAOAAAAAwAfAAAAAAAAAAAACQAUAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAOCAeHh4eAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
    public static readonly string RegistryA2Sample3VpdBase64 = "ghQAU2FtcGxlIFBDSSBDb21wb25lbnSQbwBQThhTYW1wbGUgVlBEIFBhcnQgTnVtYmVyIDJTThpTYW1wbGUgVlBEIFNlcmlhbCBOdW1iZXIgMlYwKVNhbXBsZSBFeHRyYSBEYXRhIGluIFZQRCAgICAgICAgICAgICAgICAgVjIETi9BIFJWAQGRRwBWMRhTYW1wbGUgUlcgRGF0YSBpbiBWUEQgICBZQQNOL0FZQhB4eHh4eHh4eHh4eHh4eHh4WUMQeHh4eHh4eHh4eHh4eHh4eHg=";

    // Registry Appendix A Sample 3 Components
    public static readonly string RegistryA1Sample3ClassCodeHex = "020000";
    public static readonly string RegistryA1Sample3VendorIdHex = "6D5F";
    public static readonly string RegistryA1Sample3SubsystemVendorIdHex = "4182";
    public static readonly string RegistryA1Sample3VpdMnHex = "";
    public static readonly string RegistryA1Sample3DeviceIdHex = "2B4E";
    public static readonly string RegistryA1Sample3SubsystemIdHex = "8697";
    public static readonly string RegistryA1Sample3VpdPnHex = "Sample VPD Part Number 2";
    public static readonly string RegistryA1Sample3DsnHex = "";
    public static readonly string RegistryA1Sample3VpdSnHex = "Sample VPD Serial Number 2";
    public static readonly string RegistryA1Sample3RevisionIdHex = "22";
    public static readonly string RegistryA1Sample3AddressesOid = "2.23.133.17.1";
    public static readonly string RegistryA1Sample3AddressNetworkMac = "112233445566";

    public static readonly string RegistryA2Sample4ConfigBase64 = "KOUdxwcEEAAA/gMMAAAAAAAAAN4MAADAAAAAAAwAANAAAAAAAeAAAAAAAACaBr9DAAAAAAAAAAAAAAAA/wEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";

    // Registry Appendix A Sample 4 Components
    public static readonly string RegistryA1Sample4ClassCodeHex = "0C03FE";
    public static readonly string RegistryA1Sample4VendorIdHex = "E528";
    public static readonly string RegistryA1Sample4SubsystemVendorIdHex = "069A";
    public static readonly string RegistryA1Sample4VpdMnHex = "";
    public static readonly string RegistryA1Sample4DeviceIdHex = "C71D";
    public static readonly string RegistryA1Sample4SubsystemIdHex = "43BF";
    public static readonly string RegistryA1Sample4VpdPnHex = "";
    public static readonly string RegistryA1Sample4DsnHex = "";
    public static readonly string RegistryA1Sample4VpdSnHex = "";
    public static readonly string RegistryA1Sample4RevisionIdHex = "00";

    // Registry Appendix A.1 Sample readout
    public static readonly string[] ComponentIdentifiersInJson = new string[] {
        // Registry Appendix A.1 Sample Component 1
        "{" +
        " \"COMPONENTCLASS\": {" +
          " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.4\"," +
          " \"COMPONENTCLASSVALUE\": \"00" + RegistryA1Sample1ClassCodeHex +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA1Sample1VendorIdHex + ":" + RegistryA1Sample1SubsystemVendorIdHex + ":" + RegistryA1Sample1VpdMnHex + "\"," +
        " \"MODEL\": \"" + RegistryA1Sample1DeviceIdHex + ":" + RegistryA1Sample1SubsystemIdHex + ":" + RegistryA1Sample1VpdPnHex + "\"," +
        " \"SERIAL\": \"" + RegistryA1Sample1DsnHex + ":" + RegistryA1Sample1VpdSnHex + "\"," +
        " \"REVISION\": \"" + RegistryA1Sample1RevisionIdHex +
        "\" }",
        // Registry Appendix A.1 Sample Component 2
        "{" +
        " \"COMPONENTCLASS\": {" +
          " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.4\"," +
          " \"COMPONENTCLASSVALUE\": \"00" + RegistryA1Sample2ClassCodeHex +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA1Sample2VendorIdHex + ":" + RegistryA1Sample2SubsystemVendorIdHex + ":" + RegistryA1Sample2VpdMnHex + "\"," +
        " \"MODEL\": \"" + RegistryA1Sample2DeviceIdHex + ":" + RegistryA1Sample2SubsystemIdHex + ":" + RegistryA1Sample2VpdPnHex + "\"," +
        " \"SERIAL\": \"" + RegistryA1Sample2DsnHex + ":" + RegistryA1Sample2VpdSnHex + "\"," +
        " \"REVISION\": \"" + RegistryA1Sample2RevisionIdHex +
        "\" }",
        // Registry Appendix A.1 Sample Component 3
        "{" +
        " \"COMPONENTCLASS\": {" +
          " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.4\"," +
          " \"COMPONENTCLASSVALUE\": \"00" + RegistryA1Sample3ClassCodeHex +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA1Sample3VendorIdHex + ":" + RegistryA1Sample3SubsystemVendorIdHex + ":" + RegistryA1Sample3VpdMnHex + "\"," +
        " \"MODEL\": \"" + RegistryA1Sample3DeviceIdHex + ":" + RegistryA1Sample3SubsystemIdHex + ":" + RegistryA1Sample3VpdPnHex + "\"," +
        " \"SERIAL\": \"" + RegistryA1Sample3DsnHex + ":" + RegistryA1Sample3VpdSnHex + "\"," +
        " \"REVISION\": \"" + RegistryA1Sample3RevisionIdHex + "\"," +
        " \"ADDRESSES\": [ {" +
          " \"ETHERNETMAC\": \"" + RegistryA1Sample3AddressNetworkMac +
        "\" } ]" +
        " }",
        // Registry Appendix A.1 Sample Component 4
        "{" +
        " \"COMPONENTCLASS\": {" +
          " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.4\"," +
          " \"COMPONENTCLASSVALUE\": \"00" + RegistryA1Sample4ClassCodeHex +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA1Sample4VendorIdHex + ":" + RegistryA1Sample4SubsystemVendorIdHex + ":" + RegistryA1Sample4VpdMnHex + "\"," +
        " \"MODEL\": \"" + RegistryA1Sample4DeviceIdHex + ":" + RegistryA1Sample4SubsystemIdHex + ":" + RegistryA1Sample2VpdPnHex + "\"," +
        " \"REVISION\": \"" + RegistryA1Sample4RevisionIdHex +
        "\" }"
    };


    // Test PcieDevice
    [Test]
    public void TestRegistryASample1Components() {
        byte[] config = Convert.FromBase64String(RegistryA2Sample1ConfigBase64);
        byte[] vpd = Convert.FromBase64String(RegistryA2Sample1VpdBase64);
        PcieDevice device = new(config, vpd, (bool)true);
        Assert.Multiple(() => {
            Assert.That(device.ClassCode.Hex, Is.EqualTo(RegistryA1Sample1ClassCodeHex));
            Assert.That(device.VendorId.Hex, Is.EqualTo(RegistryA1Sample1VendorIdHex));
            Assert.That(device.SubsystemVendorId.Hex, Is.EqualTo(RegistryA1Sample1SubsystemVendorIdHex));
            Assert.That(device.VpdMn, Is.EqualTo(RegistryA1Sample1VpdMnHex));
            Assert.That(device.DeviceId.Hex, Is.EqualTo(RegistryA1Sample1DeviceIdHex));
            Assert.That(device.SubsystemId.Hex, Is.EqualTo(RegistryA1Sample1SubsystemIdHex));
            Assert.That(device.VpdPn, Is.EqualTo(RegistryA1Sample1VpdPnHex));
            Assert.That(device.VpdSn, Is.EqualTo(RegistryA1Sample1VpdSnHex));
            Assert.That(device.DeviceSerialNumber, Is.EqualTo(Convert.FromHexString(RegistryA1Sample1DsnHex)));
            Assert.That(device.RevisionId.ToString("X2"), Is.EqualTo(RegistryA1Sample1RevisionIdHex));
        });
    }

    [Test]
    public void TestRegistryASample2Components() {
        byte[] config = Convert.FromBase64String(RegistryA2Sample2ConfigBase64);
        byte[] vpd = [];
        PcieDevice device = new(config, vpd, true);
        Assert.Multiple(() => {
            Assert.That(device.ClassCode.Hex, Is.EqualTo(RegistryA1Sample2ClassCodeHex));
            Assert.That(device.VendorId.Hex, Is.EqualTo(RegistryA1Sample2VendorIdHex));
            Assert.That(device.SubsystemVendorId.Hex, Is.EqualTo(RegistryA1Sample2SubsystemVendorIdHex));
            Assert.That(device.VpdMn, Is.EqualTo(RegistryA1Sample2VpdMnHex));
            Assert.That(device.DeviceId.Hex, Is.EqualTo(RegistryA1Sample2DeviceIdHex));
            Assert.That(device.SubsystemId.Hex, Is.EqualTo(RegistryA1Sample2SubsystemIdHex));
            Assert.That(device.VpdPn, Is.EqualTo(RegistryA1Sample2VpdPnHex));
            Assert.That(device.VpdSn, Is.EqualTo(RegistryA1Sample2VpdSnHex));
            Assert.That(device.DeviceSerialNumber, Is.EqualTo(Convert.FromHexString(RegistryA1Sample2DsnHex)));
            Assert.That(device.RevisionId.ToString("X2"), Is.EqualTo(RegistryA1Sample2RevisionIdHex));
        });
    }
    [Test]
    public void TestRegistryASample3Components() {
        byte[] config = Convert.FromBase64String(RegistryA2Sample3ConfigBase64);
        byte[] vpd = Convert.FromBase64String(RegistryA2Sample3VpdBase64);
        PcieDevice device = new(config, vpd, (bool)true);
        Assert.Multiple(() => {
            Assert.That(device.ClassCode.Hex, Is.EqualTo(RegistryA1Sample3ClassCodeHex));
            Assert.That(device.VendorId.Hex, Is.EqualTo(RegistryA1Sample3VendorIdHex));
            Assert.That(device.SubsystemVendorId.Hex, Is.EqualTo(RegistryA1Sample3SubsystemVendorIdHex));
            Assert.That(device.VpdMn, Is.EqualTo(RegistryA1Sample3VpdMnHex));
            Assert.That(device.DeviceId.Hex, Is.EqualTo(RegistryA1Sample3DeviceIdHex));
            Assert.That(device.SubsystemId.Hex, Is.EqualTo(RegistryA1Sample3SubsystemIdHex));
            Assert.That(device.VpdPn, Is.EqualTo(RegistryA1Sample3VpdPnHex));
            Assert.That(device.VpdSn, Is.EqualTo(RegistryA1Sample3VpdSnHex));
            Assert.That(device.DeviceSerialNumber, Is.EqualTo(Convert.FromHexString(RegistryA1Sample3DsnHex)));
            Assert.That(device.RevisionId.ToString("X2"), Is.EqualTo(RegistryA1Sample3RevisionIdHex));
        });
    }
    [Test]
    public void TestRegistryASample4Components() {
        byte[] config = Convert.FromBase64String(RegistryA2Sample4ConfigBase64);
        byte[] vpd = Array.Empty<byte>();
        PcieDevice device = new(config, vpd, (bool)true);
        Assert.Multiple(() => {
            Assert.That(device.ClassCode.Hex, Is.EqualTo(RegistryA1Sample4ClassCodeHex));
            Assert.That(device.VendorId.Hex, Is.EqualTo(RegistryA1Sample4VendorIdHex));
            Assert.That(device.SubsystemVendorId.Hex, Is.EqualTo(RegistryA1Sample4SubsystemVendorIdHex));
            Assert.That(device.VpdMn, Is.EqualTo(RegistryA1Sample4VpdMnHex));
            Assert.That(device.DeviceId.Hex, Is.EqualTo(RegistryA1Sample4DeviceIdHex));
            Assert.That(device.SubsystemId.Hex, Is.EqualTo(RegistryA1Sample4SubsystemIdHex));
            Assert.That(device.VpdPn, Is.EqualTo(RegistryA1Sample4VpdPnHex));
            Assert.That(device.VpdSn, Is.EqualTo(RegistryA1Sample4VpdSnHex));
            Assert.That(device.DeviceSerialNumber, Is.EqualTo(Convert.FromHexString(RegistryA1Sample4DsnHex)));
            Assert.That(device.RevisionId.ToString("X2"), Is.EqualTo(RegistryA1Sample4RevisionIdHex));
        });
    }

    // Test PcieHardwareManifestPlugin
    [Test]
    public void TestRegistryAComponentList() {
        ManifestV2 manifestV2 = new();
        byte[] config1 = Convert.FromBase64String(RegistryA2Sample1ConfigBase64);
        byte[] vpd1 = Convert.FromBase64String(RegistryA2Sample1VpdBase64);
        byte[] config2 = Convert.FromBase64String(RegistryA2Sample2ConfigBase64);
        byte[] vpd2 = Array.Empty<byte>();
        byte[] config3 = Convert.FromBase64String(RegistryA2Sample3ConfigBase64);
        byte[] vpd3 = Convert.FromBase64String(RegistryA2Sample3VpdBase64);
        byte[] config4 = Convert.FromBase64String(RegistryA2Sample4ConfigBase64);
        byte[] vpd4 = Array.Empty<byte>();
        PcieDevice device1 = new(config1, vpd1, (bool)true);
        PcieDevice device2 = new(config2, vpd2, (bool)true);
        PcieDevice device3 = new(config3, vpd3, (bool)true);
        PcieDevice device4 = new(config4, vpd4, (bool)true);
        IList<PcieDevice> list = new List<PcieDevice> {
            device1,
            device2,
            device3,
            device4
        };

        device3.NetworkMac = Convert.FromHexString(RegistryA1Sample3AddressNetworkMac);

        IDictionary<int, IList<PcieDevice>> devices = CreateDictionary(list);

        PcieHardwareManifestPlugin.AddComponentsToManifestV2(devices, manifestV2);
        string jsonManifestV2 = manifestV2.ToString();

        Assert.That(ComponentIdentifiersInJson, Has.Length.GreaterThan(0));

        // Currently only the component with class code 0x02 will be collected
        Assert.That(jsonManifestV2, Contains.Substring(ComponentIdentifiersInJson[2]));
    }

    public static IDictionary<int, IList<PcieDevice>> CreateDictionary(IList<PcieDevice> list) {
        IDictionary<int, IList<PcieDevice>> devices = new Dictionary<int, IList<PcieDevice>>();
        foreach (PcieDevice device in list) {
            if (!devices.ContainsKey(device.ClassCode.Class)) {
                devices.Add(device.ClassCode.Class, new List<PcieDevice>());
            }
            devices[device.ClassCode.Class].Add(device);
        }

        return devices;
    }
}