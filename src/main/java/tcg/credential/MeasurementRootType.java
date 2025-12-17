package tcg.credential;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * <pre>
 * -- V1.1 of this specification adds hybrid and physical.
 * -- Hybrid means the measurement root is capable of static AND dynamic
 * -- Physical means that the root is anchored by a physical TPM
 * -- Virtual means the TPM is virtualized (possibly running in a VMM).
 * -- TPMs or RTMs might leverage other lower layer RTMs to virtualize the
 * -- the capabilities of the platform.
 * 
 * MeasurementRootType ::= ENUMERATED {
 *      static (0),
 *      dynamic (1),
 *      nonHost (2),
 *      hybrid (3),
 *      physical (4),
 *      virtual (5) }
 * </pre>
 */
public class MeasurementRootType extends ASN1Object {
	
	ASN1Enumerated value;
	
	public enum Enumerated {
	    Static(0),
	    dynamic(1),
	    nonHost(2),
	    hybrid(3),
	    physical(4),
	    virtual(5);
	    
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
	
	public static MeasurementRootType getInstance(Object obj) {
		if (obj instanceof MeasurementRootType good) {
			return good;
		}
		if (obj != null) {
			return new MeasurementRootType(ASN1Enumerated.getInstance(obj).getValue().intValue());
		}
		return null;
	}
	
	public MeasurementRootType(int type) {
		this(Enumerated.lookup(type));
	}
	
	public MeasurementRootType(String type) {
	    this(Enumerated.lookup(type));
	}
	
	public MeasurementRootType(Enumerated option) {
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
