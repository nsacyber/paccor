package paccor.json;

import org.bouncycastle.asn1.ASN1VisibleString;
import org.bouncycastle.asn1.DERVisibleString;

/**
 * Custom deserializer for the {@code ASN1VisibleString} class.
 */
public class ASN1VisibleStringDeserializer extends AbstractASN1StringDeserializer<ASN1VisibleString> {
    public ASN1VisibleStringDeserializer() {
        super(DERVisibleString::new, ASN1VisibleString.class);
    }
}
