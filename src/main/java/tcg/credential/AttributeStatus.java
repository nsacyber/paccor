package tcg.credential;

import java.math.BigInteger;
import java.util.Hashtable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

import tcg.credential.StrengthOfFunction.Enumerated;

/**
 * <pre>
 * AttributeStatus ::= ENUMERATED {
 *      added (0),
 *      modified (1),
 *      removed (2) }
 * </pre>
 */
public class AttributeStatus extends ASN1Object {

    ASN1Enumerated value = null;
    
    // enum.ordinal() is based on the ordering below, 
    public enum Enumerated {
        added(0),
        modified(1),
        removed(2);
        
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
            for (Enumerated opt : lookup.values()) {
                if (opt.name().equalsIgnoreCase(value)) {
                    return opt;
                }
            }
            throw new IllegalArgumentException(value + " is not a valid enum constant.");
        }
    }
    
    public static AttributeStatus getInstance(Object obj) {
        if (obj instanceof AttributeStatus) {
            return (AttributeStatus) obj;
        }
        if (obj != null) {
            return new AttributeStatus(ASN1Enumerated.getInstance(obj).getValue().intValue());
        }
        return null;
    }
    
    public AttributeStatus(int type) {
        this(Enumerated.lookup(type));
    }
    
    public AttributeStatus(String option) {
        this(Enumerated.lookup(option));
    }
    
    public AttributeStatus(Enumerated option) {
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
