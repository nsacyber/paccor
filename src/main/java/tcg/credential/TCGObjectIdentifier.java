package tcg.credential;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class TCGObjectIdentifier {
    // TCG Root OID
    public static final ASN1ObjectIdentifier tcg = new ASN1ObjectIdentifier("2.23.133").intern();

    // TCG OIDs
    public static final ASN1ObjectIdentifier tcgTcpaSpecVersion = tcg.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAttribute = tcg.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgProtocol = tcg.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgAlgorithm = tcg.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgPlatformClass = tcg.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgCe = tcg.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgKp = tcg.branch("8").intern();
    public static final ASN1ObjectIdentifier tcgTpma = tcg.branch("10").intern();
    public static final ASN1ObjectIdentifier tcgCa = tcg.branch("11").intern();
    public static final ASN1ObjectIdentifier tcgTnc = tcg.branch("16").intern();
    public static final ASN1ObjectIdentifier tcgAddress = tcg.branch("17").intern();
    public static final ASN1ObjectIdentifier tcgRegistry = tcg.branch("18").intern();
    public static final ASN1ObjectIdentifier tcgTraits = tcg.branch("19").intern();

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
    public static final ASN1ObjectIdentifier tcgAtTcgCredentialSpecification = tcgAttribute.branch("23").intern();
    public static final ASN1ObjectIdentifier tcgAtTcgCredentialType = tcgAttribute.branch("25").intern();
    public static final ASN1ObjectIdentifier tcgAtPreviousPlatformCertificates = tcgAttribute.branch("26").intern();
    public static final ASN1ObjectIdentifier tcgAtTbbSecurityAssertionsV3 = tcgAttribute.branch("27").intern();
    public static final ASN1ObjectIdentifier tcgAtCryptographicAnchors = tcgAttribute.branch("28").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformOwnership = tcgAttribute.branch("29").intern();
    public static final ASN1ObjectIdentifier tcgAtManufacturingAssertions = tcgAttribute.branch("30").intern();

    // TCG Protocol OIDs
    public static final ASN1ObjectIdentifier tcgPrtTpmIdProtocol = tcgProtocol.branch("1").intern();

    // TCG Algorithm OIDs
    public static final ASN1ObjectIdentifier tcgAlgorithmNull = tcgAlgorithm.branch("1").intern();

    // TCG Platform Class Common OIDs
    public static final ASN1ObjectIdentifier tcgCommon = tcgPlatformClass.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgDice = tcgPlatformClass.branch("4").intern();

    // TCG Common Attribute OIDs
    public static final ASN1ObjectIdentifier tcgAtPlatformManufacturerStr = tcgCommon.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformManufacturerId = tcgCommon.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformConfigUri = tcgCommon.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformModel = tcgCommon.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformVersion = tcgCommon.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformSerial = tcgCommon.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformConfiguration = tcgCommon.branch("7").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformIdentifier = tcgCommon.branch("8").intern();

    // TCG Platform Configuration OIDs
    public static final ASN1ObjectIdentifier tcgAtPlatformConfigurationV1 = tcgAtPlatformConfiguration.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformConfigurationV2 = tcgAtPlatformConfiguration.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgAtPlatformConfigurationV3 = tcgAtPlatformConfiguration.branch("3").intern();

    // TCG DICE OIDs
    public static final ASN1ObjectIdentifier tcgDiceTcbInfo = tcgDice.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgDiceEndorsementManifest = tcgDice.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgDiceEndorsementManifestUri = tcgDice.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgDiceUeid = tcgDice.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgDiceMultiTcbInfo = tcgDice.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgDiceUccsEvidence = tcgDice.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgDiceManifestEvidence = tcgDice.branch("7").intern();
    public static final ASN1ObjectIdentifier tcgDiceKp = tcgDice.branch("100").intern();

    // TCG DICE Key Purpose OIDs
    public static final ASN1ObjectIdentifier tcgDiceKpIdentityInit = tcgDiceKp.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgDiceKpIdentityLoc = tcgDiceKp.branch("7").intern();
    public static final ASN1ObjectIdentifier tcgDiceKpAttestInit = tcgDiceKp.branch("8").intern();
    public static final ASN1ObjectIdentifier tcgDiceKpAttestLoc = tcgDiceKp.branch("9").intern();
    public static final ASN1ObjectIdentifier tcgDiceKpAssertInit = tcgDiceKp.branch("10").intern();
    public static final ASN1ObjectIdentifier tcgDiceKpAssertLoc = tcgDiceKp.branch("11").intern();
    public static final ASN1ObjectIdentifier tcgDiceKpEca = tcgDiceKp.branch("12").intern();

    // TCG Certificate Extension OIDs
    public static final ASN1ObjectIdentifier tcgCeSkae = tcgCe.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgCeRelevantCredentials = tcgCe.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgCeRelevantManifests = tcgCe.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgCeVirtualPlatformAttestationService = tcgCe.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgCeMigrationControllerAttestationService = tcgCe.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgCeMigrationControllerRegistrationService = tcgCe.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgCeVirtualPlatformBackupService = tcgCe.branch("7").intern();

    // TCG Subject Key Attestation Evidence OIDs
    public static final ASN1ObjectIdentifier tcgCeSkaeSubjectKeyAttestationEvidence = tcgCeSkae.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgCeSkaeCaVerifiedTpmKey = tcgCeSkae.branch("2").intern();

    // TCG Key Purpose OIDs
    public static final ASN1ObjectIdentifier tcgKpEkCertificate = tcgKp.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgKpPlatformAttributeCertificate = tcgKp.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgKpAikCertificate = tcgKp.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgKpPlatformKeyCertificate = tcgKp.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgKpDeltaPlatformAttributeCertificate = tcgKp.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgKpDeltaPlatformKeyCertificate = tcgKp.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgKpAdditionalPlatformAttributeCertificate = tcgKp.branch("7").intern();
    public static final ASN1ObjectIdentifier tcgKpAdditionalPlatformKeyCertificate = tcgKp.branch("8").intern();

    // TCG TPMA OIDs
    public static final ASN1ObjectIdentifier tcgTpmaX509 = tcgTpma.branch("1").intern();

    // TCG TPMA X509 OIDs
    public static final ASN1ObjectIdentifier tcgTpmaX509Key = tcgTpmaX509.branch("1").intern();

    // TCG TPMA X509 Key OIDs
    public static final ASN1ObjectIdentifier tcgTpmaX509KeyUsage = tcgTpmaX509Key.branch("1").intern();

    // TCG Certificate Authority OIDs
    public static final ASN1ObjectIdentifier tcgCaPolicy = tcgCa.branch("1").intern();

    // TCG CA Policy OIDs
    public static final ASN1ObjectIdentifier tcgCapVerifiedTPMResidency = tcgCaPolicy.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgCapVerifiedTPMFixed = tcgCaPolicy.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgCapVerifiedTPMRestricted = tcgCaPolicy.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgCapVerifiedPlatformCertificate = tcgCaPolicy.branch("4").intern();

    // TCG Trusted Network Connect OIDs
    public static final ASN1ObjectIdentifier tcgTncIfm = tcgTnc.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgTncIfftnc = tcgTnc.branch("2").intern();

    // TCG TNC IF-M OIDs
    public static final ASN1ObjectIdentifier tcgTncIfmCms = tcgTncIfm.branch("1").intern();

    // TCG IFM CMS OIDs
    public static final ASN1ObjectIdentifier tcgTncIfmCmsIfmSecurityCapabilities = tcgTncIfmCms.branch("1").intern();

    // TCG TNC Federated TNC OIDs
    public static final ASN1ObjectIdentifier tcgTncIfftncPosture = tcgTncIfftnc.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgTncIfftncEiu = tcgTncIfftnc.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgTncIfftncEndpoint = tcgTncIfftnc.branch("3").intern();

    // TCG TNC Endpoint Identity OIDs
    public static final ASN1ObjectIdentifier tcgTncIfftncEiuNameIdentifier = tcgTncIfftncEiu.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgTncIfftncEiuSamlAaEntityId = tcgTncIfftncEiu.branch("2").intern();

    // TCG TNC Endpoint Attribute OIDs
    public static final ASN1ObjectIdentifier tcgTncIfftncEndpointFtncServiceEndpoint = tcgTncIfftncEndpoint.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgTncIfftncEndpointOldestReceivedInformationAge = tcgTncIfftncEndpoint.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgTncIfftncEndpointIftnccsAccessRecommendation = tcgTncIfftncEndpoint.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgTncIfftncEndpointIfmapResultFilters = tcgTncIfftncEndpoint.branch("4").intern();

    // TCG Address OIDs
    public static final ASN1ObjectIdentifier tcgAddressEthernetMac = tcgAddress.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgAddressWlanMac = tcgAddress.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgAddressBluetoothMac = tcgAddress.branch("3").intern();

    // TCG Registry OIDs
    public static final ASN1ObjectIdentifier tcgRegistryPlatformClass = tcgRegistry.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgRegistryVendorId = tcgRegistry.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClass = tcgRegistry.branch("3").intern();

    // TCG Platform Class Registry OIDs
    public static final ASN1ObjectIdentifier tcgRegistryPlatformClassTcg = tcgRegistryPlatformClass.branch("1").intern();

    // TCG Vendor Id Registry OIDs
    public static final ASN1ObjectIdentifier tcgRegistryVendorIdTcg = tcgRegistryVendorId.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgRegistryVendorIdIana = tcgRegistryVendorId.branch("2").intern();

    // TCG Component Class Registry OIDs
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassTcg = tcgRegistryComponentClass.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassIetf = tcgRegistryComponentClass.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassDmtf = tcgRegistryComponentClass.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassPcie = tcgRegistryComponentClass.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgRegistryComponentClassDisk = tcgRegistryComponentClass.branch("5").intern();

    // TCG Trait OIDs
    public static final ASN1ObjectIdentifier tcgTrId = tcgTraits.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgTrCategory = tcgTraits.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgTrRegistry = tcgTraits.branch("3").intern();

    // TCG Trait ID OIDs
    public static final ASN1ObjectIdentifier tcgTrIdBoolean = tcgTrId.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgTrIdCertificateIdentifier = tcgTrId.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgTrIdCommonCriteria = tcgTrId.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgTrIdComponentClass = tcgTrId.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgTrIdComponentIdentifierV11 = tcgTrId.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgTrIdFipsLevel = tcgTrId.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgTrIdIso9000Level = tcgTrId.branch("7").intern();
    public static final ASN1ObjectIdentifier tcgTrIdNetworkMac = tcgTrId.branch("8").intern();
    public static final ASN1ObjectIdentifier tcgTrIdOid = tcgTrId.branch("9").intern();
    public static final ASN1ObjectIdentifier tcgTrIdPen = tcgTrId.branch("10").intern();
    public static final ASN1ObjectIdentifier tcgTrIdPlatformFirmwareCapabilities = tcgTrId.branch("11").intern();
    public static final ASN1ObjectIdentifier tcgTrIdPlatformFirmwareSignatureVerification = tcgTrId.branch("12").intern();
    public static final ASN1ObjectIdentifier tcgTrIdPlatformFirmwareUpdateCompliance = tcgTrId.branch("13").intern();
    public static final ASN1ObjectIdentifier tcgTrIdPlatformHardwareCapabilities = tcgTrId.branch("14").intern();
    public static final ASN1ObjectIdentifier tcgTrIdRtm = tcgTrId.branch("15").intern();
    public static final ASN1ObjectIdentifier tcgTrIdStatus = tcgTrId.branch("16").intern();
    public static final ASN1ObjectIdentifier tcgTrIdUri = tcgTrId.branch("17").intern();
    public static final ASN1ObjectIdentifier tcgTrIdUtf8String = tcgTrId.branch("18").intern();
    public static final ASN1ObjectIdentifier tcgTrIdIa5String = tcgTrId.branch("19").intern();
    public static final ASN1ObjectIdentifier tcgTrIdPemCertString = tcgTrId.branch("20").intern();
    public static final ASN1ObjectIdentifier tcgTrIdPublicKey = tcgTrId.branch("21").intern();
    public static final ASN1ObjectIdentifier tcgTrIdEntGeoLocation = tcgTrId.branch("22").intern();
    public static final ASN1ObjectIdentifier tcgTrIdCountryOfOrigin = tcgTrId.branch("23").intern();

    // TCG Trait Category OIDs
    public static final ASN1ObjectIdentifier tcgTrCatPlatformManufacturer = tcgTrCategory.branch("1").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformModel = tcgTrCategory.branch("2").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformVersion = tcgTrCategory.branch("3").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformSerial = tcgTrCategory.branch("4").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformManufactureridentifier = tcgTrCategory.branch("5").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformOwnership = tcgTrCategory.branch("6").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentClass = tcgTrCategory.branch("7").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentManufacturer = tcgTrCategory.branch("8").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentModel = tcgTrCategory.branch("9").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentSerial = tcgTrCategory.branch("10").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentStatus = tcgTrCategory.branch("11").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentLocation = tcgTrCategory.branch("12").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentRevision = tcgTrCategory.branch("13").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentFieldReplaceable = tcgTrCategory.branch("14").intern();
    public static final ASN1ObjectIdentifier tcgTrCatEkCertificate = tcgTrCategory.branch("15").intern();
    public static final ASN1ObjectIdentifier tcgTrCatIakCertificate = tcgTrCategory.branch("16").intern();
    public static final ASN1ObjectIdentifier tcgTrCatIdevidCertificate = tcgTrCategory.branch("17").intern();
    public static final ASN1ObjectIdentifier tcgTrCatDiceCertificate = tcgTrCategory.branch("18").intern();
    public static final ASN1ObjectIdentifier tcgTrCatSpdmCertificate = tcgTrCategory.branch("19").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPemCertificate = tcgTrCategory.branch("20").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformCertificate = tcgTrCategory.branch("21").intern();
    public static final ASN1ObjectIdentifier tcgTrCatDeltaPlatformCertificate = tcgTrCategory.branch("22").intern();
    public static final ASN1ObjectIdentifier tcgTrCatRebasePlatformCertificate = tcgTrCategory.branch("23").intern();
    public static final ASN1ObjectIdentifier tcgTrCatGenericCertificate = tcgTrCategory.branch("24").intern();
    public static final ASN1ObjectIdentifier tcgTrCatCommonCriteria = tcgTrCategory.branch("25").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentIdentifierV11 = tcgTrCategory.branch("26").intern();
    public static final ASN1ObjectIdentifier tcgTrCatFipsLevel = tcgTrCategory.branch("27").intern();
    public static final ASN1ObjectIdentifier tcgTrCatIso9000 = tcgTrCategory.branch("28").intern();
    public static final ASN1ObjectIdentifier tcgTrCatNetworkMac = tcgTrCategory.branch("29").intern();
    public static final ASN1ObjectIdentifier tcgTrCatOid = tcgTrCategory.branch("30").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPen = tcgTrCategory.branch("31").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformFirmwareCapabilities = tcgTrCategory.branch("32").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformHardwareCapabilities = tcgTrCategory.branch("33").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformFirmwareSignatureVerification = tcgTrCategory.branch("34").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPlatformFirmwareUpdateCompliance = tcgTrCategory.branch("35").intern();
    public static final ASN1ObjectIdentifier tcgTrCatRtm = tcgTrCategory.branch("36").intern();
    public static final ASN1ObjectIdentifier tcgTrCatPublicKey = tcgTrCategory.branch("37").intern();
    public static final ASN1ObjectIdentifier tcgTrCatComponentPartNumber = tcgTrCategory.branch("38").intern();
    public static final ASN1ObjectIdentifier tcgTrCatEntGeoLocation = tcgTrCategory.branch("39").intern();
    public static final ASN1ObjectIdentifier tcgTrCatCountryOfOrigin = tcgTrCategory.branch("40").intern();

    // TCG Trait Registry OIDs
    public static final ASN1ObjectIdentifier tcgTrRegNone = tcgTrRegistry.branch("1").intern();

    // TCG SNMP/MIB OIDs  Look to put them into a tree in the future
    public static final ASN1ObjectIdentifier tcgTpmQuoteMibVerBase = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.1.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteMibVersion = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.1.2").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteMibGeneralVersionInfo = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.1.3").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLockHolderIpAddrType = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.2.1.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLockHolderIpAddress = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.2.1.2").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLockNotification = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.2.2.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.3.1.1.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmClass = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.3.1.1.2").intern();
    public static final ASN1ObjectIdentifier tcgTpmSpecRev = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.3.1.1.3").intern();
    public static final ASN1ObjectIdentifier tcgTpmSelectorDescription = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.3.1.1.4").intern();
    public static final ASN1ObjectIdentifier tcgTpmFirmwareVersion = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.3.1.1.5").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLockTpmSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.4.1.1.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteTpmLockVal = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.4.1.1.2").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLockTimeout = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.4.1.1.3").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteCertTpmSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.1.1.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteCertChainIndex = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.1.1.2").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteCertType = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.1.1.3").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteCertFragmentIndex = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.1.1.4").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteCertBuf = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.1.1.5").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteCertStatus = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.1.1.6").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteTpmSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLockValue = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.2").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteCertSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.3").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteReqType = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.4").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteNonce = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.5").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteDigestSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.6").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuotePcrSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.7").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuotePCRDigestAlg = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.8").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteRespType = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.9").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteQualifiedSigner = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.10").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteClockInfo = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.11").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteFirmwareVersion = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.12").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuote = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.13").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLogFileLines = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.14").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuotePCRDigest = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.15").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteRowStatus = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.2.1.16").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLogTpmSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.3.1.1").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLogSelector = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.3.1.2").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLogLineNumber = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.3.1.3").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLogFragmentIndex = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.3.1.4").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLogLineBuf = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.3.1.5").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteLogStatus = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.5.3.1.6").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteNotificationGroup = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.6").intern();
    public static final ASN1ObjectIdentifier tcgTpmSelectionGroup = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.7").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteObjectGroup = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.8").intern();
    public static final ASN1ObjectIdentifier tcgTpmQuoteMibVersionGroup = new ASN1ObjectIdentifier("1.3.6.1.4.1.21911.1.1.9").intern();
}
