package crypto;

import cert.CertSigEncoding;
import cli.ClientExitCodes;
import exception.JsonException;
import exception.PaccorException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import json.ObjectMapperFactory;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record RemoteSignatureStrategy(
        String remoteUrl,
        String remoteAuth,
        int remoteTimeoutMs) implements SignatureStrategy {
    @Override
    public byte[] sign(byte[] tbs, AlgorithmIdentifier algId) throws PaccorException {
        algId = AlgorithmSupport.ensureNullParamsForEcdsa(algId);
        return signRemote(tbs, algId, remoteUrl, remoteAuth, remoteTimeoutMs);
    }

    @Override public boolean isLocal() {
        return false;
    }

    // Remote signing: POST JSON { algOid, payloadB64 } and expect { signatureB64, encoding }
    public static byte[] signRemote(byte[] tbs, AlgorithmIdentifier algId, String url, String auth, int timeoutMs) throws PaccorException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        String json = createRequestBody(tbs, algId);
        HttpRequest request = createRequest(json, url, auth, timeoutMs);
        HttpResponse<String> resp;
        try {
            resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new PaccorException(ClientExitCodes.REMOTE_ERROR, e);
        }
        if (resp.statusCode() / 100 != 2) {
            throw new PaccorException(ClientExitCodes.REMOTE_ERROR, "Remote signer HTTP " + resp.statusCode());
        }
        return readResponseBody(resp, algId);
    }

    // Remote signing: POST JSON { algOid, payloadB64 }
    private static String createRequestBody(byte[] tbs, AlgorithmIdentifier algId) throws PaccorException {
        Map<String, Object> req = new HashMap<>();
        req.put("algOid", algId.getAlgorithm().getId());
        req.put("payloadB64", Base64.getEncoder().encodeToString(tbs));
        ObjectMapper om = ObjectMapperFactory.create();
        String json = null;
        try {
            json = om.writeValueAsString(req);
        } catch (JacksonException e) {
            throw new JsonException(e);
        }
        return json;
    }

    private static HttpRequest createRequest(String jsonBody, String url, String auth, int timeoutMs) {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json");
        if (auth != null && !auth.isEmpty()) {
            if (auth.startsWith("bearer:")) {
                rb.header("Authorization", "Bearer " + auth.substring(7));
            } else if (auth.startsWith("basic:")) {
                String b = auth.substring(6);
                String enc = Base64.getEncoder().encodeToString(b.getBytes(StandardCharsets.UTF_8));
                rb.header("Authorization", "Basic " + enc);
            } else if (auth.startsWith("header:")) {
                String hv = auth.substring(7);
                int idx = hv.indexOf('=');
                if (idx > 0) rb.header(hv.substring(0, idx), hv.substring(idx+1));
            }
        }
        return rb.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
    }

    // Remote signing: POST response expect { signatureB64, encoding }
    private static byte[] readResponseBody(HttpResponse<String> resp, AlgorithmIdentifier algId) throws PaccorException {
        ObjectMapper om = ObjectMapperFactory.create();
        JsonNode node = null;
        try {
            node = om.readTree(resp.body());
        } catch (JacksonException e) {
            throw new JsonException(e);
        }
        String sigB64 = node.path("signatureB64").asString(null);
        String enc = node.path("encoding").asString("der");
        if (sigB64 == null) {
            throw new PaccorException(ClientExitCodes.REMOTE_ERROR, "Remote signer response missing signatureB64");
        }
        byte[] sig = Base64.getDecoder().decode(sigB64);
        CertSigEncoding se = CertSigEncoding.DER;
        try {
            se = CertSigEncoding.valueOf(enc.toUpperCase());
        } catch (Exception ignored) {}
        return AlgorithmSupport.maybeConvertToDer(sig, se, algId);
    }
}
