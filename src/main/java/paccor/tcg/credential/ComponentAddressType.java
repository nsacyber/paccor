package paccor.tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * Component Address Types
 */
@AllArgsConstructor
@Getter
public enum ComponentAddressType implements EnumWithStringValue {
    /**
     * ETHERNET MAC
     */
    ETHERNETMAC(TCGObjectIdentifier.tcgAddressEthernetMac.getId()),
    /**
     * WLAN MAC
     */
    WLANMAC(TCGObjectIdentifier.tcgAddressWlanMac.getId()),
    /**
     * BLUETOOTH MAC
     */
    BLUETOOTHMAC(TCGObjectIdentifier.tcgAddressBluetoothMac.getId());

    @NonNull
    private final String value;

    /**
     * Get the OID for this enum value.
     * @return OID
     */
    public final ASN1ObjectIdentifier getOid() {
        return new ASN1ObjectIdentifier(value);
    }
}
