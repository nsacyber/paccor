package json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import model.CertificateReference;
import tcg.credential.Trait;
import tcg.credential.TraitMap;

/**
 * Trait map that also carries file-backed certificate reference metadata
 * used to build those traits.
 */
public class ResolvedCertificateReferenceMap extends TraitMap {
    @JsonIgnore
    private final List<CertificateReference> certificateReferences;

    public ResolvedCertificateReferenceMap(
            Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> traits,
            List<CertificateReference> certificateReferences) {
        super(traits);
        this.certificateReferences = List.copyOf(certificateReferences != null ? certificateReferences : List.of());
    }

    @JsonIgnore
    public List<CertificateReference> certificateReferences() {
        return certificateReferences;
    }

    public static List<CertificateReference> referencesOf(TraitMap traits) {
        if (traits instanceof ResolvedCertificateReferenceMap resolved) {
            return resolved.certificateReferences();
        }
        return List.of();
    }
}
