package tcg.credential;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * <pre>
 * StrengthOfFunction ::= ENUMERATED {
 *      basic (0),
 *      medium (1),
 *      high (2) }
 * </pre>
 */
public class StrengthOfFunction extends ASN1Object {
	
    // Case taken from spec
	public static final int basic = 0;
	public static final int medium = 1;
	public static final int high = 2;
	
	ASN1Enumerated value;
	
	public enum Enumerated {
        basic(0),
        medium(1),
        high(2);
        
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
        
        public static final Enumerated lookup(String value) {
            for (Enumerated opt : lookup.values()) {
                if (opt.name().equalsIgnoreCase(value)) {
                    return opt;
                }
            }
            throw new IllegalArgumentException(value + " is not a valid enum constant.");
        }
    }
	
	public static StrengthOfFunction getInstance(Object obj) {
		if (obj instanceof StrengthOfFunction passThru) {
			return passThru;
		}
		if (obj != null) {
			return new StrengthOfFunction(ASN1Enumerated.getInstance(obj).getValue().intValue());
		}
		return null;
	}

	public StrengthOfFunction(int strength) {
	    this(Enumerated.lookup(strength));
	}
	
	public StrengthOfFunction(String strength) {
	    this(Enumerated.lookup(strength));
	}
	
	public StrengthOfFunction(Enumerated option) {
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
