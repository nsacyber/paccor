using System.Security.Cryptography.X509Certificates;
using System.Text.Json;

namespace HpEpsc;

/// <summary>
/// The certificates reported by Get-HPEpscCerts.
/// The Device ID certificate is self-signed. It issues the Attestation certificate, whose key signs bootlog evidence.
/// </summary>
internal sealed class EpscCertificates {
    public const string DeviceIdName = "HP EpSC Device ID Certificate";
    public const string AttestationName = "HP EpSC Attestation Certificate";
    public const string DeviceAuthenticationName = "HP EpSC Device Authentication Certificate";

    public required X509Certificate2 DeviceId {
        get;
        init;
    }

    public required X509Certificate2 Attestation {
        get;
        init;
    }

    public X509Certificate2? DeviceAuthentication {
        get;
        init;
    }

    /// <summary>
    /// Certificates for the evidence signing key, leaf first.
    /// </summary>
    public IReadOnlyList<byte[]> EvidenceChain => [Attestation.RawData, DeviceId.RawData];

    /// <summary>
    /// Reads a JSON array of { "Name": ..., "Der": base64 } objects.
    /// </summary>
    /// <returns>The certificates, or null if the Device ID or Attestation certificate is missing.</returns>
    public static EpscCertificates? FromJson(string json) {
        Dictionary<string, X509Certificate2> byName = [];
        foreach (NamedCertificate entry in JsonSerializer.Deserialize<List<NamedCertificate>>(json) ?? []) {
            byName.TryAdd(entry.Name, X509CertificateLoader.LoadCertificate(Convert.FromBase64String(entry.Der)));
        }

        if (!byName.TryGetValue(DeviceIdName, out X509Certificate2? deviceId) || !byName.TryGetValue(AttestationName, out X509Certificate2? attestation)) {
            return null;
        }

        return new EpscCertificates {
            DeviceId = deviceId,
            Attestation = attestation,
            DeviceAuthentication = byName.GetValueOrDefault(DeviceAuthenticationName)
        };
    }

    private sealed record NamedCertificate(string Name, string Der);
}
