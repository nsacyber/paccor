package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import tcg.credential.ASN1EnumBase;

@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ASN1EnumBaseDeserializer extends JsonDeserializer<ASN1EnumBase<?>> implements ContextualDeserializer {
    @NonNull
    private final transient Method method;

    @Override
    public ASN1EnumBase<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        try {
            return (ASN1EnumBase<?>)method.invoke(null, jsonNode);
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new IOException(e);
        }
    }

    @Override
    public JsonDeserializer<ASN1EnumBase<?>> createContextual(DeserializationContext ctxt, BeanProperty property) {
        Class<?> type = property == null
                ? Optional.ofNullable(ctxt.getContextualType())
                        .map(JavaType::getRawClass)
                        .orElse(null)
                : Optional.ofNullable(property.getType())
                        .map(JavaType::getRawClass)
                        .orElse(null);

        if (type == null || !ASN1EnumBase.class.isAssignableFrom(type)) {
            return this;
        }

        try {
            Method method = type.getMethod("getInstance", Object.class);
            return new ASN1EnumBaseDeserializer(method);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Classes that extend ASN1EnumBase must have a static" +
                    " getInstance(Object) method. " + e.getMessage(), e);
        }
    }
}