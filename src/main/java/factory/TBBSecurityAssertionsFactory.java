package factory;

import com.fasterxml.jackson.databind.JsonNode;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERIA5String;
import tcg.credential.CommonCriteriaMeasures;
import tcg.credential.FIPSLevel;
import tcg.credential.MeasurementRootType;
import tcg.credential.SecurityLevel;
import tcg.credential.TBBSecurityAssertions;

/**
 * Functions to help manage the creation of the TBB security assertions attributes.
 */
public class TBBSecurityAssertionsFactory {
    /**
     * fields of the TBB security assertions JSON object
     */
    public enum Json {
        VERSION,
        CCINFO,
        FIPSLEVEL,
        RTMTYPE,
        ISO9000CERTIFIED,
        ISO9000URI;
    }
    
    /**
     * FIPS options
     */
    public enum FipsJson {
        VERSION,
        LEVEL,
        PLUS;
    }
    
    private ASN1Integer version = new ASN1Integer(1); // default = 1
    private CommonCriteriaMeasures ccInfo = null; // optional, tagged 0
    private FIPSLevel fipsLevel = null; // optional, tagged 1
    private MeasurementRootType rtmType = null; // optional, tagged 2
    private ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default = false
    private DERIA5String iso9000Uri = null; // optional
    
    private TBBSecurityAssertionsFactory() {
        version = new ASN1Integer(1);
        ccInfo = null;
        fipsLevel = null;
        rtmType = null;
        iso9000Certified = ASN1Boolean.FALSE;
        iso9000Uri = null;
    }
    
    /**
     * Begin defining the TBB security assertions attribute.
     */
    public static final TBBSecurityAssertionsFactory create() {
        return new TBBSecurityAssertionsFactory();
    }
    
    /**
     * Set the version. Required field.  Defaults to 1.
     * @param version {@link ASN1Integer}
     */
    public final TBBSecurityAssertionsFactory version(final ASN1Integer version) {
        this.version = version;
        return this;
    }
    
    /**
     * Set the common criteria measures field. Optional field.
     * @param ccInfo {@link CommonCriteriaMeasures}
     * @see CommonCriteriaMeasuresFactory
     */
    public final TBBSecurityAssertionsFactory ccInfo(final CommonCriteriaMeasures ccInfo) {
        this.ccInfo = ccInfo;
        return this;
    }
    
    /**
     * Set the FIPS level. Optional field.
     * @param level {@link FIPSLevel}
     */
    public final TBBSecurityAssertionsFactory fipsLevel(final FIPSLevel level) {
        fipsLevel = level;
        return this;
    }
    
    /**
     * Set the measurement root type. Optional field.
     * @param type {@link MeasurementRootType}
     */
    public final TBBSecurityAssertionsFactory rtmType(final MeasurementRootType type) {
        rtmType = type;
        return this;
    }
    
    /**
     * Set the ISO9000 certified flag. Required field.  Defaults to false.
     * @param certified {@link ASN1Boolean}
     */
    public final TBBSecurityAssertionsFactory iso9000Certified(final ASN1Boolean certified) {
        iso9000Certified = certified;
        return this;
    }
    
    /**
     * Set the ISO9000 URI. Optional field.
     * This field is defined as a string in the profile.  It is not a URIReference.
     * @param uri {@link DERIA5String}
     */
    public final TBBSecurityAssertionsFactory iso9000Uri(final DERIA5String uri) {
        iso9000Uri = uri;
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link TBBSecurityAssertions}
     */
    public final TBBSecurityAssertions build() {
        TBBSecurityAssertions obj = new TBBSecurityAssertions(version, ccInfo, fipsLevel, rtmType, iso9000Certified, iso9000Uri);
        
        return obj;
    }
    
    /**
     * Parse the JSON objects for TBB security assertions.
     * @param refNode JsonNode with {@link TBBSecurityAssertions} data
     * @see TBBSecurityAssertionsFactory.Json
     */
    public final TBBSecurityAssertionsFactory fromJsonNode(final JsonNode refNode) {
        if (refNode.has(Json.VERSION.name()) && refNode.has(Json.ISO9000CERTIFIED.name())) {
            JsonNode versionNode = refNode.get(Json.VERSION.name());
            JsonNode ccInfoNode = refNode.get(Json.CCINFO.name());
            JsonNode fipsLevelNode = refNode.get(Json.FIPSLEVEL.name());
            JsonNode rtmTypeNode = refNode.get(Json.RTMTYPE.name());
            JsonNode iso9000CertifiedNode = refNode.get(Json.ISO9000CERTIFIED.name());
            JsonNode iso9000UriNode = refNode.get(Json.ISO9000URI.name());
            
            // required field
            int version = 1;
            if (versionNode != null) {
                version = versionNode.asInt();
            }
            version(new ASN1Integer(version));
            
            if (ccInfoNode != null) {
                CommonCriteriaMeasuresFactory ccmf = CommonCriteriaMeasuresFactory.fromJsonNode(ccInfoNode);
                ccInfo(ccmf.build());
            }
            
            if (fipsLevelNode != null) {
                if (fipsLevelNode.has(FipsJson.VERSION.name()) && fipsLevelNode.has(FipsJson.LEVEL.name())) {
                    JsonNode fipsVersionNode = fipsLevelNode.get(FipsJson.VERSION.name());
                    JsonNode levelNode = fipsLevelNode.get(FipsJson.LEVEL.name());
                    JsonNode fipsPlusNode = fipsLevelNode.get(FipsJson.PLUS.name());
                    boolean fipsPlus = false;
                    if (fipsPlusNode != null) {
                        if (fipsPlusNode.isBoolean()) {
                            fipsPlus = fipsPlusNode.asBoolean();
                        } else if (iso9000CertifiedNode.isInt()) {
                            fipsPlus = fipsPlusNode.asInt() != 0;   
                        }
                    }
                    final String fipsVersion = fipsVersionNode.asText();
                    fipsLevel(new FIPSLevel(new DERIA5String(fipsVersion), new SecurityLevel(levelNode.asText()), ASN1Boolean.getInstance(fipsPlus)));
                }
            }
            
            if (rtmTypeNode != null) {
                rtmType(new MeasurementRootType(rtmTypeNode.asText()));
            }
            
            // required field, which has a default value
            boolean iso9000Certified = false;
            if (iso9000CertifiedNode != null) {
                if (iso9000CertifiedNode.isBoolean()) {
                    iso9000Certified = iso9000CertifiedNode.asBoolean();
                } else if (iso9000CertifiedNode.isInt()) {
                    iso9000Certified = iso9000CertifiedNode.asInt() != 0;   
                }
            }
            iso9000Certified(ASN1Boolean.getInstance(iso9000Certified));
            
            if (iso9000UriNode != null) {
                iso9000Uri(new DERIA5String(iso9000UriNode.asText()));
            }
        }
        
        return this;
    }
}
