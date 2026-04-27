package json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import tcg.credential.ASN1EnumBase;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code ASN1EnumBase} classes.
 */
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ASN1EnumBaseDeserializer extends ValueDeserializer<ASN1EnumBase<?>> {
    @NonNull
    private final transient Method method;

    @Override
    public ASN1EnumBase<?> deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode jsonNode = context.readTree(p);
        try {
            return (ASN1EnumBase<?>)method.invoke(null, jsonNode);
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw JacksonIOException.construct(new IOException(e));
        }
    }

    @Override
    public ValueDeserializer<ASN1EnumBase<?>> createContextual(DeserializationContext context, BeanProperty property) {
        Class<?> type = property == null
                ? Optional.ofNullable(context.getContextualType())
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
