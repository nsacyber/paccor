package json;

import cert.PlatformCertificate;
import java.io.File;
import java.io.IOException;
import java.util.List;
import model.CertificateReference;
import tcg.credential.TraitMap;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.JsonNode;

public class CryptographicAnchorsDeserializer extends CertificateBackedTraitMapDeserializer {
    @Override
    protected TraitMap buildResolvedCollection(
            TraitMap.TraitMapBuilder builder,
            List<CertificateReference> referenceObjects) {
        return new ResolvedCertificateReferenceMap(builder.build().getTraits(), referenceObjects);
    }

    @Override
    protected void validateCertificate(
            File file,
            PlatformCertificate certificate,
            JsonNode node) throws JacksonException {
        if (certificate.isAttributeCertificate()) {
            throw JacksonIOException.construct(new IOException(
                    "CryptographicAnchors FILE entries must reference public key certificates: " + file.getPath()));
        }
    }

    @Override
    protected CertificateReference toReference(
            File file,
            PlatformCertificate certificate,
            JsonNode node)
            throws JacksonException {
        return CryptographicAnchorClassifier.toReference(certificate);
    }
}
