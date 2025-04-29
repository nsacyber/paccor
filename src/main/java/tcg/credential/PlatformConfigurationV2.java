package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import java.util.List;

/**
 * <pre>
 * platformConfiguration ATTRIBUTE ::= {
 *      WITH SYNTAX PlatformConfiguration
 *      ID tcg-at-platformConfiguration-v2 }
 * 
 * PlatformConfiguration ::= SEQUENCE {
 *      componentIdentifiers [0] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF ComponentIdentifier OPTIONAL,
 *      componentIdentifiersUri [1] IMPLICIT URIReference OPTIONAL,
 *      platformProperties [1] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF Properties OPTIONAL,
 *      platformPropertiesUri [2] IMPLICIT URIReference OPTIONAL }
 * </pre>
 */
public class PlatformConfigurationV2 extends ASN1Object {
	
	// minimum 0, max 4
	ComponentIdentifierV2[] componentIdentifiers = null; // optional, tagged 0, placed in sequence of 1 to max length
	URIReference componentIdentifiersUri = null; // optional, tagged 1
	PlatformPropertiesV2[] platformProperties = null; // optional, tagged 2, placed in sequence of 1 to max length
	URIReference platformPropertiesUri = null; // optional, tagged 3

	public static PlatformConfigurationV2 getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformConfigurationV2) {
			return (PlatformConfigurationV2) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new PlatformConfigurationV2((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	private PlatformConfigurationV2(ASN1Sequence seq) {
		if (seq.size() < 0 || seq.size() > 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 0)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof ASN1Sequence tempSeq) {
				ASN1Object[] tempElements = (ASN1Object[]) tempSeq.toArray();
				componentIdentifiers = new ComponentIdentifierV2[tempElements.length];
				for(int i = 0; i < tempElements.length; i++) {
					if (tempElements[i] instanceof ComponentIdentifierV2) {
						componentIdentifiers[i] = (ComponentIdentifierV2) tempElements[i];
					} else {
						throw new IllegalArgumentException("Expected ComponentIdentifier, received " + tempElements[i].getClass().getName());
					}
				}
			} else {
				throw new IllegalArgumentException("Expected ASN1Sequence object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 1)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
            if (elementObject instanceof URIReference uriRef) {
                componentIdentifiersUri = uriRef;
            } else {
                throw new IllegalArgumentException("Expected URIReference object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 2)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof ASN1Sequence tempSeq) {
				ASN1Object[] tempElements = (ASN1Object[]) tempSeq.toArray();
				platformProperties = new PlatformPropertiesV2[tempElements.length];
				for(int i = 0; i < tempElements.length; i++) {
					if (tempElements[i] instanceof PlatformPropertiesV2) {
						platformProperties[i] = (PlatformPropertiesV2) tempElements[i];
					} else {
						throw new IllegalArgumentException("Expected Properties, received " + tempElements[i].getClass().getName());
					}
				}
			} else {
				throw new IllegalArgumentException("Expected ASN1Sequence object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 3)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof URIReference uriRef) {
				platformPropertiesUri = uriRef;
			} else {
				throw new IllegalArgumentException("Expected URIReference object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if ((elements.length - pos) > 0) {
			throw new IllegalArgumentException("Too many elements in PlatformConfiguration");
		}
	}
	
	public PlatformConfigurationV2(List<ComponentIdentifierV2> componentIdentifiers, URIReference componentIdentifiersUri, 
             List<PlatformPropertiesV2> platformProperties, URIReference platformPropertiesUri) {
	    this(componentIdentifiers.toArray(new ComponentIdentifierV2[componentIdentifiers.size()]),
	         componentIdentifiersUri,
	         platformProperties.toArray(new PlatformPropertiesV2[platformProperties.size()]),
	         platformPropertiesUri);
	}
	
	public PlatformConfigurationV2(ComponentIdentifierV2[] componentIdentifiers, URIReference componentIdentifiersUri,
	        PlatformPropertiesV2[] platformProperties, URIReference platformPropertiesUri) {
		this.componentIdentifiers = componentIdentifiers;
		this.componentIdentifiersUri = componentIdentifiersUri;
		this.platformProperties = platformProperties;
		this.platformPropertiesUri = platformPropertiesUri;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (componentIdentifiers != null) {
			ASN1EncodableVector vec2 = new ASN1EncodableVector();
			for (int i = 0; i < componentIdentifiers.length; i++) {
				vec2.add(componentIdentifiers[i]);
			}
			vec.add(new DERTaggedObject(false, 0, new DERSequence(vec2)));
		}
		if (componentIdentifiersUri != null) {
            vec.add(new DERTaggedObject(false, 1, componentIdentifiersUri));
        }
		if (platformProperties != null) {
			ASN1EncodableVector vec2 = new ASN1EncodableVector();
			for (int i = 0; i < platformProperties.length; i++) {
				vec2.add(platformProperties[i]);
			}
			vec.add(new DERTaggedObject(false, 2, new DERSequence(vec2)));
		}
		if (platformPropertiesUri != null) {
			vec.add(new DERTaggedObject(false, 3, platformPropertiesUri));
		}
		return new DERSequence(vec);
	}

	public ComponentIdentifierV2[] getComponentIdentifier() {
		return componentIdentifiers;
	}
	
	public URIReference getComponentIdentifiersUri() {
        return componentIdentifiersUri;
    }

	public PlatformPropertiesV2[] getPlatformProperties() {
		return platformProperties;
	}

	public URIReference getPlatformPropertiesUri() {
		return platformPropertiesUri;
	}
}
