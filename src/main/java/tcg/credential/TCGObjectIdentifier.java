package tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class TCGObjectIdentifier {
	/*
	 * Base OIDs -- used as references to build other OIDs, but not used as themselves
	public static final ASN1ObjectIdentifier tcg = new ASN1ObjectIdentifier("2.23.133").intern();
	public static final ASN1ObjectIdentifier tcgTcpaSpecVersion = new ASN1ObjectIdentifier("2.23.133.1").intern();
	public static final ASN1ObjectIdentifier tcgAttribute = new ASN1ObjectIdentifier("2.23.133.2").intern();
	public static final ASN1ObjectIdentifier tcgProtocol = new ASN1ObjectIdentifier("2.23.133.3").intern();
	public static final ASN1ObjectIdentifier tcgAlgorithm = new ASN1ObjectIdentifier("2.23.133.4").intern();
	public static final ASN1ObjectIdentifier tcgPlatformClass = new ASN1ObjectIdentifier("2.23.133.5").intern();
	public static final ASN1ObjectIdentifier tcgCe = new ASN1ObjectIdentifier("2.23.133.6").intern();
	public static final ASN1ObjectIdentifier tcgKp = new ASN1ObjectIdentifier("2.23.133.8").intern();
	public static final ASN1ObjectIdentifier tcgAddress = new ASN1ObjectIdentifier("2.23.133.17").intern();
	public static final ASN1ObjectIdentifier tcgCommon = new ASN1ObjectIdentifier("2.23.133.5.1").intern();
	*/
	public static final ASN1ObjectIdentifier tcgAtTpmManufacturer = new ASN1ObjectIdentifier("2.23.133.2.1").intern();
	public static final ASN1ObjectIdentifier tcgAtTpmModel = new ASN1ObjectIdentifier("2.23.133.2.2").intern();
	public static final ASN1ObjectIdentifier tcgAtTpmVersion = new ASN1ObjectIdentifier("2.23.133.2.3").intern();
	public static final ASN1ObjectIdentifier tcgAtSecurityQualities = new ASN1ObjectIdentifier("2.23.133.2.10").intern();
	public static final ASN1ObjectIdentifier tcgAtTpmProtectionProfile = new ASN1ObjectIdentifier("2.23.133.2.11").intern();
	public static final ASN1ObjectIdentifier tcgAtTpmSecurityTarget = new ASN1ObjectIdentifier("2.23.133.2.12").intern();
	public static final ASN1ObjectIdentifier tcgAtTbbProtectionProfile = new ASN1ObjectIdentifier("2.23.133.2.13").intern();
	public static final ASN1ObjectIdentifier tcgAtTbbSecurityTarget = new ASN1ObjectIdentifier("2.23.133.2.14").intern();
	public static final ASN1ObjectIdentifier tcgAtTpmIdLabel = new ASN1ObjectIdentifier("2.23.133.2.15").intern();
	public static final ASN1ObjectIdentifier tcgAtTpmSpecification = new ASN1ObjectIdentifier("2.23.133.2.16").intern();
	public static final ASN1ObjectIdentifier tcgAtTcgPlatformSpecification = new ASN1ObjectIdentifier("2.23.133.2.17").intern();
	public static final ASN1ObjectIdentifier tcgAtTpmSecurityAssertions = new ASN1ObjectIdentifier("2.23.133.2.18").intern();
	public static final ASN1ObjectIdentifier tcgAtTbbSecurityAssersions = new ASN1ObjectIdentifier("2.23.133.2.19").intern();
	/**
	 * @deprecated Use tcgAtTcgCertificateSpecification
	 */
	public static final ASN1ObjectIdentifier tcgAtTcgCredentialSpecification = new ASN1ObjectIdentifier("2.23.133.2.23").intern();
	public static final ASN1ObjectIdentifier tcgAtTcgCertificateSpecification = new ASN1ObjectIdentifier("2.23.133.2.23").intern();
	public static final ASN1ObjectIdentifier tcgAtTcgCredentialType = new ASN1ObjectIdentifier("2.23.133.2.25").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformManufacturerStr = new ASN1ObjectIdentifier("2.23.133.5.1.1").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformManufacturerId = new ASN1ObjectIdentifier("2.23.133.5.1.2").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformConfigUri = new ASN1ObjectIdentifier("2.23.133.5.1.3").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformModel = new ASN1ObjectIdentifier("2.23.133.5.1.4").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformVersion = new ASN1ObjectIdentifier("2.23.133.5.1.5").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformSerial = new ASN1ObjectIdentifier("2.23.133.5.1.6").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformConfiguration = new ASN1ObjectIdentifier("2.23.133.5.1.7").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformConfigurationV1 = new ASN1ObjectIdentifier("2.23.133.5.1.7.1").intern();
	public static final ASN1ObjectIdentifier tcgAtPlatformConfigurationV2 = new ASN1ObjectIdentifier("2.23.133.5.1.7.2").intern();
	public static final ASN1ObjectIdentifier tcgAlgorithmNull = new ASN1ObjectIdentifier("2.23.133.4.1").intern();
	public static final ASN1ObjectIdentifier tcgKpEkCertificate = new ASN1ObjectIdentifier("2.23.133.8.1").intern();
	public static final ASN1ObjectIdentifier tcgKpPlatformAttributeCertificate = new ASN1ObjectIdentifier("2.23.133.8.2").intern();
	public static final ASN1ObjectIdentifier tcgKpAikCertificate = new ASN1ObjectIdentifier("2.23.133.8.3").intern();
	public static final ASN1ObjectIdentifier tcgKpPlatformKeyCertificate = new ASN1ObjectIdentifier("2.23.133.8.4").intern();
	public static final ASN1ObjectIdentifier tcgKpDeltaPlatformAttributeCertificate = new ASN1ObjectIdentifier("2.23.133.8.5").intern();
	public static final ASN1ObjectIdentifier tcgCeRelevantCredentials = new ASN1ObjectIdentifier("2.23.133.6.2").intern();
	public static final ASN1ObjectIdentifier tcgCeRelevantManifests = new ASN1ObjectIdentifier("2.23.133.6.3").intern();
	public static final ASN1ObjectIdentifier tcgCeVirtualPlatformAttestationService = new ASN1ObjectIdentifier("2.23.133.6.4").intern();
	public static final ASN1ObjectIdentifier tcgCeMigrationControllerAttestationService = new ASN1ObjectIdentifier("2.23.133.6.5").intern();
	public static final ASN1ObjectIdentifier tcgCeMigrationControllerRegistrationService = new ASN1ObjectIdentifier("2.23.133.6.6").intern();
	public static final ASN1ObjectIdentifier tcgCeVirtualPlatformBackupService = new ASN1ObjectIdentifier("2.23.133.6.7").intern();
	public static final ASN1ObjectIdentifier tcgPrtTpmIdProtocol = new ASN1ObjectIdentifier("2.23.133.3.1").intern();
	public static final ASN1ObjectIdentifier tcgAddressEthernetMac = new ASN1ObjectIdentifier("2.23.133.17.1").intern();
	public static final ASN1ObjectIdentifier tcgAddressWlanMac = new ASN1ObjectIdentifier("2.23.133.17.2").intern();
	public static final ASN1ObjectIdentifier tcgAddressBluetoothMac = new ASN1ObjectIdentifier("2.23.133.17.3").intern();
	public static final ASN1ObjectIdentifier tcgRegistryComponentClass = new ASN1ObjectIdentifier("2.23.133.18.3").intern();
	public static final ASN1ObjectIdentifier tcgRegistryComponentClassTcg = new ASN1ObjectIdentifier("2.23.133.18.3.1").intern();
	public static final ASN1ObjectIdentifier tcgRegistryComponentClassIetf = new ASN1ObjectIdentifier("2.23.133.18.3.2").intern();
	public static final ASN1ObjectIdentifier tcgRegistryComponentClassDmtf = new ASN1ObjectIdentifier("2.23.133.18.3.3").intern();
}
