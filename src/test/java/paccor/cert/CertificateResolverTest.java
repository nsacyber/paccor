package paccor.cert;

import java.io.File;
import paccor.cert.CertKind;
import paccor.cert.CertSpecVersion;
import paccor.cert.CertificateProfile;
import paccor.cert.CertificateResolver;
import paccor.cert.TbsEnvelope;
import paccor.model.HolderInfo;
import paccor.model.SubjectInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CertificateResolverTest {
    private static final String RES_CA_CERT = "src/test/resources/TestCA.cert.example.pem";
    private static final String RES_HOLDER_CERT = "src/test/resources/ek.cer";
    private static final String RES_HOLDER_CERT_2187 = "src/test/resources/ek_cert_2187.der";
    private static final String RES_GEN1_BUILD_PCERT_FILE = "src/test/resources/sample_testgen1/platform_cert.20250909102720.crt";

    @Test
    void inferKind_prefersACForEKandAC() {
        CertKind t1 = CertificateResolver.inferKind(new File(RES_GEN1_BUILD_PCERT_FILE));
        // EK certs infer AC by design
        Assertions.assertSame(CertKind.AC, t1);

        CertKind t2 = CertificateResolver.inferKind(new File(RES_HOLDER_CERT_2187));
        // RES_HOLDER_CERT_2187 does not have TCG EK Extended Key Usage, so it infers PKC
        Assertions.assertSame(CertKind.PKC, t2);

        CertKind t3 = CertificateResolver.inferKind(null);
        Assertions.assertNull(t3);
    }

    @Test
    void resolveHolder_buildsHolderInfoFromX509() throws Exception {
        HolderInfo hi = CertificateResolver.resolveHolder(new File(RES_HOLDER_CERT), null);
        Assertions.assertNotNull(hi, "HolderInfo should be constructed from EK cert");
        Assertions.assertNotNull(hi.holder(), "ASN1 Holder should be present");
        Assertions.assertNotNull(hi.holderDerB64(), "B64 Holder should be present");
    }

    @Test
    void resolveSubject_returnsSubjectAndSpki() throws Exception {
        SubjectInfo subj = CertificateResolver.resolveSubject(new File(RES_CA_CERT));
        Assertions.assertNotNull(subj);
        Assertions.assertNotNull(subj.nameInfo().nameDerB64());
        Assertions.assertNotNull(subj.subjectPublicKeyInfoDerB64());
    }

    @Test
    void resolveKind_fallsBackCorrectly() {
        // Explicit
        Assertions.assertEquals(CertKind.PKC,
            CertificateResolver.resolveKind(CertKind.PKC, null, null));
        
        // Inferred
        Assertions.assertEquals(CertKind.AC,
            CertificateResolver.resolveKind(null, new File(RES_HOLDER_CERT), null));
        
        // Envelope
        TbsEnvelope env = TbsEnvelope.builder().type(CertKind.PKC).build();
        Assertions.assertEquals(CertKind.PKC,
            CertificateResolver.resolveKind(null, null, env));
            
        // Default
        Assertions.assertEquals(CertKind.AC,
            CertificateResolver.resolveKind(null, null, null));
    }

    @Test
    void inferProfile_detectsCorrectly() {
        Assertions.assertNull(CertificateResolver.inferProfile(null, CertSpecVersion.V1_0));

        // EK file -> AC (inferred), V2.0
        CertificateProfile p2 = CertificateResolver.inferProfile(new File(RES_GEN1_BUILD_PCERT_FILE), null);
        Assertions.assertEquals(CertSpecVersion.V2_0, p2.specVersion());
        Assertions.assertEquals(CertKind.AC, p2.outputType());
    }
}
