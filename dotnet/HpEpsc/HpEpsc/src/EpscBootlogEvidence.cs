using System.Buffers.Binary;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace HpEpsc;

/// <summary>
/// Reads HP EpSC bootlog evidence as written by Get-HPEpscBootlogEvidence -OutFile.
/// The layout comes from analysis of sample evidence, not from an HP specification:
/// version (1) | ECR0 record | ECR1 record | nonce (32) | attestation certificate digest (48) | system UUID (16) | ECDSA P-384 signature r||s (96).
/// Each record starts with a 4 byte tag and a 2 byte little endian size that includes the tag and size.
/// The signature covers every byte before it.
/// </summary>
internal sealed class EpscBootlogEvidence {
    public const byte SupportedVersion = 1;
    public const int NonceSize = 32;
    public const int Sha384Size = 48;
    public const int UuidSize = 16;
    public const int SignatureSize = 96;
    private const int RecordHeaderSize = 6;
    private const int Ecr1FixedFieldsSize = 6; // version, auth config, header version, 3 reserved bytes
    private static readonly byte[] Ecr0Tag = [0x4B, 0xA5, 0xF0, 0xE9];
    private static readonly byte[] Ecr1Tag = [0x5C, 0x36, 0x35, 0x39];

    public byte Version { get; private init; }
    public byte[] Ecr0 { get; private init; } = [];
    public byte[] Ecr1 { get; private init; } = [];
    public byte[] DeviceIdCertHash { get; private init; } = [];
    public byte[] Nonce { get; private init; } = [];
    public byte[] AttestationCertDigest { get; private init; } = [];
    public Guid Uuid { get; private init; }
    public byte[] SignedData { get; private init; } = [];
    public byte[] Signature { get; private init; } = [];

    /// <returns>The parsed evidence, or null if it does not have the expected layout.</returns>
    public static EpscBootlogEvidence? Parse(byte[] evidence) {
        if (evidence.Length == 0 || evidence[0] != SupportedVersion) {
            return null;
        }

        int pos = 1;
        byte[]? ecr0 = ReadRecord(evidence, ref pos, Ecr0Tag);
        byte[]? ecr1 = ReadRecord(evidence, ref pos, Ecr1Tag);
        int trailerSize = NonceSize + Sha384Size + UuidSize + SignatureSize;
        int deviceIdHashStart = RecordHeaderSize + Ecr1FixedFieldsSize;

        if (ecr0 == null || ecr1 == null || ecr1.Length < deviceIdHashStart + Sha384Size || evidence.Length - pos != trailerSize) {
            return null;
        }

        int digestStart = pos + NonceSize;
        int uuidStart = digestStart + Sha384Size;
        int signatureStart = uuidStart + UuidSize;

        return new EpscBootlogEvidence {
            Version = evidence[0],
            Ecr0 = ecr0,
            Ecr1 = ecr1,
            DeviceIdCertHash = ecr1[deviceIdHashStart..(deviceIdHashStart + Sha384Size)],
            Nonce = evidence[pos..digestStart],
            AttestationCertDigest = evidence[digestStart..uuidStart],
            Uuid = new Guid(evidence.AsSpan(uuidStart, UuidSize)), // SMBIOS UUID byte order matches Guid
            SignedData = evidence[..signatureStart],
            Signature = evidence[signatureStart..]
        };
    }

    /// <summary>
    /// Checks the evidence signature with the public key of the certificate expected to have signed it (the Attestation certificate).
    /// </summary>
    public bool VerifySignature(X509Certificate2 signer) {
        using ECDsa? key = signer.GetECDsaPublicKey();
        return key != null && key.VerifyData(SignedData, Signature, HashAlgorithmName.SHA384, DSASignatureFormat.IeeeP1363FixedFieldConcatenation);
    }

    private static byte[]? ReadRecord(byte[] evidence, ref int pos, byte[] expectedTag) {
        if (evidence.Length - pos < RecordHeaderSize || !evidence.AsSpan(pos, expectedTag.Length).SequenceEqual(expectedTag)) {
            return null;
        }

        int size = BinaryPrimitives.ReadUInt16LittleEndian(evidence.AsSpan(pos + expectedTag.Length, 2));
        if (size < RecordHeaderSize || evidence.Length - pos < size) {
            return null;
        }

        byte[] record = evidence[pos..(pos + size)];
        pos += size;
        return record;
    }
}
