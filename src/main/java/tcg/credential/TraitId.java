package tcg.credential;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class TraitId {
    /**
     * Trait classes indexed by Trait ID OID
     */
    private static final Map<ASN1ObjectIdentifier, Class<? extends Trait<?,?>>> REGISTRY = new HashMap<>() {{
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

    public static Class<? extends Trait<?, ?>> getTraitClassForId(ASN1ObjectIdentifier oid) {
        return REGISTRY.getOrDefault(oid, ASN1ObjectTrait.class);
    }
}
