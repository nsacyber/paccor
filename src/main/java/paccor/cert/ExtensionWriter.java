package paccor.cert;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

@FunctionalInterface
interface ExtensionWriter {
    void add(ASN1ObjectIdentifier oid, boolean critical, ASN1Encodable value) throws Exception;
}
