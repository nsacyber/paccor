package json;

import java.io.IOException;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.bouncycastle.asn1.ASN1String;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom {@code ValueDeserializer} for the abstract {@code ASN1String} class.
 * Concrete ASN1Strings can extend this.
 * @param <T> The concrete ASN1String class.
 */
@AllArgsConstructor
public abstract class AbstractASN1StringDeserializer<T extends ASN1String> extends ValueDeserializer<T> {
    private final Function<String, T> constructor;
    private final Class<T> typeClass;

    @Override
    public T deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node == null || node.isNull()) return null;

        return JsonUtils.handleStringFormat(node)
                .map(constructor)
                .orElseThrow(() -> JacksonIOException.construct(new IOException("Unexpected JSON for " + typeClass + ": " + node)));
    }
}
