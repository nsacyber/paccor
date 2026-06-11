package paccor.json;

import paccor.cert.CertTypeResolver;
import paccor.cert.PlatformCertificate;
import java.io.File;
import paccor.model.CertificateReference;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

public class PreviousPlatformCertificatesDeserializer extends CertificateBackedTraitMapDeserializer {
    @Override
    protected CertificateReference toReference(
            File file,
            PlatformCertificate certificate,
            JsonNode node)
            throws JacksonException {
        return CertTypeResolver.toReference(certificate);
    }
}
