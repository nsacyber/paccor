package json;

import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.DERBMPString;

/**
 * Custom deserializer for the {@code ASN1BMPString} class.
 */
public class ASN1BMPStringDeserializer extends AbstractASN1StringDeserializer<ASN1BMPString> {
    public ASN1BMPStringDeserializer() {
        super(DERBMPString::new, ASN1BMPString.class);
    }
}
