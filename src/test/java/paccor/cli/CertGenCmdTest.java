package paccor.cli;

import paccor.cert.TbsEnvelope;
import java.io.File;
import paccor.json.ObjectMapperFactory;
import paccor.model.PlatformCertificateInformationModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class CertGenCmdTest extends TestSupport {

    @Test
    public void testGeneratePkcEnvelope_withIssuerAndHolder() throws Exception {
        File out = tempFile("certgen-", ".json");
        File issuer = new File("src/test/resources/TestCA.cert.example.pem");
        File holder = new File("src/test/resources/TestCA.cert.example.pem");

        int code = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--out", out.getAbsolutePath(),
                "--kind", "PKC",
                "--issuer-cert", issuer.getAbsolutePath(),
                "--holder-cert", holder.getAbsolutePath()
        );
        Assertions.assertEquals(0, code);
        TbsEnvelope env = ObjectMapperFactory.get().readValue(out, TbsEnvelope.class);
        Assertions.assertNotNull(env.getSigAlgDerB64());
        Assertions.assertNotNull(env.getTbsDerB64());
        Assertions.assertNotNull(env.getPlatformInfoJson());
        
        PlatformCertificateInformationModel pi = ObjectMapperFactory.get().readValue(env.getPlatformInfoJson(), PlatformCertificateInformationModel.class);
        Assertions.assertNotNull(pi.getIssuer());
        Assertions.assertNotNull(pi.getSubject());
    }

    @Test
    public void testFinalizeFailsWhenCannotRebuildTbs() throws Exception {
        File out = tempFile("certgen-finalize-", ".json");
        CommandLine cmd = new CommandLine(new RootCmd());
        // In picocli, execution exception handler might suppress the exception from maybeFinalize
        // unless we rethrow it or check the code.
        int code = cmd.execute(
                "certgen",
                "--out", out.getAbsolutePath(),
                "--finalize"
        );
        // TbsBuilder.maybeFinalize throws IllegalStateException if rr.tbsB64() is null and finalize is true
        // Default picocli handler prints stack trace and returns 1
        Assertions.assertNotEquals(0, code);
    }

    @Test
    public void testCertGen_withAttributesAndComponents() throws Exception {
        File out = tempFile("certgen-full-", ".json");
        File issuer = new File("src/test/resources/TestCA.cert.example.pem");
        File holder = new File("src/test/resources/TestCA.cert.example.pem");
        File attr = new File("src/test/resources/paccor-gen1-samples/attributes.json");
        File comp = new File("src/test/resources/paccor-gen1-samples/components-v3.json");

        if (!attr.exists() || !comp.exists()) {
            return; // Skip if resources are not found in this environment
        }

        int code = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--out", out.getAbsolutePath(),
                "--kind", "AC",
                "--issuer-cert", issuer.getAbsolutePath(),
                "--holder-cert", holder.getAbsolutePath(),
                "--attributes-json", attr.getAbsolutePath(),
                "--components-json", comp.getAbsolutePath(),
                "--serial", "1234",
                "--finalize"
        );
        Assertions.assertEquals(0, code);
        TbsEnvelope env = ObjectMapperFactory.get().readValue(out, TbsEnvelope.class);
        Assertions.assertNotNull(env.getPlatformInfoJson());

        PlatformCertificateInformationModel pi = ObjectMapperFactory.get().readValue(env.getPlatformInfoJson(), PlatformCertificateInformationModel.class);
        Assertions.assertNotNull(pi.getTcgPlatformSpecification());
        Assertions.assertNotNull(pi.getPlatformConfiguration());
        Assertions.assertFalse(pi.getPlatformConfiguration().getPlatformComponents().isEmpty());
    }
}
