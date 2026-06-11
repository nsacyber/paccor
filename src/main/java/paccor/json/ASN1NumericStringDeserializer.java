package paccor.json;

import org.bouncycastle.asn1.ASN1NumericString;
import org.bouncycastle.asn1.DERNumericString;

/**
 * Custom deserializer for the {@code ASN1NumericString} class.
 */
public class ASN1NumericStringDeserializer extends AbstractASN1StringDeserializer<ASN1NumericString> {
    public ASN1NumericStringDeserializer() {
        super(DERNumericString::new, ASN1NumericString.class);
    }
}
