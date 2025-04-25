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
 *      ID tcg-at-platformConfiguration-v1 }
 * 
 * PlatformConfiguration ::= SEQUENCE {
 *      componentIdentifier [0] IMPLICIT SEQUENCE(SIZE(1..CONFIGMAX)) OF ComponentIdentifier OPTIONAL,
 *      platformProperties [1] IMPLICIT SEQUENCE(SIZE(1..CONFIGMAX)) OF Properties OPTIONAL,
 *      platformPropertiesUri [2] IMPLICIT URIReference OPTIONAL }
 * </pre>
 */
public class PlatformConfiguration extends ASN1Object {
	
	// minimum 0, max 3
	ComponentIdentifier[] componentIdentifier = null; // optional, tagged 0, placed in sequence of 1 to configmax length
	PlatformProperties[] platformProperties = null; // optional, tagged 1, placed in sequence of 1 to configmax length
	URIReference platformPropertiesUri = null; // optional, tagged 2

	public static PlatformConfiguration getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformConfiguration) {
			return (PlatformConfiguration) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new PlatformConfiguration((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	private PlatformConfiguration(ASN1Sequence seq) {
		if (seq.size() < 0 || seq.size() > 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 0)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof ASN1Sequence tempSeq) {
				ASN1Object[] tempElements = (ASN1Object[]) tempSeq.toArray();
				componentIdentifier = new ComponentIdentifier[tempElements.length];
				for(int i = 0; i < tempElements.length; i++) {
					if (tempElements[i] instanceof ComponentIdentifier) {
						componentIdentifier[i] = (ComponentIdentifier) tempElements[i];
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
			if (elementObject instanceof ASN1Sequence tempSeq) {
				ASN1Object[] tempElements = (ASN1Object[]) tempSeq.toArray();
				platformProperties = new PlatformProperties[tempElements.length];
				for(int i = 0; i < tempElements.length; i++) {
					if (tempElements[i] instanceof PlatformProperties) {
						platformProperties[i] = (PlatformProperties) tempElements[i];
					} else {
						throw new IllegalArgumentException("Expected Properties, received " + tempElements[i].getClass().getName());
					}
				}
			} else {
				throw new IllegalArgumentException("Expected ASN1Sequence object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 2)) {
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
	
	public PlatformConfiguration(List<ComponentIdentifier> componentIdentifier,
             List<PlatformProperties> platformProperties, URIReference platformPropertiesUri) {
	    this(componentIdentifier.toArray(new ComponentIdentifier[componentIdentifier.size()]),
	         platformProperties.toArray(new PlatformProperties[platformProperties.size()]),
	         platformPropertiesUri);
	}
	
	public PlatformConfiguration(ComponentIdentifier[] componentIdentifier, PlatformProperties[] platformProperties,
			URIReference platformPropertiesUri) {
		this.componentIdentifier = componentIdentifier;
		this.platformProperties = platformProperties;
		this.platformPropertiesUri = platformPropertiesUri;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (componentIdentifier != null) {
			ASN1EncodableVector vec2 = new ASN1EncodableVector();
			for (int i = 0; i < componentIdentifier.length; i++) {
				vec2.add(componentIdentifier[i]);
			}
			vec.add(new DERTaggedObject(false, 0, new DERSequence(vec2)));
		}
		if (platformProperties != null) {
			ASN1EncodableVector vec2 = new ASN1EncodableVector();
			for (int i = 0; i < platformProperties.length; i++) {
				vec2.add(platformProperties[i]);
			}
			vec.add(new DERTaggedObject(false, 1, new DERSequence(vec2)));
		}
		if (platformPropertiesUri != null) {
			vec.add(new DERTaggedObject(false, 2, platformPropertiesUri));
		}
		return new DERSequence(vec);
	}

	public ComponentIdentifier[] getComponentIdentifier() {
		return componentIdentifier;
	}

	public PlatformProperties[] getPlatformProperties() {
		return platformProperties;
	}

	public URIReference getPlatformPropertiesUri() {
		return platformPropertiesUri;
	}
}
