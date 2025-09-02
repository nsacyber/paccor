package json;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.TargetInformation;

@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
public class ExtensionsJsonHelper {
    @JsonDeserialize(using = CertificatePoliciesDeserializer.class)
    private final CertificatePolicies certificatePolicies;
    @JsonAlias("authorityInfoAccess")
    @JsonDeserialize(using = AuthorityInformationAccessDeserializer.class)
    private final AuthorityInformationAccess authorityInformationAccess;
    @JsonAlias("crlDistribution")
    @JsonDeserialize(using = CrlDeserializer.class)
    private final CRLDistPoint crlDistPoint;
    @JsonDeserialize(using = KeyUsageDeserializer.class)
    private final KeyUsage keyUsage;
    @JsonAlias("targetInformation") // v1.1 support
    @JsonDeserialize(using = TargetingInformationDeserializer.class)
    private final TargetInformation targetingInformation;

    public static final ExtensionsJsonHelper read(@NonNull final String filename) {
        ObjectMapper mapper = JsonMapper.builder().build();
        try {
            return mapper.readValue(Path.of(filename).toFile(), ExtensionsJsonHelper.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
