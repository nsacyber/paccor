package tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class TCGObjectIdentifier {
    // TCG specific OIDs
    public static final ASN1ObjectIdentifier tcg = new ASN1ObjectIdentifier("2.23.133").intern();
    public static final ASN1ObjectIdentifier tcgTcpaSpecVersion = tcg.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAttribute = tcg.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgProtocol = tcg.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgAlgorithm = tcg.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgPlatformClass = tcg.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgCe = tcg.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgKp = tcg.branch("8").intern();
    public static final ASN1ObjectIdentifier tcgAddress = tcg.branch("17").intern();
    public static final ASN1ObjectIdentifier tcgRegistry = tcg.branch("18").intern();
    
    // TCG Attribute OIDs
    public static final ASN1ObjectIdentifier tcgAtTpmManufacturer = tcgAttribute.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAtTpmModel = tcgAttribute.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgAtTpmVersion = tcgAttribute.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgAtSecurityQualities = tcgAttribute.branch("10").intern();
    public static final ASN1ObjectIdentifier tcgAtTpmProtectionProfile = tcgAttribute.branch("11").intern();
    public static final ASN1ObjectIdentifier tcgAtTpmSecurityTarget = tcgAttribute.branch("12").intern();
    public static final ASN1ObjectIdentifier tcgAtTbbProtectionProfile = tcgAttribute.branch("13").intern();
    public static final ASN1ObjectIdentifier tcgAtTbbSecurityTarget = tcgAttribute.branch("14").intern();
    public static final ASN1ObjectIdentifier tcgAtTpmIdLabel = tcgAttribute.branch("15").intern();
    public static final ASN1ObjectIdentifier tcgAtTpmSpecification = tcgAttribute.branch("16").intern();
    public static final ASN1ObjectIdentifier tcgAtTcgPlatformSpecification = tcgAttribute.branch("17").intern();
    public static final ASN1ObjectIdentifier tcgAtTpmSecurityAssertions = tcgAttribute.branch("18").intern();
    public static final ASN1ObjectIdentifier tcgAtTbbSecurityAssertions = tcgAttribute.branch("19").intern();
    /**
     * @deprecated Use tcgAtTcgCertificateSpecification.  This variable has not been removed
     * yet because it is still documented in the Platform Certificate specification.
     */
    public static final ASN1ObjectIdentifier tcgAtTcgCredentialSpecification = tcgAttribute.branch("23").intern();
    /**
     * @deprecated Use tcgAtTcgCertificateType.  This variable has not been removed
     * yet because it is still documented in the Platform Certificate specification.
     */
    public static final ASN1ObjectIdentifier tcgAtTcgCredentialType = tcgAttribute.branch("25").intern();
    // Use of the word Credential in the Platform Certificate specification has been deprecated.
    public static final ASN1ObjectIdentifier tcgAtTcgCertificateSpecification = tcgAttribute.branch("23").intern();
    public static final ASN1ObjectIdentifier tcgAtTcgCertificateType = tcgAttribute.branch("25").intern();
    
    // TCG Platform Class Common OIDs
    public static final ASN1ObjectIdentifier tcgCommon = tcgPlatformClass.branch("1").intern();
    
    // TCG Common Attribute OIDs
    public static final ASN1ObjectIdentifier tcgAtPlatformManufacturerStr = tcgCommon.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformManufacturerId = tcgCommon.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformConfigUri = tcgCommon.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformModel = tcgCommon.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformVersion = tcgCommon.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformSerial = tcgCommon.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformConfiguration = tcgCommon.branch("7").intern();
    
    // TCG Platform Configuration OIDs
    public static final ASN1ObjectIdentifier tcgAtPlatformConfigurationV1 = tcgAtPlatformConfiguration.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformConfigurationV2 = tcgAtPlatformConfiguration.branch("2").intern();
    
    // TCG Algorithm OIDs
    public static final ASN1ObjectIdentifier tcgAlgorithmNull = tcgAlgorithm.branch("1").intern();
    
    // TCG Key Purposes OIDs
    public static final ASN1ObjectIdentifier tcgKpEkCertificate = tcgKp.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgKpPlatformAttributeCertificate = tcgKp.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgKpAikCertificate = tcgKp.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgKpPlatformKeyCertificate = tcgKp.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgKpDeltaPlatformAttributeCertificate = tcgKp.branch("5").intern();
    
    // TCG Certificate Extensions
    public static final ASN1ObjectIdentifier tcgCeRelevantCredentials = tcgCe.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgCeRelevantManifests = tcgCe.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgCeVirtualPlatformAttestationService = tcgCe.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgCeMigrationControllerAttestationService = tcgCe.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgCeMigrationControllerRegistrationService = tcgCe.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgCeVirtualPlatformBackupService = tcgCe.branch("7").intern();
    
    // TCG Protocol OIDs
    public static final ASN1ObjectIdentifier tcgPrtTpmIdProtocol = tcgProtocol.branch("1").intern();
    
    // TCG Address OIDs
    public static final ASN1ObjectIdentifier tcgAddressEthernetMac = tcgAddress.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAddressWlanMac = tcgAddress.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgAddressBluetoothMac = tcgAddress.branch("3").intern();
    
    // TCG Registry OIDs
    public static final ASN1ObjectIdentifier tcgRegistryComponentClass = tcgRegistry.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassTcg = tcgRegistryComponentClass.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassIetf = tcgRegistryComponentClass.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassDmtf = tcgRegistryComponentClass.branch("3").intern();
}
