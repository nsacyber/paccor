package paccor.tcg.credential;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class TraitId {
    /**
     * Trait classes indexed by Trait ID OID
     */
    private static final Map<ASN1ObjectIdentifier, Class<? extends Trait<?,?>>> ID_REGISTRY = new HashMap<>() {{
        put(TCGObjectIdentifier.tcgTrIdBoolean, BooleanTrait.class);
        put(TCGObjectIdentifier.tcgTrIdCertificateIdentifier, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrIdCommonCriteria, CommonCriteriaTrait.class);
        put(TCGObjectIdentifier.tcgTrIdComponentClass, ComponentClassTrait.class);
        put(TCGObjectIdentifier.tcgTrIdComponentIdentifierV11, ComponentIdentifierV11Trait.class);
        put(TCGObjectIdentifier.tcgTrIdFipsLevel, FIPSLevelTrait.class);
        put(TCGObjectIdentifier.tcgTrIdIso9000Level, ISO9000Trait.class);
        put(TCGObjectIdentifier.tcgTrIdNetworkMac, NetworkMACTrait.class);
        put(TCGObjectIdentifier.tcgTrIdOid, OIDTrait.class);
        put(TCGObjectIdentifier.tcgTrIdPen, PENTrait.class);
        put(TCGObjectIdentifier.tcgTrIdPlatformFirmwareCapabilities, PlatformFirmwareCapabilitiesTrait.class);
        put(TCGObjectIdentifier.tcgTrIdPlatformFirmwareSignatureVerification, PlatformFirmwareSignatureVerificationTrait.class);
        put(TCGObjectIdentifier.tcgTrIdPlatformFirmwareUpdateCompliance, PlatformFirmwareUpdateComplianceTrait.class);
        put(TCGObjectIdentifier.tcgTrIdPlatformHardwareCapabilities, PlatformHardwareCapabilitiesTrait.class);
        put(TCGObjectIdentifier.tcgTrIdRtm, RTMTrait.class);
        put(TCGObjectIdentifier.tcgTrIdStatus, StatusTrait.class);
        put(TCGObjectIdentifier.tcgTrIdUri, URITrait.class);
        put(TCGObjectIdentifier.tcgTrIdUtf8String, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrIdIa5String, IA5StringTrait.class);
        put(TCGObjectIdentifier.tcgTrIdPemCertString, PEMCertStringTrait.class);
        put(TCGObjectIdentifier.tcgTrIdPublicKey, PublicKeyTrait.class);
        put(TCGObjectIdentifier.tcgTrIdEntGeoLocation, EntityGeoLocationTrait.class);
        put(TCGObjectIdentifier.tcgTrIdCountryOfOrigin, CountryOfOriginTrait.class);
    }};

    private static final Map<ASN1ObjectIdentifier, Class<? extends Trait<?,?>>> CAT_REGISTRY = new HashMap<>() {{
        put(TCGObjectIdentifier.tcgTrCatPlatformManufacturer, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformModel, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformVersion, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformSerial, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier, PENTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformOwnership, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentClass, ComponentClassTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentManufacturer, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentModel, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentSerial, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentStatus, StatusTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentLocation, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentRevision, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentFieldReplaceable, BooleanTrait.class);
        put(TCGObjectIdentifier.tcgTrCatEkCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatIakCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatIdevidCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatDiceCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatSpdmCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPemCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatDeltaPlatformCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatRebasePlatformCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatGenericCertificate, CertificateIdentifierTrait.class);
        put(TCGObjectIdentifier.tcgTrCatCommonCriteria, CommonCriteriaTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentIdentifierV11, ComponentIdentifierV11Trait.class);
        put(TCGObjectIdentifier.tcgTrCatFipsLevel, FIPSLevelTrait.class);
        put(TCGObjectIdentifier.tcgTrCatIso9000, ISO9000Trait.class);
        put(TCGObjectIdentifier.tcgTrCatNetworkMac, NetworkMACTrait.class);
        put(TCGObjectIdentifier.tcgTrCatOid, OIDTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPen, PENTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformFirmwareCapabilities, PlatformFirmwareCapabilitiesTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformFirmwareSignatureVerification, PlatformFirmwareSignatureVerificationTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformFirmwareUpdateCompliance, PlatformFirmwareUpdateComplianceTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPlatformHardwareCapabilities, PlatformHardwareCapabilitiesTrait.class);
        put(TCGObjectIdentifier.tcgTrCatRtm, RTMTrait.class);
        put(TCGObjectIdentifier.tcgTrCatPublicKey, PublicKeyTrait.class);
        put(TCGObjectIdentifier.tcgTrCatComponentPartNumber, UTF8StringTrait.class);
        put(TCGObjectIdentifier.tcgTrCatEntGeoLocation, EntityGeoLocationTrait.class);
        put(TCGObjectIdentifier.tcgTrCatCountryOfOrigin, CountryOfOriginTrait.class);
    }};

    /**
     * Trait classes indexed by Trait Value alias
     */
    private static final Map<String, Class<? extends Trait<?, ?>>> ALIAS_REGISTRY = new HashMap<>() {{
        put("asn1", ASN1ObjectTrait.class);
        put("asn1ObjectTrait", ASN1ObjectTrait.class);
        put("bool", BooleanTrait.class);
        put("boolean", BooleanTrait.class);
        put("booleanTrait", BooleanTrait.class);
        put("booleanValue", BooleanTrait.class);
        put("certificateIdentifier", CertificateIdentifierTrait.class);
        put("certificateIdentifierTrait", CertificateIdentifierTrait.class);
        put("commonCriteria", CommonCriteriaTrait.class);
        put("commonCriteriaTrait", CommonCriteriaTrait.class);
        put("componentClass", ComponentClassTrait.class);
        put("componentClassTrait", ComponentClassTrait.class);
        put("componentClassValue", ComponentClassTrait.class);
        put("componentIdentifierV11", ComponentIdentifierV11Trait.class);
        put("componentIdentifierV11Trait", ComponentIdentifierV11Trait.class);
        put("fipsLevel", FIPSLevelTrait.class);
        put("fipsLevelTrait", FIPSLevelTrait.class);
        put("iso9000", ISO9000Trait.class);
        put("iso9000Trait", ISO9000Trait.class);
        put("networkMAC", NetworkMACTrait.class);
        put("networkMACTrait", NetworkMACTrait.class);
        put("oid", OIDTrait.class);
        put("oidTrait", OIDTrait.class);
        put("pen", PENTrait.class);
        put("penTrait", PENTrait.class);
        put("platformFirmwareCapabilities", PlatformFirmwareCapabilitiesTrait.class);
        put("platformFirmwareCapabilitiesTrait", PlatformFirmwareCapabilitiesTrait.class);
        put("platformFirmwareSignatureVerification", PlatformFirmwareSignatureVerificationTrait.class);
        put("platformFirmwareSignatureVerificationTrait", PlatformFirmwareSignatureVerificationTrait.class);
        put("platformFirmwareUpdateCompliance", PlatformFirmwareUpdateComplianceTrait.class);
        put("platformFirmwareUpdateComplianceTrait", PlatformFirmwareUpdateComplianceTrait.class);
        put("platformHardwareCapabilities", PlatformHardwareCapabilitiesTrait.class);
        put("platformHardwareCapabilitiesTrait", PlatformHardwareCapabilitiesTrait.class);
        put("rTM", RTMTrait.class);
        put("rTMTrait", RTMTrait.class);
        put("rTMTypes", RTMTrait.class);
        put("attributeStatus", StatusTrait.class);
        put("status", StatusTrait.class);
        put("statusTrait", StatusTrait.class);
        put("uri", URITrait.class);
        put("uriReference", URITrait.class);
        put("uriTrait", URITrait.class);
        put("utf8", UTF8StringTrait.class);
        put("utf8String", UTF8StringTrait.class);
        put("utf8StringTrait", UTF8StringTrait.class);
        put("ia5", IA5StringTrait.class);
        put("ia5String", IA5StringTrait.class);
        put("ia5StringTrait", IA5StringTrait.class);
        put("pem", PEMCertStringTrait.class);
        put("pemTrait", PEMCertStringTrait.class);
        put("publicKey", PublicKeyTrait.class);
        put("publicKeyTrait", PublicKeyTrait.class);
        put("entGeoLocation", EntityGeoLocationTrait.class);
        put("entGeoLocationTrait", EntityGeoLocationTrait.class);
        put("countryOfOrigin", CountryOfOriginTrait.class);
        put("countryOfOriginTrait", CountryOfOriginTrait.class);
    }};

    public static Class<? extends Trait<?, ?>> getTraitClassForId(ASN1ObjectIdentifier oid) {
        return ID_REGISTRY.getOrDefault(oid, ASN1ObjectTrait.class);
    }

    public static Class<? extends Trait<?, ?>> getTraitClassForId(String oid) {
        return getTraitClassForId(new ASN1ObjectIdentifier(oid));
    }

    public static Class<? extends Trait<?, ?>> getTraitClassForCategory(ASN1ObjectIdentifier oid) {
        return CAT_REGISTRY.getOrDefault(oid, ASN1ObjectTrait.class);
    }

    public static Class<? extends Trait<?, ?>> getTraitClassForCategory(String oid) {
        return getTraitClassForCategory(new ASN1ObjectIdentifier(oid));
    }

    public static Set<ASN1ObjectIdentifier> getRegisteredIds() {
        return ID_REGISTRY.keySet();
    }

    public static Set<String> getRegisteredAliases() {
        return ALIAS_REGISTRY.keySet();
    }

    public static Class<? extends Trait<?, ?>> getTraitClassByAlias(String alias) {
        return ALIAS_REGISTRY.get(alias);
    }
}
