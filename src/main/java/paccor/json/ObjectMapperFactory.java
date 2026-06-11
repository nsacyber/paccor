package paccor.json;

import paccor.exception.JsonException;
import java.io.File;
import java.nio.file.Files;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

public final class ObjectMapperFactory {
    private static final ThreadLocal<ObjectMapper> MAPPER =
            ThreadLocal.withInitial(ObjectMapperFactory::create);

    private ObjectMapperFactory() {}

    public static final ObjectMapper create() {
        return JsonMapper.builder()
                .addModule(new JacksonAsn1Module())
                .build();
    }

    public static ObjectMapper get() {
        return MAPPER.get();
    }

    public static final <T> T fromJsonNode(JsonNode node, Class<T> type) {
        return get().convertValue(node, type);
    }

    public static final <T> T fromJson(String json, Class<T> type) throws JsonException {
        return withOrWithoutModule(om -> {
            try {
                return om.readValue(json, type);
            } catch (JacksonException e) {
                throw new JsonException(e);
            }
        });
    }

    public static final <T> T fromJson(File jsonFile, Class<T> type) throws JsonException {
        return withOrWithoutModule(om -> {
                try {
                    return om.readValue(jsonFile, type);
                } catch (JacksonException e) {
                    throw new JsonException(jsonFile.getAbsolutePath(), e);
                }
            });
    }

    public static final <T> T fromJsonSafe(String json, Class<T> type) {
        T result = null;
        try {
            result = fromJson(json, type);
        } catch (Exception ignored) {
            // intentionally ignored
        }
        return result;
    }

    public static final <T> T fromJsonSafe(File jsonFile, Class<T> type) {
        T result = null;
        try {
            result = fromJson(jsonFile, type);
        } catch (Exception ignored) {
            // intentionally ignored
        }
        return result;
    }

    @FunctionalInterface
    private interface MapperReader<T> {
        T read(ObjectMapper mapper) throws JsonException;
    }

    private static <T> T withOrWithoutModule(MapperReader<T> read) throws JsonException {
        T value;

        try {
            value = withModule(read);
        } catch (JsonException exceptionWithModule) {
            try {
                value = withoutModule(read);
            } catch (JsonException exceptionWithoutModule) {
                exceptionWithModule.addSuppressed(exceptionWithoutModule);
                throw exceptionWithModule;
            }
        }

        return value;
    }

    private static <T> T withModule(MapperReader<T> read) throws JsonException {
        return read.read(get());
    }
    private static <T> T withoutModule(MapperReader<T> read) throws JsonException {
        return read.read(new ObjectMapper());
    }

    public static final void write(File f, Object obj) throws JsonException {
        try {
            ObjectWriter om = ObjectMapperFactory.get().writerWithDefaultPrettyPrinter();
            Files.writeString(f.toPath(), om.writeValueAsString(obj));
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }
}
