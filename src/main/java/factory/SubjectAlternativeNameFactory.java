package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import tcg.credential.TCGObjectIdentifier;

/**
 * Functions to help manage the creation of a subject alternative name extension.
 */
public class SubjectAlternativeNameFactory {
    /**
     * field names relevant to a SAN object for a platform credential
     */
    public enum Json {
        PLATFORM;
    }
    
    /**
     * platform certificate subject alternative name element options
     */
    public enum ElementJson {
        PLATFORMMODEL(TCGObjectIdentifier.tcgAtPlatformModel),
        PLATFORMMANUFACTURERSTR(TCGObjectIdentifier.tcgAtPlatformManufacturerStr),
        PLATFORMVERSION(TCGObjectIdentifier.tcgAtPlatformVersion),
        PLATFORMSERIAL(TCGObjectIdentifier.tcgAtPlatformSerial),
        PLATFORMMANUFACTURERID(TCGObjectIdentifier.tcgAtPlatformManufacturerId);
        
        private ASN1ObjectIdentifier oid;
        
        private ElementJson(final ASN1ObjectIdentifier oid) {
            this.oid = oid;
        }
        
        public final ASN1ObjectIdentifier getOid() {
            return oid;
        }
    }
    
    private GeneralNames san;
    private Vector<RDN> names;
    
    private SubjectAlternativeNameFactory() {
        names = new Vector<RDN>();
        san = null;
    }
    
    /**
     * Begin defining the subject alternative name extension.
     * @return A new SubjectAlternativeNameFactory builder.
     */
    public static final SubjectAlternativeNameFactory create() {
        return new SubjectAlternativeNameFactory();
    }
    
    /**
     * Add another descriptor.
     * @param name {@link RDN}
     * @return The SubjectAlternativeNameFactory object with an RDN added.
     */
    public final SubjectAlternativeNameFactory addRDN(final RDN name) {
        if (name != null) {
            names.add(name);
        }
        return this;
    }
    
    /**
     * Add another descriptor.
     * @param oid {@link ASN1ObjectIdentifier}
     * @param name {@link DERUTF8String}
     * @return The SubjectAlternativeNameFactory object with an RDN added.
     */
    public final SubjectAlternativeNameFactory addRDN(final ASN1ObjectIdentifier oid, final DERUTF8String name) {
        if (oid != null && name != null) {
            addRDN(new RDN(oid, name));
        }
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link GeneralNames}
     */
    public final GeneralNames build() {
        X500Name name = new X500Name(names.toArray(new RDN[names.size()]));
        GeneralName gn = new GeneralName(name);
        san = new GeneralNames(gn);
        return san;
    }
    
    /**
     * Read a file for JSON data to incorporate into the subject alternative name.
     * @param jsonFile String file to read containing JSON data
     * @see SubjectAlternativeNameFactory.Json
     * @throws IOException If there are issues reading the file or with the JSON structure.
     * @return The SubjectAlternativeNameFactory object with new information from the file.
     */
    public static final SubjectAlternativeNameFactory fromJsonFile(final String jsonFile) throws IOException {
        SubjectAlternativeNameFactory psdaf = create();
        final String jsonData = new String(Files.readAllBytes(Paths.get(jsonFile)));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonData);
        if (root.has(Json.PLATFORM.name())) {
            JsonNode platformNode = root.get(Json.PLATFORM.name());
            psdaf.fromJsonNode(platformNode);
        }
        return psdaf;
    }
    
    /**
     * Parse the JSON objects for SAN data.
     * @param refNode JsonNode
     * @see SubjectAlternativeNameFactory.Json
     * @return The SubjectAlternativeNameFactory object with new components from the JSON data.
     */
    public final SubjectAlternativeNameFactory fromJsonNode(final JsonNode refNode) {
        if (refNode.has(ElementJson.PLATFORMMODEL.name()) && refNode.has(ElementJson.PLATFORMMANUFACTURERSTR.name()) && refNode.has(ElementJson.PLATFORMVERSION.name())) {
            JsonNode platformModelNode = refNode.get(ElementJson.PLATFORMMODEL.name());
            JsonNode platformManufacturerStrNode = refNode.get(ElementJson.PLATFORMMANUFACTURERSTR.name());
            JsonNode platformVersionNode = refNode.get(ElementJson.PLATFORMVERSION.name());
            JsonNode platformSerialNode = refNode.get(ElementJson.PLATFORMSERIAL.name());
            JsonNode platformManufacturerIdNode = refNode.get(ElementJson.PLATFORMMANUFACTURERID.name());
            
            // Required fields
            addRDN(ElementJson.PLATFORMMODEL.getOid(), new DERUTF8String(platformModelNode.asText()));
            addRDN(ElementJson.PLATFORMMANUFACTURERSTR.getOid(), new DERUTF8String(platformManufacturerStrNode.asText()));
            addRDN(ElementJson.PLATFORMVERSION.getOid(), new DERUTF8String(platformVersionNode.asText()));
            
            // Optional fields
            if (platformSerialNode != null) {
                addRDN(ElementJson.PLATFORMSERIAL.getOid(), new DERUTF8String(platformSerialNode.asText()));
            }
            if (platformManufacturerIdNode != null) {
                addRDN(new RDN(ElementJson.PLATFORMMANUFACTURERID.getOid(), new ASN1ObjectIdentifier(platformManufacturerIdNode.asText())));
            }
        }
        
        return this;
    }
}
