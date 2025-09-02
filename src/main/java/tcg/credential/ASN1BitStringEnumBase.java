package tcg.credential;

import java.util.function.Function;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERBitString;

/**
 * Abstract base class for enums mapped to ASN1Enumerated.
 * @param <T> Java enum type that implements EnumWithValue.
 */
public abstract class ASN1BitStringEnumBase<T extends Enum<T> & EnumWithIntegerValue> extends ASN1EnumBase<T> {
    /**
     * New instance from int value and enum class.
     * @param value int
     * @param enumType Java enum type that implements EnumWithValue.
     */
    public ASN1BitStringEnumBase(int value, @NonNull Class<T> enumType) {
        super(value, enumType);
    }

    @Override
    public ASN1Primitive toASN1Primitive() {
        return new DERBitString(getValue());
    }

    /**
     * Conversion between int value and ASN1BitString.
     * @param enumType Java enum type that implements EnumWithValue.
     * @param method constructor
     * @return Factory
     * @param <T> Java enum type that implements EnumWithValue.
     * @param <U> Subclass
     */
    public static <T extends Enum<T> & EnumWithIntegerValue, U extends ASN1BitStringEnumBase<T>> Factory<T, U> factory(
            @NonNull Class<T> enumType, @NonNull Function<Integer, U> method) {
        return new Factory<>() {
            @Override
            public U create(int value) {
                return method.apply(value);
            }

            @Override
            public U create(@NonNull T value) {
                return create(value.getValue());
            }

            @Override
            public Class<T> getEnumType() {
                return enumType;
            }

            @Override
            public int fromASN1(@NonNull ASN1Primitive asn1Primitive) {
                return DERBitString.getInstance(asn1Primitive).intValue();
            }
        };
    }
}