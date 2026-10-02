using HpEpsc;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text.Json;

namespace HpEpscTests;

public class EpscCertificatesTests {
    [Test]
    public void ReadsCertificatesByNameAndBuildsLeafFirstChain() {
        using X509Certificate2 deviceId = CreateCertificate("CN=Device ID");
        using X509Certificate2 attestation = CreateCertificate("CN=Attestation");
        using X509Certificate2 authentication = CreateCertificate("CN=Authentication");
        string json = ToJson((EpscCertificates.DeviceIdName, deviceId), (EpscCertificates.AttestationName, attestation), (EpscCertificates.DeviceAuthenticationName, authentication));

        EpscCertificates? certificates = EpscCertificates.FromJson(json);

        Assert.That(certificates, Is.Not.Null);
        Assert.Multiple(() => {
            Assert.That(certificates!.DeviceId.RawData, Is.EqualTo(deviceId.RawData));
            Assert.That(certificates.Attestation.RawData, Is.EqualTo(attestation.RawData));
            Assert.That(certificates.DeviceAuthentication!.RawData, Is.EqualTo(authentication.RawData));
            Assert.That(certificates.EvidenceChain, Is.EqualTo(new[] { attestation.RawData, deviceId.RawData }));
        });
    }

    [Test]
    public void RequiresDeviceIdAndAttestationCertificates() {
        using X509Certificate2 deviceId = CreateCertificate("CN=Device ID");

        Assert.Multiple(() => {
            Assert.That(EpscCertificates.FromJson("[]"), Is.Null);
            Assert.That(EpscCertificates.FromJson(ToJson((EpscCertificates.DeviceIdName, deviceId))), Is.Null);
        });
    }

    [Test]
    public void RejectsCollectionShapedNames() {
        // What the original collection script produced when Get-HPEpscCerts returned one collection
        const string json = "[{\"Name\":[\"HP EpSC Device ID Certificate\",\"HP EpSC Attestation Certificate\"],\"Der\":\"\"}]";

        Assert.Throws<JsonException>(() => EpscCertificates.FromJson(json));
    }

    private static string ToJson(params (string Name, X509Certificate2 Certificate)[] certificates) {
        return JsonSerializer.Serialize(certificates.Select(entry => new { entry.Name, Der = Convert.ToBase64String(entry.Certificate.RawData) }));
    }

    private static X509Certificate2 CreateCertificate(string subject) {
        using ECDsa key = ECDsa.Create(ECCurve.NamedCurves.nistP384);
        return new CertificateRequest(subject, key, HashAlgorithmName.SHA384).CreateSelfSigned(DateTimeOffset.UtcNow, DateTimeOffset.UtcNow.AddDays(1));
    }
}
