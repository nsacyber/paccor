using HardwareManifestPlugin;
using HpEpsc;
using System.Management;
using System.Runtime.Versioning;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace HpEpscCli;

/// <summary>
/// Usage: HpEpscCli [output folder]
/// </summary>
public static class Program {
    public static int Main(string[] args) {
        if (!OperatingSystem.IsWindows()) {
            Console.Error.WriteLine("HP EpSC collection: FAILED (requires Windows)");
            return 1;
        }

        string outFolder = Path.GetFullPath(args.Length > 0 ? args[0] : "epsc-out");
        Directory.CreateDirectory(outFolder);
        return Run(outFolder);
    }

    [SupportedOSPlatform("windows")]
    private static int Run(string outFolder) {
        HpEpscHardwareManifestPlugin plugin = new();
        if (!plugin.GatherHardwareIdentifiers()) {
            Console.Error.WriteLine("HP EpSC identity: FAILED (" + plugin.LastError + ")");
            return 2;
        }

        File.WriteAllText(Path.Combine(outFolder, "manifest.json"), plugin.ManifestV2.ToString());
        Console.WriteLine("HP EpSC component: " + plugin.ManifestV2.COMPONENTS[0]);

        byte[] nonce = RandomNumberGenerator.GetBytes(EpscBootlogEvidence.NonceSize);
        IReadOnlyList<ComponentEvidence> evidence = plugin.GatherComponentEvidence(nonce);
        if (evidence.Count == 0) {
            Console.Error.WriteLine("HP EpSC evidence: FAILED (" + plugin.LastError + ")");
            return 3;
        }

        string evidenceFile = SaveResults(outFolder, nonce, evidence[0]);
        bool allPassed = CheckEvidence(evidence[0], nonce);
        PrintHpValidatorCommand(evidenceFile, nonce);
        return allPassed ? 0 : 4;
    }

    private static string SaveResults(string outFolder, byte[] nonce, ComponentEvidence evidence) {
        string evidenceFile = Path.Combine(outFolder, "evidence.bin");
        File.WriteAllBytes(evidenceFile, evidence.Evidence);
        File.WriteAllText(Path.Combine(outFolder, "nonce.hex"), Convert.ToHexString(nonce));
        File.WriteAllBytes(Path.Combine(outFolder, "attestation.cer"), evidence.CertificateChain[0]);
        File.WriteAllBytes(Path.Combine(outFolder, "deviceid.cer"), evidence.CertificateChain[1]);
        Console.WriteLine("Results saved to " + outFolder);
        return evidenceFile;
    }

    [SupportedOSPlatform("windows")]
    private static bool CheckEvidence(ComponentEvidence evidence, byte[] nonce) {
        EpscBootlogEvidence? parsed = EpscBootlogEvidence.Parse(evidence.Evidence);
        if (parsed == null) {
            Console.WriteLine("Evidence layout: FAILED");
            return false;
        }

        X509Certificate2 attestation = X509CertificateLoader.LoadCertificate(evidence.CertificateChain[0]);
        X509Certificate2 deviceId = X509CertificateLoader.LoadCertificate(evidence.CertificateChain[1]);
        Guid smbiosUuid = ReadSmbiosUuid();

        bool[] results = [
            Report("Evidence nonce matches request", parsed.Nonce.SequenceEqual(nonce)),
            Report($"Evidence UUID {parsed.Uuid} matches SMBIOS UUID {smbiosUuid}", parsed.Uuid == smbiosUuid),
            Report("Evidence signature verifies with the Attestation key", parsed.VerifySignature(attestation)),
            Report("Attestation certificate issued by the Device ID certificate", IsIssuedBy(attestation, deviceId)),
            Report("Attestation certificate digest in evidence " + DescribeDigest(parsed.AttestationCertDigest, attestation), MatchesEitherDigest(parsed.AttestationCertDigest, attestation)),
            Report("Device ID certificate hash in ECR1 " + DescribeDigest(parsed.DeviceIdCertHash, deviceId), MatchesEitherDigest(parsed.DeviceIdCertHash, deviceId))
        ];

        Console.WriteLine("Platform certificate identifier for the Device ID certificate: SHA-384 over the signature value, as paccor matches it");
        return results.All(passed => passed);
    }

    private static bool Report(string check, bool passed) {
        Console.WriteLine(check + ": " + (passed ? "PASS" : "FAIL"));
        return passed;
    }

    private static bool IsIssuedBy(X509Certificate2 certificate, X509Certificate2 issuer) {
        using ECDsa? key = issuer.GetECDsaPublicKey();
        return key != null && key.VerifyData(CertificateParts.TbsCertificate(certificate), CertificateParts.SignatureValue(certificate), HashAlgorithmName.SHA384, DSASignatureFormat.Rfc3279DerSequence);
    }

    private static bool MatchesEitherDigest(byte[] digest, X509Certificate2 certificate) {
        return digest.SequenceEqual(SHA384.HashData(certificate.RawData)) || digest.SequenceEqual(SHA384.HashData(CertificateParts.SignatureValue(certificate)));
    }

    private static string DescribeDigest(byte[] digest, X509Certificate2 certificate) {
        if (digest.SequenceEqual(SHA384.HashData(certificate.RawData))) {
            return "(SHA-384 over the whole certificate)";
        }
        return digest.SequenceEqual(SHA384.HashData(CertificateParts.SignatureValue(certificate))) ? "(SHA-384 over the signature value)" : "(no match)";
    }

    [SupportedOSPlatform("windows")]
    private static Guid ReadSmbiosUuid() {
        using ManagementObjectSearcher searcher = new("SELECT UUID FROM Win32_ComputerSystemProduct");
        string? uuid = searcher.Get().Cast<ManagementBaseObject>().Select(product => product["UUID"] as string).FirstOrDefault();
        return Guid.TryParse(uuid, out Guid parsed) ? parsed : Guid.Empty;
    }

    [SupportedOSPlatform("windows")]
    private static void PrintHpValidatorCommand(string evidenceFile, byte[] nonce) {
        Console.WriteLine();
        Console.WriteLine("Cross-check with HP's validator in Windows PowerShell:");
        Console.WriteLine($"Invoke-HPEpscBootlogEvidenceValidation -HpEpscEvidence (Get-HPEpscBootlogEvidence -InFile '{evidenceFile}') -HpEpscCerts (Get-HPEpscCerts) -Nonce ([byte[]]({string.Join(",", nonce)})) -Uuid '{ReadSmbiosUuid()}'");
    }
}
