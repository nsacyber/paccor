package factory;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERIA5String;
import tcg.credential.CommonCriteriaMeasures;
import tcg.credential.EKCertificateGenerationLocation;
import tcg.credential.EKGenerationLocation;
import tcg.credential.EKGenerationType;
import tcg.credential.FIPSLevel;
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
    
    public static final TPMSecurityAssertionsFactory create() {
        return new TPMSecurityAssertionsFactory();
    }
    
    /**
     * Required field.
     * @param version
     * @return
     */
    public final TPMSecurityAssertionsFactory version(int version) {
        this.version = new ASN1Integer(version);
        return this;
    }
    
    /**
     * Required field. Defaults to false.
     * @param setting
     * @return
     */
    public final TPMSecurityAssertionsFactory fieldUpgradable(ASN1Boolean setting) {
        fieldUpgradable = setting;
        return this;
    }
    
    /**
     * Optional field.
     * @param type
     * @return
     */
    public final TPMSecurityAssertionsFactory ekGenerationType(final EKGenerationType type) {
        ekGenerationType = type;
        return this;
    }
    
    /**
     * Optional field.
     * @param location
     * @return
     */
    public final TPMSecurityAssertionsFactory ekGenerationLocation(final EKGenerationLocation location) {
        ekGenerationLocation = location;
        return this;
    }
    
    /**
     * Optional field.
     * @param location
     * @return
     */
    public final TPMSecurityAssertionsFactory ekCertificateGenerationLocation(final EKCertificateGenerationLocation location) {
        ekCertificateGenerationLocation = location;
        return this;
    }
    
    /**
     * Optional field.
     * @param ccInfo
     * @return
     */
    public final TPMSecurityAssertionsFactory ccInfo(final CommonCriteriaMeasures ccInfo) {
        this.ccInfo = ccInfo;
        return this;
    }
    
    /**
     * Optional field.
     * @param level
     * @return
     */
    public final TPMSecurityAssertionsFactory fipsLevel(final FIPSLevel level) {
        fipsLevel = level;
        return this;
    }
    
    /**
     * Required field.  Defaults to false.
     * @param certified
     * @return
     */
    public final TPMSecurityAssertionsFactory iso9000Certified(final ASN1Boolean certified) {
        iso9000Certified = certified;
        return this;
    }
    
    /**
     * Optional field.
     * @param uri
     * @return
     */
    public final TPMSecurityAssertionsFactory iso9000Uri(final DERIA5String uri) {
        iso9000Uri = uri;
        return this;
    }
    
    public final TPMSecurityAssertions build() {
        TPMSecurityAssertions obj = new TPMSecurityAssertions(version, fieldUpgradable, ekGenerationType, ekGenerationLocation, ekCertificateGenerationLocation, ccInfo, fipsLevel, iso9000Certified, iso9000Uri);
        
        return obj;
    }
}
