using HardwareManifestPlugin;
using System.Security.Cryptography;
using System.Text.Json;

namespace HpEpsc;

/// <summary>
/// Prototype plugin for the HP Endpoint Security Controller (EpSC).
/// Collects the EpSC as a component, and proves the EpSC is present by having it sign bootlog evidence over a verifier's nonce.
/// Requires Windows, administrator rights, and HP's EpSC cmdlets (the HP.Security or HP.Validator module).
/// </summary>
public sealed class HpEpscHardwareManifestPlugin : HardwareManifestPluginBase {
    public static readonly string TraitDescription = "HP Endpoint Security Controller";
    public static readonly string TraitDescriptionUri = "https://developers.hp.com/hp-client-management/doc/security";
    public static readonly string PluginName = "paccor.hp.epsc";
    public static readonly string PluginDescription = "Collect the HP Endpoint Security Controller identity and evidence that it possesses its keys.";
    public const string EvidenceFormatName = "HP_EPSC_BOOTLOG";

    private EpscCertificates? certificates;
    private int componentIndex;

    public HpEpscHardwareManifestPlugin() {
        Name = PluginName;
        Description = PluginDescription;
        CollectsV2HardwareInformation = true;
    }

    public string LastError {
        get;
        private set;
    } = "";

    public override bool SupportsComponentEvidence => true;

    public override bool GatherHardwareIdentifiers() {
        certificates = LoadCertificates();
        if (certificates == null) {
            return false;
        }

        componentIndex = ManifestV2.COMPONENTS.Count;
        ManifestV2.COMPONENTS.Add(EpscComponent.FromDeviceIdCertificate(certificates.DeviceId));
        return true;
    }

    /// <param name="nonce">Must be 32 bytes, as required by the EpSC.</param>
    public override IReadOnlyList<ComponentEvidence> GatherComponentEvidence(byte[] nonce) {
        if (nonce.Length != EpscBootlogEvidence.NonceSize) {
            LastError = $"The EpSC requires a {EpscBootlogEvidence.NonceSize} byte nonce";
            return [];
        }

        certificates ??= LoadCertificates();
        byte[] evidence = certificates == null ? [] : ReadEvidence(nonce);
        if (certificates == null || evidence.Length == 0) {
            return [];
        }

        return [new ComponentEvidence {
            ComponentIndex = componentIndex,
            EvidenceFormat = EvidenceFormatName,
            EvidenceVersion = evidence[0].ToString(),
            Evidence = evidence,
            CertificateChain = certificates.EvidenceChain
        }];
    }

    private EpscCertificates? LoadCertificates() {
        if (!OperatingSystem.IsWindows()) {
            LastError = "HP EpSC collection requires Windows";
            return null;
        }

        if (!HpSecurityModule.TryGetCertificatesJson(out string json, out string error)) {
            LastError = error;
            return null;
        }

        try {
            EpscCertificates? loaded = EpscCertificates.FromJson(json);
            LastError = loaded == null ? "Get-HPEpscCerts did not return the Device ID and Attestation certificates" : "";
            return loaded;
        } catch (Exception e) when (e is JsonException or FormatException or CryptographicException) {
            LastError = "Unexpected certificate data from Get-HPEpscCerts: " + json[..Math.Min(json.Length, 300)];
            return null;
        }
    }

    private byte[] ReadEvidence(byte[] nonce) {
        if (!OperatingSystem.IsWindows()) {
            return [];
        }

        if (!HpSecurityModule.TryGetBootlogEvidence(out byte[] evidence, out string error, nonce)) {
            LastError = error;
            return [];
        }

        EpscBootlogEvidence? parsed = EpscBootlogEvidence.Parse(evidence);
        if (parsed == null || !parsed.Nonce.SequenceEqual(nonce)) {
            LastError = parsed == null ? "Evidence did not have the expected layout" : "Evidence does not contain the requested nonce";
            return [];
        }

        return evidence;
    }
}
