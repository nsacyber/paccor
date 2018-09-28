package tcg.credential;

import java.math.BigInteger;
import java.util.Hashtable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * <pre>
 * EvaluationAssuranceLevel ::= ENUMERATED {
 *      level1 (1),
 *      level2 (2),
 *      level3 (3),
 *      level4 (4),
 *      level5 (5),
 *      level6 (6),
 *      level7 (7) }
 * </pre>
 */
public class EvaluationAssuranceLevel extends ASN1Object {
    
	private ASN1Enumerated value;
	
	public enum Enumerated {
	    level1(1),
	    level2(2),
	    level3(3),
	    level4(4),
	    level5(5),
	    level6(6),
	    level7(7);
	    
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
	
	public static EvaluationAssuranceLevel getInstance(Object obj) {
		if (obj instanceof EvaluationAssuranceLevel) {
			return (EvaluationAssuranceLevel) obj;
		}
		if (obj != null) {
			return new EvaluationAssuranceLevel(ASN1Enumerated.getInstance(obj).getValue().intValue());
		}
		return null;
	}

	public EvaluationAssuranceLevel(int level) {
	    this(Enumerated.lookup(level));
	}
	
	public EvaluationAssuranceLevel(String level) {
	    this(Enumerated.lookup(level));
	}
	
	public EvaluationAssuranceLevel(Enumerated option) {
	    value = (option != null) ? new ASN1Enumerated(option.getValue()) : null;
	}
	
	public String toString() {
	    String str = "invalid";
        if (value != null) {
            int type = getValue().intValue();
            str = getClass().getSimpleName() + ": " + Enumerated.values()[type-1].name();
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
