package paccor.json;

import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * Custom deserializer for the {@code ASN1UTF8String} class.
 */
public class ASN1UTF8StringDeserializer extends AbstractASN1StringDeserializer<ASN1UTF8String> {
    public ASN1UTF8StringDeserializer() {
        super(DERUTF8String::new, ASN1UTF8String.class);
    }
}
