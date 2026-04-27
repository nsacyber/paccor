package json;

import org.bouncycastle.asn1.ASN1T61String;
import org.bouncycastle.asn1.DERT61String;

/**
 * Custom deserializer for the {@code ASN1T61String} class.
 */
public class ASN1T61StringDeserializer extends AbstractASN1StringDeserializer<ASN1T61String> {
    public ASN1T61StringDeserializer() {
        super(DERT61String::new, ASN1T61String.class);
    }
}
