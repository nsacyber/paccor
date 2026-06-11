package paccor.json;

import org.bouncycastle.asn1.ASN1GeneralString;
import org.bouncycastle.asn1.DERGeneralString;

/**
 * Custom deserializer for the {@code ASN1GeneralString} class.
 */
public class ASN1GeneralStringDeserializer extends AbstractASN1StringDeserializer<ASN1GeneralString> {
    public ASN1GeneralStringDeserializer() {
        super(DERGeneralString::new, ASN1GeneralString.class);
    }
}
