package tcg.credential;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

@FunctionalInterface
public interface TraitSequenceFilter {
    Set<Class<? extends Trait<?, ?>>> getSupportedTraitTypes();
}
