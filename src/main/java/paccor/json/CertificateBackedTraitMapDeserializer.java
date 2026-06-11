package paccor.json;

import paccor.cert.CertTypeResolver;
import paccor.cert.PlatformCertificate;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import paccor.model.CertificateReference;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitMap;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Shared implementation for certificate-backed TraitMap fields.
 */
abstract class CertificateBackedTraitMapDeserializer extends ValueDeserializer<TraitMap> {
    @Override
    public TraitMap deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode root = context.readTree(p);
        TraitMap.TraitMapBuilder builder = TraitMap.builder();
        List<CertificateReference> referenceObjects = new ArrayList<>();

        if (root == null || root.isNull()) {
            return buildResolvedCollection(builder, referenceObjects);
        }

        for (JsonNode node : elements(root)) {
            if (node == null || node.isNull()) {
                continue;
            }

            JsonNode fileNode = JsonUtils.get(node, false, "file").orElse(null);
            if (fileNode != null && fileNode.isString() && !fileNode.asString().isBlank()) {
                addFileBackedTrait(builder, referenceObjects, fileNode.asString().trim(), node);
                continue;
            }

            Class<? extends Trait<?, ?>> traitClass = TraitDeserializer.resolveTraitClass(context, node);
            Trait<?, ?> trait = Trait.getInstance(node, traitClass);
            if (trait != null) {
                builder.trait(trait);
            }
        }

        return buildResolvedCollection(builder, referenceObjects);
    }

    private static List<JsonNode> elements(JsonNode root) {
        JsonNode traitsNode = root;
        if (root.isObject()) {
            traitsNode = JsonUtils.get(root, false, "traits").orElse(root);
        }

        if (traitsNode != null && traitsNode.isArray()) {
            return JsonUtils.asStream(traitsNode.spliterator()).toList();
        }
        return List.of(traitsNode);
    }

    private void addFileBackedTrait(
            TraitMap.TraitMapBuilder builder,
            List<CertificateReference> referenceObjects,
            String filename,
            JsonNode node) throws JacksonException {
        File file = new File(filename);
        PlatformCertificate certificate = PlatformCertificate.load(file);
        if (certificate == null) {
            throw JacksonIOException.construct(new IOException("Could not read certificate file: " + filename));
        }

        validateCertificate(file, certificate, node);
        CertificateReference referenceObject = toReference(file, certificate, node);
        if (referenceObject != null) {
            builder.trait(referenceObject.toTrait());
            referenceObjects.add(referenceObject);
        }
    }

    protected TraitMap buildResolvedCollection(
            TraitMap.TraitMapBuilder builder,
            List<CertificateReference> referenceObjects) {
        return new ResolvedCertificateReferenceMap(
                builder.build().getTraits(),
                referenceObjects);
    }

    protected void validateCertificate(
            File file,
            PlatformCertificate certificate,
            JsonNode node) throws JacksonException {}

    protected CertificateReference toReference(
            File file,
            PlatformCertificate certificate,
            JsonNode node) throws JacksonException {
        return CertTypeResolver.toReference(certificate);
    }
}
