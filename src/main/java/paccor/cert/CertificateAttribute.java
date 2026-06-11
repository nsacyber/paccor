package paccor.cert;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

record CertificateAttribute(ASN1ObjectIdentifier oid, ASN1Encodable value) {
}
