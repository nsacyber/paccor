package cert;

import cli.ClientExitCodes;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import exception.JsonException;
import exception.PaccorException;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import json.ObjectMapperFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.encoders.Base64;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TbsEnvelope {
    private CertKind type; // AC or PKC
    private CertSpecVersion certSpecVersion; // V1_0, V1_1, or V2_0 (defaults to V2_0)
    private String tbsDerB64;            // Base64 DER of ACINFO or TBSCertificate
    private String sigAlgDerB64;         // Base64 DER AlgorithmIdentifier
    private String sha256OfTbs;          // optional

    // Canonical model snapshot for certificate contents; single source of truth for issuer/subject/holder data.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String platformInfoJson;     // JSON(PlatformCertificateInformationModel)

    public static final TbsEnvelope read(@NonNull final String filename) throws JsonException {
        return read(Path.of(filename).toFile());
    }

    public static final TbsEnvelope read(@NonNull final File jsonFile) throws JsonException {
        return ObjectMapperFactory.fromJson(jsonFile, TbsEnvelope.class);
    }

    public AlgorithmIdentifier decodeAlgId() throws PaccorException {
        if (this.getSigAlgDerB64() == null) {
            return null;
        }
        AlgorithmIdentifier algId;
        try {
            algId = AlgorithmIdentifier.getInstance(ASN1Primitive.fromByteArray(Base64.decode(this.getSigAlgDerB64())));
        } catch (Exception e) {
            throw new PaccorException(ClientExitCodes.RUNTIME_ERROR, e);
        }
        return algId;
    }

    public byte[] decode() {
        return Optional.ofNullable(this.getTbsDerB64())
                .map(Base64::decode)
                .orElse(new byte[0]);
    }
}
