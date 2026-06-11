package paccor.json;

import org.bouncycastle.asn1.ASN1BMPString;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1GeneralString;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1NumericString;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1T61String;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.ASN1VisibleString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import paccor.tcg.credential.ComponentAddress;
import paccor.tcg.credential.TCGCredentialType;
import paccor.tcg.credential.TCGPlatformSpecification;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitMap;
import tools.jackson.databind.module.SimpleModule;

public class JacksonAsn1Module extends SimpleModule {
    public JacksonAsn1Module() {
        addSerializer(ASN1Object.class, new ASN1ObjectSerializer());
        addSerializer(TCGPlatformSpecification.class, new ASN1ObjectSerializer());

        addDeserializer(ASN1BitString.class, new ASN1BitStringDeserializer());
        addDeserializer(ASN1BMPString.class, new ASN1BMPStringDeserializer());
        addDeserializer(ASN1Boolean.class, new ASN1BooleanDeserializer());
        addDeserializer(ASN1GeneralizedTime.class, new ASN1GeneralizedTimeDeserializer());
        addDeserializer(ASN1GeneralString.class, new ASN1GeneralStringDeserializer());
        addDeserializer(ASN1IA5String.class, new ASN1IA5StringDeserializer());
        addDeserializer(ASN1Integer.class, new ASN1IntegerDeserializer());
        addDeserializer(ASN1NumericString.class, new ASN1NumericStringDeserializer());
        addDeserializer(ASN1ObjectIdentifier.class, new ASN1ObjectIdentifierDeserializer());
        addDeserializer(ASN1OctetString.class, new ASN1OctetStringDeserializer());
        addDeserializer(ASN1PrintableString.class, new ASN1PrintableStringDeserializer());
        addDeserializer(ASN1T61String.class, new ASN1T61StringDeserializer());
        addDeserializer(ASN1UTF8String.class, new ASN1UTF8StringDeserializer());
        addDeserializer(ASN1VisibleString.class, new ASN1VisibleStringDeserializer());

        addDeserializer(AlgorithmIdentifier.class, new AlgorithmIdentifierDeserializer());
        addDeserializer(IssuerSerial.class, new IssuerSerialDeserializer());
        addDeserializer(KeyUsage.class, new KeyUsageDeserializer());
        addDeserializer(SubjectPublicKeyInfo.class, new SubjectPublicKeyInfoDeserializer());
        addDeserializer(TCGCredentialType.class, new TCGCredentialTypeDeserializer());

        addDeserializer(ComponentAddress.class, new ComponentAddressDeserializer());
        addDeserializer(Trait.class, new TraitDeserializer());
        addDeserializer(TraitMap.class, new TraitMapDeserializer());
    }
}
