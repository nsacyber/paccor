package tcg.credential;

import java.math.BigInteger;
import java.util.Hashtable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * <pre>
 * SecurityLevel ::= ENUMERATED {
 *      level1 (1),
 *      level2 (2),
 *      level3 (3),
 *      level4 (4) }
 * </pre>
 */
public class SecurityLevel extends ASN1Object {
	
    ASN1Enumerated value;
	
	public enum Enumerated {
        level1(1),
        level2(2),
        level3(3),
        level4(4);
        
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
        
        public static final Enumerated lookup(String value) {
            if (value.matches("-?\\d+")) {
                return lookup(Integer.valueOf(value));
            }
            
            for (Enumerated opt : lookup.values()) {
                if (opt.name().equalsIgnoreCase(value)) {
                    return opt;
                }
            }
            throw new IllegalArgumentException(value + " is not a valid enum constant.");
        }
    }
	
	public static SecurityLevel getInstance(Object obj) {
		if (obj instanceof SecurityLevel) {
			return (SecurityLevel) obj;
		}
		if (obj != null) {
			return new SecurityLevel(ASN1Enumerated.getInstance(obj).getValue().intValue());
		}
		return null;
	}
	
	public SecurityLevel(int level) {
	    this(Enumerated.lookup(level));
	}
	
	public SecurityLevel(String level) {
	    this(Enumerated.lookup(level));
	}
	
	public SecurityLevel(Enumerated option) {
	    value = (option != null) ? new ASN1Enumerated(option.ordinal()) : null;
	}
	
	public BigInteger getValue() {
        return value.getValue();
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

}
