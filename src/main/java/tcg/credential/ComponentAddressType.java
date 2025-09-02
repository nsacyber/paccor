package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

@AllArgsConstructor
@Getter
public enum ComponentAddressType implements EnumWithStringValue {
    ETHERNETMAC(TCGObjectIdentifier.tcgAddressEthernetMac.getId()),
    WLANMAC(TCGObjectIdentifier.tcgAddressWlanMac.getId()),
    BLUETOOTHMAC(TCGObjectIdentifier.tcgAddressBluetoothMac.getId());

    @NonNull
    private final String value;

    public final ASN1ObjectIdentifier getOid() {
        return new ASN1ObjectIdentifier(value);
    }
}