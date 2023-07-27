package factory;

import com.fasterxml.jackson.databind.JsonNode;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.encoders.Base64;
import tcg.credential.URIReference;

/**
 * Functions to help manage the creation of a URI reference object.
 */
public class URIReferenceFactory {
    /**
     * fields of the URI JSON object
     */
    public enum Json {
        UNIFORMRESOURCEIDENTIFIER,
        HASHALGORITHM,
        HASHVALUE;
    }
    
    private DERIA5String uniformResourceIdentifier;
    private AlgorithmIdentifier hashAlgorithm; // optional
    private DERBitString hashValue; // optional
    
    private URIReferenceFactory() {
        uniformResourceIdentifier = null;
        hashAlgorithm = null;
        hashValue = null;
    }
    
    /**
     * Begin defining the URI reference.
     * @return A new URIReferenceFactory builder.
     */
    public static final URIReferenceFactory create() {
        return new URIReferenceFactory();
    }
    
    /**
     * Set the URI. Required field.
     * @param uri {@link DERIA5String}
     * @return The URIReferenceFactory object with the URI set.
     */
    public final URIReferenceFactory uniformResourceIdentifier(final DERIA5String uri) {
        uniformResourceIdentifier = uri;
        return this;
    }
    
    /**
     * Set the hash algorithm ID. Optional field.
     * @param algId {@link AlgorithmIdentifier}
     * @return The URIReferenceFactory object with the hash algorithm set.
     */
    public final URIReferenceFactory hashAlgorithm(final AlgorithmIdentifier algId) {
        hashAlgorithm = algId;
        return this;
    }
    
    /**
     * Set the hash value. Optional field.
     * @param bitString {@link DERBitString}
     * @return The URIReferenceFactory object with the hash value set.
     */
    public final URIReferenceFactory hashValue(final DERBitString bitString) {
        hashValue = bitString;
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link URIReference}
     */
    public final URIReference build() {
        if (uniformResourceIdentifier == null) {
            throw new IllegalArgumentException("A required field was empty.");
        }
        
        URIReference obj = new URIReference(uniformResourceIdentifier, hashAlgorithm, hashValue);
        
        return obj;
    }
    
    /**
     * Parse the JSON objects for component data.
     * @param refNode JsonNode with {@link URIReference} data
     * @see URIReferenceFactory.Json
     * @return The URIReferenceFactory object with new components from the JSON data.
     */
    public static final URIReferenceFactory fromJsonNode(final JsonNode refNode) {
        URIReferenceFactory urif = null;
        if (refNode.has(Json.UNIFORMRESOURCEIDENTIFIER.name())) {
            
            JsonNode uriNode = refNode.get(Json.UNIFORMRESOURCEIDENTIFIER.name());
            JsonNode hashAlgNode = refNode.get(Json.HASHALGORITHM.name());
            JsonNode hashNode = refNode.get(Json.HASHVALUE.name());
            
            final String uri = uriNode.asText();
            final String hashAlg = hashAlgNode != null ? hashAlgNode.asText() : "";
            final String hash = hashNode != null ? hashNode.asText() : "";
            
            if (!uri.isEmpty()) {
                urif = URIReferenceFactory.create()
                .uniformResourceIdentifier(new DERIA5String(uri));
                
                if (!hashAlg.isEmpty() && !hash.isEmpty()) {
                    urif.hashAlgorithm(new AlgorithmIdentifier(new ASN1ObjectIdentifier(hashAlg)));
                    urif.hashValue(new DERBitString(Base64.decode(hash)));
                }
            }
        }
        return urif;
    }
}
