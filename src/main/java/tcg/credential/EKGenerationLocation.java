package tcg.credential;

import java.math.BigInteger;
import java.util.Hashtable;
import tcg.credential.EKGenerationType.Enumerated;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.util.Integers;

/**
 * <pre>
 * EKGenerationLocation ::= ENUMERATED {
 *      tpmManufacturer (0),
 *      platformManufacturer (1),
 *      ekCertSigner (2) }
 * </pre>
 */
public class EKGenerationLocation extends ASN1Object {

	ASN1Enumerated value;
	
	public enum Enumerated {
	    tpmManufacturer(0),
	    platformManufacturer(1),
	    ekCertSigner(2);
	    
	    private static final Hashtable<Integer, Enumerated> lookup =
                new Hashtable<Integer, Enumerated>();
        
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
	
	public static EKGenerationLocation getInstance(Object obj) {
		if (obj instanceof EKGenerationLocation) {
			return (EKGenerationLocation) obj;
		}
		if (obj != null) {
			return new EKGenerationLocation(ASN1Enumerated.getInstance(obj).getValue().intValue());
		}
		return null;
	}
	
	public EKGenerationLocation(int location) {
	    this(Enumerated.lookup(location));
	}
	
	public EKGenerationLocation(Enumerated option) {
	    value = (option != null) ? new ASN1Enumerated(option.ordinal()) : null;
	}
	
	public String toString() {
	    String str = "invalid";
        if (value != null) {
            int type = getValue().intValue();
            str = getClass().getSimpleName() + ": " + Enumerated.values()[type].name();
        }
        return str;
    }

	public ASN1Primitive toASN1Primitive() {
		return value;
	}

	public BigInteger getValue() {
        return value.getValue();
    }

}
