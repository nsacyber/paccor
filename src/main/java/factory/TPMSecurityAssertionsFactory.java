package factory;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERIA5String;
import tcg.credential.CommonCriteriaMeasures;
import tcg.credential.EKCertificateGenerationLocation;
import tcg.credential.EKGenerationLocation;
import tcg.credential.EKGenerationType;
import tcg.credential.FIPSLevel;
import tcg.credential.TBBSecurityAssertions;
import tcg.credential.TPMSecurityAssertions;

/**
 * Functions to help manage the creation of the TPM security assertions attribute.
 * Not clear if this is needed.
 * The profile does not list this attribute as a valid addition to a platform attribute certificate.
 */
public class TPMSecurityAssertionsFactory {
    private ASN1Integer version;
    private ASN1Boolean fieldUpgradable; // default false
    private EKGenerationType ekGenerationType; // optional, tagged 0
    private EKGenerationLocation ekGenerationLocation; // optional, tagged 1
    private EKCertificateGenerationLocation ekCertificateGenerationLocation; // optional, tagged 2
    private CommonCriteriaMeasures ccInfo; // optional, tagged 3
    private FIPSLevel fipsLevel; // optional, tagged 4
    private ASN1Boolean iso9000Certified; // default false, tagged 5
    private DERIA5String iso9000Uri; // optional

    private TPMSecurityAssertionsFactory() {
        version = new ASN1Integer(0);
        fieldUpgradable = ASN1Boolean.FALSE;
        ekGenerationType = null;
        ekGenerationLocation = null;
        ekCertificateGenerationLocation = null;
        ccInfo = null;
        fipsLevel = null;
        iso9000Certified = ASN1Boolean.FALSE;
        iso9000Uri = null;
    }
    
    /**
     * Begin defining the TPM security assertions attribute.
     * @return A new TPMSecurityAssertionsFactory builder.
     */
    public static final TPMSecurityAssertionsFactory create() {
        return new TPMSecurityAssertionsFactory();
    }
    
    /**
     * Required field.
     * @param version The version as an integer.
     * @return The TPMSecurityAssertionsFactory object with the version set.
     */
    public final TPMSecurityAssertionsFactory version(int version) {
        this.version = new ASN1Integer(version);
        return this;
    }
    
    /**
     * Required field. Defaults to false.
     * @param setting True or false.
     * @return The TPMSecurityAssertionsFactory object with the field upgradable bit set.
     */
    public final TPMSecurityAssertionsFactory fieldUpgradable(ASN1Boolean setting) {
        fieldUpgradable = setting;
        return this;
    }
    
    /**
     * Optional field.
     * @param type The EK Generation type.
     * @return The TPMSecurityAssertionsFactory object with the EK generation type set.
     */
    public final TPMSecurityAssertionsFactory ekGenerationType(final EKGenerationType type) {
        ekGenerationType = type;
        return this;
    }
    
    /**
     * Optional field.
     * @param location The EK Generation Location.
     * @return The TPMSecurityAssertionsFactory object with the EK generation location set.
     */
    public final TPMSecurityAssertionsFactory ekGenerationLocation(final EKGenerationLocation location) {
        ekGenerationLocation = location;
        return this;
    }
    
    /**
     * Optional field.
     * @param location The EK Certificate Generation Location.
     * @return The TPMSecurityAssertionsFactory object with the EK certificate generation location set.
     */
    public final TPMSecurityAssertionsFactory ekCertificateGenerationLocation(final EKCertificateGenerationLocation location) {
        ekCertificateGenerationLocation = location;
        return this;
    }
    
    /**
     * Optional field.
     * @param ccInfo The common criteria measures information.
     * @return The TPMSecurityAssertionsFactory object with the common criteria info block set.
     */
    public final TPMSecurityAssertionsFactory ccInfo(final CommonCriteriaMeasures ccInfo) {
        this.ccInfo = ccInfo;
        return this;
    }
    
    /**
     * Optional field.
     * @param level The FIPS information.
     * @return The TPMSecurityAssertionsFactory object with the FIPS information set.
     */
    public final TPMSecurityAssertionsFactory fipsLevel(final FIPSLevel level) {
        fipsLevel = level;
        return this;
    }
    
    /**
     * Required field.  Defaults to false.
     * @param certified True or false, based on if the TPM is ISO 9000 certified.
     * @return The TPMSecurityAssertionsFactory object with the ISO 9000 bit set.
     */
    public final TPMSecurityAssertionsFactory iso9000Certified(final ASN1Boolean certified) {
        iso9000Certified = certified;
        return this;
    }
    
    /**
     * Optional field.
     * @param uri The ISO 9000 URI.
     * @return The TPMSecurityAssertionsFactory object with the ISO 9000 URI set.
     */
    public final TPMSecurityAssertionsFactory iso9000Uri(final DERIA5String uri) {
        iso9000Uri = uri;
        return this;
    }
    
    /**
     * Parse the JSON objects for TPM security assertions.
     * @return The TPMSecurityAssertions object with new components from the JSON data.
     */
    public final TPMSecurityAssertions build() {
        TPMSecurityAssertions obj = new TPMSecurityAssertions(version, fieldUpgradable, ekGenerationType, ekGenerationLocation, ekCertificateGenerationLocation, ccInfo, fipsLevel, iso9000Certified, iso9000Uri);
        
        return obj;
    }
}
