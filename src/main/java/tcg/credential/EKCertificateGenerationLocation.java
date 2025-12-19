package tcg.credential;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * <pre>
 * EKCertificateGenerationLocation ::= ENUMERATED {
 *      tpmManufacturer (0),
 *      platformManufacturer (1),
 *      ekCertSigner (2) }
 * </pre>
 */
public class EKCertificateGenerationLocation extends ASN1Object {

	ASN1Enumerated value;
	
	public enum Enumerated {
	    tpmManufacturer(0),
	    platformManufacturer(1),
	    ekCertSigner(2);
	    
		private static final Map<Integer, Enumerated> lookup =
			new HashMap<>();
        
        static {
            for(Enumerated e : values()) {
                lookup.put(e.getValue(), e);
            }
        }
        
        private final int value;
        
        private Enumerated(int value) {
            this.value = value;
        }
        
        public final int getValue() {
            return value;
        }
        
        public static final Enumerated lookup(int value) {
            return lookup.get(value);
        }
	}
	
	public static EKCertificateGenerationLocation getInstance(Object obj) {
		if (obj instanceof EKCertificateGenerationLocation good) {
			return good;
		}
		if (obj != null) {
			return new EKCertificateGenerationLocation(ASN1Enumerated.getInstance(obj).getValue().intValue());
		}
		return null;
	}
	
	public EKCertificateGenerationLocation(int location) {
	    this(Enumerated.lookup(location));
	}
	
	public EKCertificateGenerationLocation(Enumerated option) {
	    value = (option != null) ? new ASN1Enumerated(option.getValue()) : null;
	}

        @Override
	public String toString() {
	    String str = "invalid";
        if (value != null) {
            int type = getValue().intValue();
            str = getClass().getSimpleName() + ": " + Enumerated.values()[type].name();
        }
        return str;
    }

	@Override
	public ASN1Primitive toASN1Primitive() {
		return value;
	}

	public BigInteger getValue() {
        return value.getValue();
    }
}
