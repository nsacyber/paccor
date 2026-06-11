package paccor.json;

import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.DERIA5String;

/**
 * Custom deserializer for the {@code ASN1IA5String} class.
 */
public class ASN1IA5StringDeserializer extends AbstractASN1StringDeserializer<ASN1IA5String> {
    public ASN1IA5StringDeserializer() {
        super(DERIA5String::new, ASN1IA5String.class);
    }
}
