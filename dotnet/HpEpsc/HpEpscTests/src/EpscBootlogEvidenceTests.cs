using HpEpsc;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace HpEpscTests;

public class EpscBootlogEvidenceTests {
    // Fields of sample evidence returned by Get-HPEpscBootlogEvidence -Nonce on an EpSC Gen5 system
    private const string Version = "01";
    private const string Ecr0 = "4BA5F0E9" + "9800" + "00" + "66"
        + "932CCF627DAFE67A8A9C3442838DF4646A8B9314690CCBCFD77346437ACE74EE01476D9B67225C5DBA5B49FB2008824E78371D6F8A1C869AD559827B43902447" // ROM hash
        + "98D5D0EB69D238D93DAF65D4CECE3513FE7B359E2197712220C152C31E399D721FBB39B100FF4614F5DE2475397C2211D3A88B8A487B05EDDE88EC4FD3202F84" // L0 hash
        + "495A5D7E" + "1000" + "00" + "66" + "06" + "0F" + "FF" + "F0" + "3A" + "3A" + "3A" + "66"; // ACR0
    private const string DeviceIdCertHash = "68B88127046C37784120CA69F75B44102734EA036CFB9A8896F53D641D960AFA353B264DEDBBC13DE79BC12163FF0B82";
    private const string Ecr1 = "5C363539" + "2C01" + "00" + "03" + "01" + "000000"
        + DeviceIdCertHash
        + "0E93AF97976D0DFF8CF5569421914C781299C2D27126E8E8D0D8A3F482E350739567E54D9ECB1E98DA7965C150A36980" // L1 hash
        + "E105B01F256503B451D07520C972C50461058AC0B29B832158EEF5F64C28A57965DA4D43A1C1BE598966B7F505D83E25" // RSA key 1 hash
        + "2F112A22CD606F028D7AA7D39524A6DEF5E4E98A98F2AA5B3F40D06308A55299CA77F3082A06CEA37271433F864EFD53" // RSA key 2 hash
        + "E2A77D099537FE741E0E7380938536A4953D54A44FC3328F9FD4F213D512F641B3D91C6DE08B880B85320DFC3FC8182E" // HSS key 1 hash
        + "D3072EFAE5E76398F5BAF41B6570D65F273607C2A3D2F5078FD736EA214397232E30C0029F67243ED607D85B570CADBB"; // HSS key 2 hash
    private const string Nonce = "59CA36010A70077E0AB033C0BFD466E16BC02A668BB222E4534E5663525EA892";
    private const string AttestationCertDigest = "10EC790464F815858493CFC94A8D6E780D9D007071A383D7CDF78E53A5CD8453A396460D147090473BAA1E89A5688D74";
    private const string Uuid = "98EDB501D0A59E43A88BB8E15625E167";
    private const string Signature = "DFC6314ED967AEE03DC98843EA41C063FB9E76654DE9FF04B5D460B806950C1521CF4CE77A69EFED08F2B43F34D53E457FDF5FF432AED104341110CBB3425CFB58597FC1B6000510EA1F1DC861D680C0AC300BCA5ED1C7D4235B8A0FD96EAF7A";
    private static readonly byte[] SampleEvidence = Convert.FromHexString(Version + Ecr0 + Ecr1 + Nonce + AttestationCertDigest + Uuid + Signature);

    [Test]
    public void ParsesSampleEvidence() {
        EpscBootlogEvidence? evidence = EpscBootlogEvidence.Parse(SampleEvidence);

        Assert.That(evidence, Is.Not.Null);
        Assert.Multiple(() => {
            Assert.That(SampleEvidence, Has.Length.EqualTo(645));
            Assert.That(evidence!.Version, Is.EqualTo(1));
            Assert.That(evidence.Ecr0, Has.Length.EqualTo(152));
            Assert.That(evidence.Ecr1, Has.Length.EqualTo(300));
            Assert.That(Convert.ToHexString(evidence.DeviceIdCertHash), Is.EqualTo(DeviceIdCertHash));
            Assert.That(Convert.ToHexString(evidence.Nonce), Is.EqualTo(Nonce));
            Assert.That(Convert.ToHexString(evidence.AttestationCertDigest), Is.EqualTo(AttestationCertDigest));
            Assert.That(evidence.Uuid, Is.EqualTo(Guid.Parse("01B5ED98-A5D0-439E-A88B-B8E15625E167")));
            Assert.That(evidence.SignedData, Has.Length.EqualTo(549));
            Assert.That(Convert.ToHexString(evidence.Signature), Is.EqualTo(Signature));
        });
    }

    [Test]
    public void RejectsEvidenceWithUnexpectedLayout() {
        byte[] wrongVersion = (byte[])SampleEvidence.Clone();
        wrongVersion[0] = 2;
        byte[] wrongEcr0Tag = (byte[])SampleEvidence.Clone();
        wrongEcr0Tag[1] ^= 0xFF;

        Assert.Multiple(() => {
            Assert.That(EpscBootlogEvidence.Parse([]), Is.Null);
            Assert.That(EpscBootlogEvidence.Parse(wrongVersion), Is.Null);
            Assert.That(EpscBootlogEvidence.Parse(wrongEcr0Tag), Is.Null);
            Assert.That(EpscBootlogEvidence.Parse(SampleEvidence[..^1]), Is.Null);
        });
    }

    [Test]
    public void VerifiesSignatureOverEverythingBeforeIt() {
        using ECDsa key = ECDsa.Create(ECCurve.NamedCurves.nistP384);
        using X509Certificate2 signer = new CertificateRequest("CN=Test Attestation", key, HashAlgorithmName.SHA384).CreateSelfSigned(DateTimeOffset.UtcNow, DateTimeOffset.UtcNow.AddDays(1));
        byte[] signed = SampleEvidence[..^EpscBootlogEvidence.SignatureSize];
        byte[] evidence = [.. signed, .. key.SignData(signed, HashAlgorithmName.SHA384, DSASignatureFormat.IeeeP1363FixedFieldConcatenation)];
        byte[] tampered = (byte[])evidence.Clone();
        tampered[10] ^= 0x01;

        Assert.Multiple(() => {
            Assert.That(EpscBootlogEvidence.Parse(evidence)!.VerifySignature(signer), Is.True);
            Assert.That(EpscBootlogEvidence.Parse(tampered)!.VerifySignature(signer), Is.False);
            Assert.That(EpscBootlogEvidence.Parse(SampleEvidence)!.VerifySignature(signer), Is.False);
        });
    }
}
