package tcg.credential;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * <pre>
 * EKGenerationType ::= ENUMERATED {
 *      internal (0),
 *      injected (1),
 *      internalRevocable(2),
 *      injectedRevocable(3) }
 * </pre>
 */
public class EKGenerationType extends ASN1Object {
	
	ASN1Enumerated value = null;
	
	// enum.ordinal() is based on the ordering below, 
	public enum Enumerated {
	    internal(0),
	    injected(1),
	    internalRevocable(2),
	    injectedRevocable(3);
	    
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
	
	public static EKGenerationType getInstance(Object obj) {
		if (obj instanceof EKGenerationType good) {
			return good;
		}
		if (obj != null) {
			return new EKGenerationType(ASN1Enumerated.getInstance(obj).getValue().intValue());
		}
		return null;
	}
	
	public EKGenerationType(int type) {
	    this(Enumerated.lookup(type));
	}
	
	public EKGenerationType(Enumerated option) {
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
