using HardwareManifestProto;
using HpEpsc;
using System.Formats.Asn1;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace HpEpscTests;

public class EpscComponentTests {
    private const string DeviceSerial = "0x01775AB4FCD34768DB";
    private const string DeviceCommonName = "HP EpSC 24 ROM v21.22.00.03, L0 v21.21.01.80";

    [Test]
    public void DescribesEpscFromDeviceIdCertificate() {
        using X509Certificate2 deviceId = CreateDeviceIdCertificate(DeviceCommonName);

        ComponentIdentifier component = EpscComponent.FromDeviceIdCertificate(deviceId);

        Assert.Multiple(() => {
            Assert.That(component.COMPONENTCLASS.COMPONENTCLASSREGISTRY, Is.EqualTo("2.23.133.18.3.1"));
            Assert.That(component.COMPONENTCLASS.COMPONENTCLASSVALUE, Is.EqualTo("00010008"));
            Assert.That(component.MANUFACTURER, Is.EqualTo("HP Inc."));
            Assert.That(component.MODEL, Is.EqualTo("HP EpSC 24"));
            Assert.That(component.SERIAL, Is.EqualTo(DeviceSerial));
            Assert.That(component.REVISION, Is.EqualTo("ROM v21.22.00.03, L0 v21.21.01.80"));
            Assert.That(component.PLATFORMCERT.HASHEDCERTIDENTIFIER.HASHALG, Is.EqualTo(EpscComponent.Sha384Oid));
            Assert.That(component.PLATFORMCERT.HASHEDCERTIDENTIFIER.HASHVALUE, Is.EqualTo(Convert.ToBase64String(SHA384.HashData(CertificateParts.SignatureValue(deviceId)))));
        });
    }

    [Test]
    public void UsesWholeCommonNameAsModelWhenItHasNoRomVersion() {
        using X509Certificate2 deviceId = CreateDeviceIdCertificate("HP EpSC 25");

        ComponentIdentifier component = EpscComponent.FromDeviceIdCertificate(deviceId);

        Assert.Multiple(() => {
            Assert.That(component.MODEL, Is.EqualTo("HP EpSC 25"));
            Assert.That(component.REVISION, Is.Empty);
        });
    }

    [Test]
    public void CertificatePartsReassembleIntoAVerifiableSignature() {
        using ECDsa key = ECDsa.Create(ECCurve.NamedCurves.nistP384);
        using X509Certificate2 certificate = new CertificateRequest("CN=Parts", key, HashAlgorithmName.SHA384).CreateSelfSigned(DateTimeOffset.UtcNow, DateTimeOffset.UtcNow.AddDays(1));

        bool verified = key.VerifyData(CertificateParts.TbsCertificate(certificate), CertificateParts.SignatureValue(certificate), HashAlgorithmName.SHA384, DSASignatureFormat.Rfc3279DerSequence);

        Assert.That(verified, Is.True);
    }

    // Mirrors the self-signed EpSC Device ID certificate subject: SERIALNUMBER, CN
    private static X509Certificate2 CreateDeviceIdCertificate(string commonName) {
        X500DistinguishedNameBuilder subject = new();
        subject.Add("2.5.4.5", DeviceSerial, UniversalTagNumber.PrintableString);
        subject.AddCommonName(commonName);
        using ECDsa key = ECDsa.Create(ECCurve.NamedCurves.nistP384);
        return new CertificateRequest(subject.Build(), key, HashAlgorithmName.SHA384).CreateSelfSigned(DateTimeOffset.UtcNow, DateTimeOffset.UtcNow.AddDays(1));
    }
}
