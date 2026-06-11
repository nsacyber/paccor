package paccor.json;

import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.DERPrintableString;

/**
 * Custom deserializer for the {@code ASN1PrintableString} class.
 */
public class ASN1PrintableStringDeserializer extends AbstractASN1StringDeserializer<ASN1PrintableString> {
    public ASN1PrintableStringDeserializer() {
        super(DERPrintableString::new, ASN1PrintableString.class);
    }
}
