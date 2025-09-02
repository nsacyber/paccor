package json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import tcg.credential.TBBSecurityAssertions;
import tcg.credential.TCGPlatformSpecification;
import tcg.credential.TCGSpecificationVersion;
import tcg.credential.URIReference;

@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
public class AttributesJsonHelper {
    private final TCGPlatformSpecification tCGPlatformSpecification;
    private final TCGSpecificationVersion tCGCredentialSpecification;
    private final TBBSecurityAssertions tBBSecurityAssertions;
    private final URIReference platformConfigUri;

    public static final AttributesJsonHelper read(@NonNull final String filename) {
        ObjectMapper mapper = JsonMapper.builder().build();
        try {
            return mapper.readValue(Path.of(filename).toFile(), AttributesJsonHelper.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
