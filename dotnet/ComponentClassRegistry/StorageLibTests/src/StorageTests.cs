using HardwareManifestProto;
using PcieLib;
using Storage;
using StorageAta;
using StorageLib;
using StorageLib.Linux;
using StorageNvme;
using StorageScsi;
using System.Text;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace StorageLibTests;
public class StorageTests {
    public static readonly string RegistryA21AtaComponentSample1Page3Base64 = "gAAAAAADAAGAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAACAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAC8WvHeIxJFNAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    public static readonly string RegistryA21AtaComponentSample1Page5Base64 = "gAAAAAAFAAFhU3BtZWxTIHJlYWkgbCAxICAgIAAAAABXRlYgcmUxIAAAAAAAAAAAYVNwbWVsTSBkb2xlTiBtdWViIHIgMSAgICAgICAgICAgICAgICAgIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    public static readonly string RegistryA31ScsiComponentSample1InquiryDataBase64 = "fwAHAAAAAABFWEFNUExFIFNDU0kgTW9kZWwgMSAgICBTViBBAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSgFiMXZwXAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    public static readonly string RegistryA31ScsiComponentSample1Page80Base64 = "f4AAG1NhbXBsZSBTQ1NJIFNlcmlhbCBOdW1iZXIgMQ==";
    public static readonly string RegistryA32ScsiComponentSample2InquiryDataBase64 = "fwAHAAAAAABFWEFNUExFIFNDU0kgTW9kZWwgMiAgICBTViBCAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSgFiMXZwXAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    public static readonly string RegistryA32ScsiComponentSample2Page80Base64 = "f4AAG1NhbXBsZSBTQ1NJIFNlcmlhbCBOdW1iZXIgMg==";
    public static readonly string RegistryA32ScsiComponentSample2Page83Base64 = "f4MALgIBAB5FWEFNUExFIFNhbXBsZSBTZXJpYWwgTnVtYmVyIDMBAwAIMSNFZ4mrze8=";
    public static readonly string RegistryA41NvmeComponentSample1IdentifyControllerBase64 = "zas0ElNOMSAgICAgICAgICAgICAgICAgTTIgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIEZXMyAgICAgAO/NqwAAAAAABAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABEiM0RVZneIq83vmaq7zN0AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAbnFuLjIwMTQtMDguY29tLmV4YW1wbGU6bnZtZTpudm0tc3Vic3lzdGVtLXNuLWQ3ODQzMgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
    
    /*
      SEQUENCE (1 elem)
       [0] (3 elem)
         SEQUENCE (5 elem)
           SEQUENCE (2 elem)
             OBJECT IDENTIFIER 2.23.133.18.3.5
             OCTET STRING (4 byte) 00000002
           UTF8String ABCDEF
           UTF8String Sample Model Number 1
           [0] (25 byte) Sample Serial 1:112233445
           [1] (8 byte) FW Ver 1
         SEQUENCE (5 elem)
           SEQUENCE (2 elem)
             OBJECT IDENTIFIER 2.23.133.18.3.5
             OCTET STRING (4 byte) 0100007F
           UTF8String EXAMPLE
           UTF8String SCSI Model 1
           [0] (28 byte) :Sample SCSI Serial Number 1
           [1] (4 byte) SV A
         SEQUENCE (5 elem)
           SEQUENCE (2 elem)
             OBJECT IDENTIFIER 2.23.133.18.3.5
             OCTET STRING (4 byte) 0100007F
           UTF8String EXAMPLE
           UTF8String SCSI Model 2
           [0] (43 byte) 123456789ABCDEF:Sample SCSI Serial Number 2
           [1] (4 byte) SV B
         SEQUENCE (5 elem)
           SEQUENCE (2 elem)
             OBJECT IDENTIFIER 2.23.133.18.3.5
             OCTET STRING (4 byte) 02010802
           UTF8String ABCD:1234:ABCDEF
           UTF8String M2
           [0] (89 byte) 1122334455667788ABCDEF99AABBCCDD:SN1:nqn.2014-08.com.example:nvme:nvm-subsystem-sn-d78432
           [1] (3 byte) FW3
     */

    public static readonly string ComponentClassValuePrefixAta = "00";
    public static readonly string ComponentClassValuePrefixScsi = "01";
    public static readonly string ComponentClassValuePrefixNvme = "02";


    // Registry Appendix A.2 ATA Sample 1
    public static readonly string RegistryA21AtaComponentSample1FormFactor = "02";
    public static readonly string RegistryA21AtaComponentSample1Aoi = "ABCDEF";
    public static readonly string RegistryA21AtaComponentSample1ModelNumber = "Sample Model Number 1";
    public static readonly string RegistryA21AtaComponentSample1SerialNumber = "Sample Serial 1";
    public static readonly string RegistryA21AtaComponentSample1UniqueId = "112233445";
    public static readonly string RegistryA21AtaComponentSample1FirmwareRevision = "FW Ver 1";
    // Registry Appendix A.2 ATA Sample 1 readout
    public static readonly string[] RegistryA21AtaComponentSample1IdentifiersInJson = new string[] {
        // Registry Appendix A.2 ATA Sample 1 Component 1
        "{" +
        " \"COMPONENTCLASS\": {" +
        " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.5\"," +
        " \"COMPONENTCLASSVALUE\": \"" + ComponentClassValuePrefixAta + "0000" + RegistryA21AtaComponentSample1FormFactor +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA21AtaComponentSample1Aoi + "\"," +
        " \"MODEL\": \"" + RegistryA21AtaComponentSample1ModelNumber + "\"," +
        " \"SERIAL\": \"" + RegistryA21AtaComponentSample1SerialNumber + ":" + RegistryA21AtaComponentSample1UniqueId + "\"," +
        " \"REVISION\": \"" + RegistryA21AtaComponentSample1FirmwareRevision +
        "\" }"
    };

    // Registry Appendix A.2 SCSI Sample 1
    public static readonly string RegistryA31ScsiComponentSample1InquiryClass = "7F";
    public static readonly string RegistryA31ScsiComponentSample1T10VendorIdentification = "EXAMPLE";
    public static readonly string RegistryA31ScsiComponentSample1ProductIdentification = "SCSI Model 1";
    public static readonly string RegistryA31ScsiComponentSample1VpdUniqueId = "";
    public static readonly string RegistryA31ScsiComponentSample1VpdSn = "Sample SCSI Serial Number 1";
    public static readonly string RegistryA31ScsiComponentSample1RevisionLevel = "SV A";
    // Registry Appendix A.2 SCSI Sample 1 readout
    public static readonly string[] RegistryA32ScsiComponentSample1IdentifiersInJson = new string[] {
        // Registry Appendix A.2 SCSI Sample 1 Component 1
        "{" +
        " \"COMPONENTCLASS\": {" +
        " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.5\"," +
        " \"COMPONENTCLASSVALUE\": \"" + ComponentClassValuePrefixScsi + "0000" + RegistryA31ScsiComponentSample1InquiryClass +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA31ScsiComponentSample1T10VendorIdentification + "\"," +
        " \"MODEL\": \"" + RegistryA31ScsiComponentSample1ProductIdentification + "\"," +
        " \"SERIAL\": \"" + RegistryA31ScsiComponentSample1VpdUniqueId + ":" + RegistryA31ScsiComponentSample1VpdSn + "\"," +
        " \"REVISION\": \"" + RegistryA31ScsiComponentSample1RevisionLevel +
        "\" }"
    };

    // Registry Appendix A.2 SCSI Sample 2
    public static readonly string RegistryA32ScsiComponentSample2InquiryClass = "7F";
    public static readonly string RegistryA32ScsiComponentSample2T10VendorIdentification = "EXAMPLE";
    public static readonly string RegistryA32ScsiComponentSample2ProductIdentification = "SCSI Model 2";
    public static readonly string RegistryA32ScsiComponentSample2VpdUniqueId = "123456789ABCDEF";
    public static readonly string RegistryA32ScsiComponentSample2VpdSn = "Sample SCSI Serial Number 2";
    public static readonly string RegistryA32ScsiComponentSample2RevisionLevel = "SV B";
    // Registry Appendix A.2 SCSI Sample 2 readout
    public static readonly string[] RegistryA32ScsiComponentSample2IdentifiersInJson = new string[] {
        // Registry Appendix A.2 SCSI Sample 2 Component 1
        "{" +
        " \"COMPONENTCLASS\": {" +
        " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.5\"," +
        " \"COMPONENTCLASSVALUE\": \"" + ComponentClassValuePrefixScsi + "0000" + RegistryA32ScsiComponentSample2InquiryClass +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA32ScsiComponentSample2T10VendorIdentification + "\"," +
        " \"MODEL\": \"" + RegistryA32ScsiComponentSample2ProductIdentification + "\"," +
        " \"SERIAL\": \"" + RegistryA32ScsiComponentSample2VpdUniqueId + ":" + RegistryA32ScsiComponentSample2VpdSn + "\"," +
        " \"REVISION\": \"" + RegistryA32ScsiComponentSample2RevisionLevel +
        "\" }"
    };

    // Registry Appendix A.2 NVMe Sample 1
    public static readonly string RegistryA41NvmeComponentSample1PciClassCode = "010802";
    public static readonly string RegistryA41NvmeComponentSample1Vid = "ABCD";
    public static readonly string RegistryA41NvmeComponentSample1Ssvid = "1234";
    public static readonly string RegistryA41NvmeComponentSample1Ieee = "ABCDEF";
    public static readonly string RegistryA41NvmeComponentSample1Mn = "M2";
    public static readonly string RegistryA41NvmeComponentSample1FGuid = "1122334455667788ABCDEF99AABBCCDD";
    public static readonly string RegistryA41NvmeComponentSample1Sn = "SN1";
    public static readonly string RegistryA41NvmeComponentSample1Subnqn = "nqn.2014-08.com.example:nvme:nvm-subsystem-sn-d78432";
    public static readonly string RegistryA41NvmeComponentSample1Ver = "00010400";
    public static readonly string RegistryA41NvmeComponentSample1Fr = "FW3";
    // Registry Appendix A.2 NVMe Sample 1 readout
    public static readonly string[] RegistryA41NvmeComponentSample1IdentifiersInJson = new string[] {
        // Registry Appendix A.2 NVMe Sample 1 Component 1
        "{" +
        " \"COMPONENTCLASS\": {" +
        " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.5\"," +
        " \"COMPONENTCLASSVALUE\": \"" + ComponentClassValuePrefixNvme + RegistryA41NvmeComponentSample1PciClassCode +
        "\" }," +
        " \"MANUFACTURER\": \"" + RegistryA41NvmeComponentSample1Vid + ":" + RegistryA41NvmeComponentSample1Ssvid + ":" + RegistryA41NvmeComponentSample1Ieee + "\"," +
        " \"MODEL\": \"" + RegistryA41NvmeComponentSample1Mn + "\"," +
        " \"SERIAL\": \"" + RegistryA41NvmeComponentSample1FGuid + ":" + RegistryA41NvmeComponentSample1Sn + ":" + RegistryA41NvmeComponentSample1Subnqn + "\"," +
        " \"REVISION\": \"" + RegistryA41NvmeComponentSample1Ver + ":" + RegistryA41NvmeComponentSample1Fr +
        "\" }"
    };


    [Test]
    public void TestRegistryA21Sample1() {
        byte[] page3 = Convert.FromBase64String(RegistryA21AtaComponentSample1Page3Base64);
        byte[] page5 = Convert.FromBase64String(RegistryA21AtaComponentSample1Page5Base64);

        bool build = StorageAtaData.Build(out StorageAtaData ataData, page3, page5);
        Assert.That(build, Is.True);

        List<StorageAtaData> ataDataList = [ataData];

        ManifestV2 manifestV2 = new();
        StorageHardwareManifestPlugin.AddComponentsToManifestV2([], ataDataList, [], manifestV2);

        string jsonManifestV2 = manifestV2.ToString();

        Assert.That(RegistryA21AtaComponentSample1IdentifiersInJson, Has.Length.GreaterThan(0));

        foreach (string componentJson in RegistryA21AtaComponentSample1IdentifiersInJson) {
            Assert.That(jsonManifestV2, Contains.Substring(componentJson));
        }
    }

    [Test]
    public void TestAtaFormFactor() {
        byte[] page3 = Convert.FromBase64String(RegistryA21AtaComponentSample1Page3Base64);

        StorageAtaStructs.AtaCapabilitiesData capsData = StorageCommonHelpers.CreateStruct<StorageAtaStructs.AtaCapabilitiesData>(page3);

        Assert.That(StorageHardwareManifestPlugin.ATA_FormFactor(capsData.FormFactor), Is.EqualTo(RegistryA21AtaComponentSample1FormFactor));
    }

    [Test]
    public void TestAtaUniqueId() {
        byte[] page3 = Convert.FromBase64String(RegistryA21AtaComponentSample1Page3Base64);

        StorageAtaStructs.AtaCapabilitiesData capsData = StorageCommonHelpers.CreateStruct<StorageAtaStructs.AtaCapabilitiesData>(page3);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.ATA_UNIQUEID(capsData.WWN), Is.EqualTo(RegistryA21AtaComponentSample1UniqueId));
            Assert.That(StorageHardwareManifestPlugin.ATA_UNIQUEID(capsData.WWN[0..2]), Is.Not.EqualTo(RegistryA21AtaComponentSample1UniqueId));
        });
    }

    [Test]
    public void TestAtaString() {
        byte[] page5 = Convert.FromBase64String(RegistryA21AtaComponentSample1Page5Base64);

        StorageAtaStructs.AtaStringsData stringsData = StorageCommonHelpers.CreateStruct<StorageAtaStructs.AtaStringsData>(page5);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.ATA_String([]), Is.Empty);
            Assert.That(StorageHardwareManifestPlugin.ATA_String(stringsData.SN), Is.EqualTo(RegistryA21AtaComponentSample1SerialNumber));
            Assert.That(StorageHardwareManifestPlugin.ATA_String(stringsData.FR), Is.EqualTo(RegistryA21AtaComponentSample1FirmwareRevision));
            Assert.That(StorageHardwareManifestPlugin.ATA_String(stringsData.MN), Is.EqualTo(RegistryA21AtaComponentSample1ModelNumber));
        });
    }

    [Test]
    public void TestAtaAoi() {
        byte[] page3 = Convert.FromBase64String(RegistryA21AtaComponentSample1Page3Base64);

        StorageAtaStructs.AtaCapabilitiesData capsData = StorageCommonHelpers.CreateStruct<StorageAtaStructs.AtaCapabilitiesData>(page3);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.ATA_AOI(capsData.WWN), Is.EqualTo(RegistryA21AtaComponentSample1Aoi));
            Assert.That(StorageHardwareManifestPlugin.ATA_AOI(capsData.WWN[0..2]), Is.Not.EqualTo(RegistryA21AtaComponentSample1Aoi));
        });
    }

    [Test]
    public void TestRegistryA31Sample1() {
        byte[] inquiryData = Convert.FromBase64String(RegistryA31ScsiComponentSample1InquiryDataBase64);
        byte[] page80 = Convert.FromBase64String(RegistryA31ScsiComponentSample1Page80Base64);

        bool build = StorageScsiData.Build(out StorageScsiData scsiData, inquiryData, page80, []);
        Assert.That(build, Is.True);

        List<StorageScsiData> scsiDataList = [scsiData];

        ManifestV2 manifestV2 = new();
        StorageHardwareManifestPlugin.AddComponentsToManifestV2([], [], scsiDataList, manifestV2);

        string jsonManifestV2 = manifestV2.ToString();

        Assert.That(RegistryA32ScsiComponentSample1IdentifiersInJson, Has.Length.GreaterThan(0));

        foreach (string componentJson in RegistryA32ScsiComponentSample1IdentifiersInJson) {
            Assert.That(jsonManifestV2, Contains.Substring(componentJson));
        }
    }

    [Test]
    public void TestRegistryA32Sample2() {
        byte[] inquiryData = Convert.FromBase64String(RegistryA32ScsiComponentSample2InquiryDataBase64);
        byte[] page80 = Convert.FromBase64String(RegistryA32ScsiComponentSample2Page80Base64);
        byte[] page83 = Convert.FromBase64String(RegistryA32ScsiComponentSample2Page83Base64);

        bool build = StorageScsiData.Build(out StorageScsiData scsiData, inquiryData, page80, page83);
        Assert.That(build, Is.True);

        List<StorageScsiData> scsiDataList = [scsiData];

        ManifestV2 manifestV2 = new();
        StorageHardwareManifestPlugin.AddComponentsToManifestV2([], [], scsiDataList, manifestV2);

        string jsonManifestV2 = manifestV2.ToString();

        Assert.That(RegistryA32ScsiComponentSample2IdentifiersInJson, Has.Length.GreaterThan(0));

        foreach (string componentJson in RegistryA32ScsiComponentSample2IdentifiersInJson) {
            Assert.That(jsonManifestV2, Contains.Substring(componentJson));
        }
    }
    
    [Test]
    public void TestScsiInquiryClass() {
        byte[] inquiry1 = Convert.FromBase64String(RegistryA31ScsiComponentSample1InquiryDataBase64);
        byte[] inquiry2 = Convert.FromBase64String(RegistryA32ScsiComponentSample2InquiryDataBase64);

        StorageScsiStructs.ScsiInquiryDataNoVendorSpecific inquiryData1 = StorageCommonHelpers.CreateStruct<StorageScsiStructs.ScsiInquiryDataNoVendorSpecific>(inquiry1);
        StorageScsiStructs.ScsiInquiryDataNoVendorSpecific inquiryData2 = StorageCommonHelpers.CreateStruct<StorageScsiStructs.ScsiInquiryDataNoVendorSpecific>(inquiry2);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_Class(inquiryData1.PeripheralQualifierAndDeviceType), Is.EqualTo(RegistryA31ScsiComponentSample1InquiryClass));
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_Class(inquiryData2.PeripheralQualifierAndDeviceType), Is.EqualTo(RegistryA32ScsiComponentSample2InquiryClass));
        });
    }
    
    [Test]
    public void TestScsiInquiryString() {
        byte[] inquiry1 = Convert.FromBase64String(RegistryA31ScsiComponentSample1InquiryDataBase64);
        byte[] inquiry2 = Convert.FromBase64String(RegistryA32ScsiComponentSample2InquiryDataBase64);

        StorageScsiStructs.ScsiInquiryDataNoVendorSpecific inquiryData1 = StorageCommonHelpers.CreateStruct<StorageScsiStructs.ScsiInquiryDataNoVendorSpecific>(inquiry1);
        StorageScsiStructs.ScsiInquiryDataNoVendorSpecific inquiryData2 = StorageCommonHelpers.CreateStruct<StorageScsiStructs.ScsiInquiryDataNoVendorSpecific>(inquiry2);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_String(inquiryData1.T10VendorIdentification), Is.EqualTo(RegistryA31ScsiComponentSample1T10VendorIdentification));
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_String(inquiryData1.ProductIdentification), Is.EqualTo(RegistryA31ScsiComponentSample1ProductIdentification));
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_String(inquiryData1.ProductRevisionLevel), Is.EqualTo(RegistryA31ScsiComponentSample1RevisionLevel));
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_String(inquiryData2.T10VendorIdentification), Is.EqualTo(RegistryA32ScsiComponentSample2T10VendorIdentification));
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_String(inquiryData2.ProductIdentification), Is.EqualTo(RegistryA32ScsiComponentSample2ProductIdentification));
            Assert.That(StorageHardwareManifestPlugin.SPC_INQUIRY_String(inquiryData2.ProductRevisionLevel), Is.EqualTo(RegistryA32ScsiComponentSample2RevisionLevel));
        });
    }
    
    [Test]
    public void TestScsiVpdSnString() {
        byte[] page801 = Convert.FromBase64String(RegistryA31ScsiComponentSample1Page80Base64);
        byte[] page802 = Convert.FromBase64String(RegistryA32ScsiComponentSample2Page80Base64);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.SPC_VPD_SN_String(page801), Is.EqualTo(RegistryA31ScsiComponentSample1VpdSn));
            Assert.That(StorageHardwareManifestPlugin.SPC_VPD_SN_String(page802), Is.EqualTo(RegistryA32ScsiComponentSample2VpdSn));
        });
    }
    
    
    
    [Test]
    public void TestScsiVpdDiUniqueIdString() {
        byte[] page83 = Convert.FromBase64String(RegistryA32ScsiComponentSample2Page83Base64);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.SPC_VPD_DI_UNIQUEID_String(page83), Is.EqualTo(RegistryA32ScsiComponentSample2VpdUniqueId));
        });
    }

    [Test]
    public void TestRegistryA41Sample2() {
        byte[] idCtrlData = Convert.FromBase64String(RegistryA41NvmeComponentSample1IdentifyControllerBase64);
        StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl = StorageCommonHelpers.CreateStruct<StorageNvmeStructs.NvmeIdentifyControllerData>(idCtrlData);
        byte[] classCodeData = Convert.FromHexString(RegistryA41NvmeComponentSample1PciClassCode);
        ClassCode classCode = new(classCodeData, false);
        List<StorageNvmeData> nvmeData = [new StorageNvmeData(nvmeCtrl, classCode)];

        ManifestV2 manifestV2 = new();
        StorageHardwareManifestPlugin.AddComponentsToManifestV2(nvmeData, [], [], manifestV2);

        string jsonManifestV2 = manifestV2.ToString();

        Assert.That(RegistryA41NvmeComponentSample1IdentifiersInJson, Has.Length.GreaterThan(0));

        foreach (string componentJson in RegistryA41NvmeComponentSample1IdentifiersInJson) {
            Assert.That(jsonManifestV2, Contains.Substring(componentJson));
        }
    }

    [Test]
    public void TestNvmeVal() {
        byte[] idCtrlData = Convert.FromBase64String(RegistryA41NvmeComponentSample1IdentifyControllerBase64);

        StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl = StorageCommonHelpers.CreateStruct<StorageNvmeStructs.NvmeIdentifyControllerData>(idCtrlData);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.NVMe_Val(nvmeCtrl.VID, true), Is.EqualTo(RegistryA41NvmeComponentSample1Vid));
            Assert.That(StorageHardwareManifestPlugin.NVMe_Val(nvmeCtrl.SSVID, true), Is.EqualTo(RegistryA41NvmeComponentSample1Ssvid));
            Assert.That(StorageHardwareManifestPlugin.NVMe_Val(nvmeCtrl.FGUID, false), Is.EqualTo(RegistryA41NvmeComponentSample1FGuid));
            Assert.That(StorageHardwareManifestPlugin.NVMe_Val(nvmeCtrl.VER, true), Is.EqualTo(RegistryA41NvmeComponentSample1Ver));
        });
    }

    [Test]
    public void TestNvmeOui() {
        byte[] idCtrlData = Convert.FromBase64String(RegistryA41NvmeComponentSample1IdentifyControllerBase64);

        StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl = StorageCommonHelpers.CreateStruct<StorageNvmeStructs.NvmeIdentifyControllerData>(idCtrlData);
        Assert.That(StorageHardwareManifestPlugin.NVMe_OUI(nvmeCtrl.IEEE), Is.EqualTo(RegistryA41NvmeComponentSample1Ieee));

    }

    [Test]
    public void TestNvmeString() {
        byte[] idCtrlData = Convert.FromBase64String(RegistryA41NvmeComponentSample1IdentifyControllerBase64);

        StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl = StorageCommonHelpers.CreateStruct<StorageNvmeStructs.NvmeIdentifyControllerData>(idCtrlData);

        Assert.Multiple(() => {
            Assert.That(StorageHardwareManifestPlugin.NVMe_String(nvmeCtrl.MN), Is.EqualTo(RegistryA41NvmeComponentSample1Mn));
            Assert.That(StorageHardwareManifestPlugin.NVMe_String(nvmeCtrl.SN), Is.EqualTo(RegistryA41NvmeComponentSample1Sn));
            Assert.That(StorageHardwareManifestPlugin.NVMe_String(nvmeCtrl.SUBNQN), Is.EqualTo(RegistryA41NvmeComponentSample1Subnqn));
            Assert.That(StorageHardwareManifestPlugin.NVMe_String(nvmeCtrl.FR), Is.EqualTo(RegistryA41NvmeComponentSample1Fr));
        });
    }

    [Test]
    public void TestPDL() {
        StorageLinux.GetPhysicalDevicePaths();
    }
}