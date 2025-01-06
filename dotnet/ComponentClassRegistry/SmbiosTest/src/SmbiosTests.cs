using HardwareManifestProto;
using Smbios;

namespace SmbiosTests {
    public class SmbiosTests {
        public static readonly string RegistrySampleDataA2Base64 = "X1NNM1/cGAMCAAEAVAkAABgAAAAAAAAAABgAAAECAPADD4CYi38BAAAAMwcID///U2FtcGxlIEJp\r\nb3MgVmVuZG9yAFExLjA5Lnh5egAwOS8wMS8yMDIwAAABGwEAAQIDBAACAAMABAAFAAYABwAIAAkG\r\nBQZTYW1wbGUgU3lzdGVtIE1hbnVmYWN0dXJlcgBTYW1wbGUgU3lzdGVtIE1vZGVsAFNhbXBsZSBT\r\neXN0ZW0gUmV2aXNpb24AU2FtcGxlIFN5c3RlbSBTZXJpYWwgTnVtYmVyAFRvIEJlIEZpbGxlZCBC\r\neSBPLkUuTS4AVG8gQmUgRmlsbGVkIEJ5IE8uRS5NLgAAAg8CAAECAwQFCQYDAAoAU2FtcGxlIEJh\r\nc2Vib2FyZCBNYW51ZmFjdHVyZXIAU2FtcGxlIEJhc2Vib2FyZCBNb2RlbABTYW1wbGUgQmFzZWJv\r\nYXJkIFJldmlzaW9uAFNhbXBsZSBCYXNlYm9hcmQgU2VyaWFsIE51bWJlcgBUbyBCZSBGaWxsZWQg\r\nQnkgTy5FLk0uAFRvIEJlIEZpbGxlZCBCeSBPLkUuTS4AAAMVAwABAwIDBAMDAwMAAAAAAAEAAFNh\r\nbXBsZSBDaGFzc2lzIE1hbnVmYWN0dXJlcgBTYW1wbGUgQ2hhc3NpcyBSZXZpc2lvbgBTYW1wbGUg\r\nQ2hhc3NpcyBTZXJpYWwgTnVtYmVyAFRvIEJlIEZpbGxlZCBCeSBPLkUuTS4AAAQoBAABA8YCpQYB\r\nAP/7678DAIUAagpABkEBBQAGAAcABAUGBAQIBABDUFVTb2NrZXQAU2FtcGxlIFByb2Nlc3NvciBN\r\nYW51ZmFjdHVyZXIAU2FtcGxlIFByb2Nlc3NvciBSZXZpc2lvbgBTYW1wbGUgUHJvY2Vzc29yIFNl\r\ncmlhbCBOdW1iZXIAVG8gQmUgRmlsbGVkIEJ5IE8uRS5NLgBUbyBCZSBGaWxsZWQgQnkgTy5FLk0u\r\nAAAEKAQAAQPvAqUGAQD/++u/AwCFAGoKQAZBBgUABgAHAAQFBgQECAQAQ1BVU29ja2V0AFNhbXBs\r\nZSBDUFUgTWFudWZhY3R1cmVyAFNhbXBsZSBDUFUgUmV2aXNpb24AU2FtcGxlIENQVSBTZXJpYWwg\r\nTnVtYmVyAFRvIEJlIEZpbGxlZCBCeSBPLkUuTS4AVG8gQmUgRmlsbGVkIEJ5IE8uRS5NLgAABxMF\r\nAAGAAAABAAEBAAEAAAQDBUwxLUNhY2hlAAAHEwYAAYEAAAQABAEAAQAABQUHTDItQ2FjaGUAAAcT\r\nBwABggEAIAAgAQABAAAFBQhMMy1DYWNoZQAACQ0IAAEGBQMDAQAMAVBDSTEAAAkNCQABBgUDAwIA\r\nDAFQQ0kyAAAJDQoAAaUIBAMRAAwBUENJRTEAAAkNCwABpQ0DAxIADAFQQ0lFMgAACQ0MAAGlCAMD\r\nEwAMAVBDSUUzAAAJDQ0AAaUNBAMUAAwBUENJRTQAAAkNDgABpQoDAxUADAFQQ0lFNQAAEA8PAAMD\r\nBgAAAAz+/wYAAAATDxAAAAAAAP//vwAPAAQAABFUEQAPAP7/SABAAAAICQABAhgCADUFAwQFBgAA\r\nAAAAAAAAAAAAAAABAAAHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAERJ\r\nTU0wAEJBTkswAFNhbXBsZSBSQU0gTWFudWZhY3R1cmVyAFNhbXBsZSBSQU0gU2VyaWFsIE51bWJl\r\nciAxAEFzc2V0VGFnTnVtMABTYW1wbGUgUkFNIE1vZGVsAFNhbXBsZSBSQU0gUmV2aXNpb24AABQT\r\nEgAAAAAA//8fABEAEAABAAEAABFUEwAPAP7/SABAAAAICQABAhgCADUFAwQFAAAAAAAAAAAAAAAA\r\nAAABAAAHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAERJTU0xAEJBTksx\r\nAFNhbXBsZSBSQU0gTWFudWZhY3R1cmVyAFNhbXBsZSBSQU0gU2VyaWFsIE51bWJlciAyAEFzc2V0\r\nVGFnTnVtMQBTYW1wbGUgUkFNIE1vZGVsAFNhbXBsZSBSQU0gUmV2aXNpb24AABQTFAAAACAA//8/\r\nABMAEAABAAEAABFUFQAPAP7/SABAAAAICQABAhgCADUFAwQFBgAAAAAAAAAAAAAAAAABAAAHAAAA\r\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAERJTU0yAEJBTksyAFNhbXBsZSBS\r\nQU0gTWFudWZhY3R1cmVyAFNhbXBsZSBSQU0gU2VyaWFsIE51bWJlciAzAEFzc2V0VGFnTnVtMgBT\r\nYW1wbGUgUkFNIE1vZGVsAFNhbXBsZSBSQU0gUmV2aXNpb24AABQTFgAAAEAA//9fABUAEAABAAEA\r\nABFUFwAPAP7/SABAAAAICQABAhgCADUFAwQFBgAAAAAAAAAAAAAAAAABAAAHAAAAAAAAAAAAAAAA\r\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAERJTU0zAEJBTkszAFNhbXBsZSBSQU0gTWFudWZh\r\nY3R1cmVyAFNhbXBsZSBSQU0gU2VyaWFsIE51bWJlciA0AEFzc2V0VGFnTnVtMwBTYW1wbGUgUkFN\r\nIE1vZGVsAFNhbXBsZSBSQU0gUmV2aXNpb24AABQTGAAAAGAA//9/ABcAEAABAAEAACAUHQAAAAAA\r\nAAAAAAAAAAAAAAAAAAAnFiEAAAECAwQFBgeAABIJ////////U2FtcGxlIFBvd2VyIFN1cHBseSBM\r\nb2NhdGlvbgBTYW1wbGUgUG93ZXIgU3VwcGx5IERldmljZSBOYW1lAFNhbXBsZSBQb3dlciBTdXBw\r\nbHkgTWFudWZhY3R1cmVyAFNhbXBsZSBQb3dlciBTdXBwbHkgU2VyaWFsIE51bWJlcgBTYW1wbGUg\r\nUG93ZXIgU3VwcGx5IEFzc2V0IFRhZyBOdW1iZXIAU2FtcGxlIFBvd2VyIFN1cHBseSBNb2RlbCBQ\r\nYXJ0IE51bWJlcgBTYW1wbGUgUG93ZXIgU3VwcGx5IFJldmlzaW9uIExldmVsAAArHyMAQUJDAAIA\r\nAQIDBAUGBwgBBAAAAAAAAAAAAAAAU2FtcGxlIFRQTSBEZXNjcmlwdGlvbgAAfwQlAAAA";
        public static readonly string[] ComponentIdentifiersInJson = new string[] {
            // BIOS
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00003307\" }," +
            " \"MANUFACTURER\": \"Sample Bios Vendor\"," +
            " \"MODEL\": \"Q1.09.xyz\"," +
            " \"REVISION\": \"080F\" }",
            // SYSTEM
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00010000\" }," +
            " \"MANUFACTURER\": \"Sample System Manufacturer\"," +
            " \"MODEL\": \"Sample System Model\"," +
            " \"SERIAL\": \"Sample System Serial Number\"," +
            " \"REVISION\": \"Sample System Revision\" }",
            // BASEBOARD
            "{"+
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"0002000A\" }," +
            " \"MANUFACTURER\": \"Sample Baseboard Manufacturer\"," +
            " \"MODEL\": \"Sample Baseboard Model\"," +
            " \"SERIAL\": \"Sample Baseboard Serial Number\"," +
            " \"REVISION\": \"Sample Baseboard Revision\"," +
            " \"FIELDREPLACEABLE\": \"true\" }",
            // CHASSIS
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00030003\" }," +
            " \"MANUFACTURER\": \"Sample Chassis Manufacturer\"," +
            " \"MODEL\": \"03\"," +
            " \"SERIAL\": \"Sample Chassis Serial Number\"," +
            " \"REVISION\": \"Sample Chassis Revision\" }",
            // PROCESSOR
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00040003\" }," +
            " \"MANUFACTURER\": \"Sample Processor Manufacturer\"," +
            " \"MODEL\": \"C6\"," +
            " \"SERIAL\": \"Sample Processor Serial Number\"," +
            " \"REVISION\": \"Sample Processor Revision\"," +
            " \"FIELDREPLACEABLE\": \"true\" }",
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00040003\" }," +
            " \"MANUFACTURER\": \"Sample CPU Manufacturer\"," +
            " \"MODEL\": \"EF\"," +
            " \"SERIAL\": \"Sample CPU Serial Number\"," +
            " \"REVISION\": \"Sample CPU Revision\"," +
            " \"FIELDREPLACEABLE\": \"false\" }",
            // RAM
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00110018\" }," +
            " \"MANUFACTURER\": \"Sample RAM Manufacturer\"," +
            " \"MODEL\": \"Sample RAM Model\"," +
            " \"SERIAL\": \"Sample RAM Serial Number 1\"," +
            " \"REVISION\": \"Sample RAM Revision\" }",
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00110018\" }," +
            " \"MANUFACTURER\": \"Sample RAM Manufacturer\"," +
            " \"SERIAL\": \"Sample RAM Serial Number 2\"," +
            " \"REVISION\": \"Sample RAM Revision\" }",
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00110018\" }," +
            " \"MANUFACTURER\": \"Sample RAM Manufacturer\"," +
            " \"MODEL\": \"Sample RAM Model\"," +
            " \"SERIAL\": \"Sample RAM Serial Number 3\"," +
            " \"REVISION\": \"Sample RAM Revision\" }",
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00110018\" }," +
            " \"MANUFACTURER\": \"Sample RAM Manufacturer\"," +
            " \"MODEL\": \"Sample RAM Model\"," +
            " \"SERIAL\": \"Sample RAM Serial Number 4\"," +
            " \"REVISION\": \"Sample RAM Revision\" }",
            // POWER SUPPLY
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"00270000\" }," +
            " \"MANUFACTURER\": \"Sample Power Supply Manufacturer\"," +
            " \"MODEL\": \"Sample Power Supply Model Part Number\"," +
            " \"SERIAL\": \"Sample Power Supply Serial Number\"," +
            " \"REVISION\": \"Sample Power Supply Revision Level\"," +
            " \"FIELDREPLACEABLE\": \"false\" }", 
            // TPM
            "{" +
            " \"COMPONENTCLASS\": {" +
              " \"COMPONENTCLASSREGISTRY\": \"2.23.133.18.3.3\"," +
              " \"COMPONENTCLASSVALUE\": \"002B0000\" }," +
            " \"MANUFACTURER\": \"41424300\"," +
            " \"MODEL\": \"0200\"," +
            " \"REVISION\": \"0102030405060708\" }"
        };

        [Test]
        public void TestSampleDataA2() {
            byte[] data = Convert.FromBase64String(RegistrySampleDataA2Base64);
            ManifestV2 manifestV2 = new();
            Dictionary<int, IList<SmbiosTable>> structures = Smbios.Smbios.ParseSmbiosData(data);
            SmbiosHardwareManifestPlugin.AddComponentsToManifestV2(structures, manifestV2);
            string jsonManifestV2 = manifestV2.ToString();

            Assert.That(ComponentIdentifiersInJson, Has.Length.GreaterThan(0));

            foreach (string componentJson in ComponentIdentifiersInJson) {
                Assert.That(jsonManifestV2, Contains.Substring(componentJson));
            }
        }
    }
}