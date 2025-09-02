package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * UTF8StringTrait TRAIT ::= {
 *      SYNTAX UTF8String(SIZE (1..STRMAX))
 *      IDENTIFIED BY tcg-tr-ID-UTF8String }
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UTF8StringTrait extends ASN1UTF8StringTrait {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return UTF8StringTrait
     */
    public static UTF8StringTrait getInstance(Object obj) {
        return (UTF8StringTrait)ASN1UTF8StringTrait.getInstance(obj);
    }

    @Override
    public UTF8StringTrait createInstance(Object obj) {
        return UTF8StringTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return UTF8StringTrait
     */
    public static UTF8StringTrait fromASN1Sequence(ASN1Sequence seq) {
        return (UTF8StringTrait)ASN1UTF8StringTrait.fromASN1Sequence(seq);
    }

    /**
     * Initializes a builder with expected UTF8StringTrait metadata.
     * @return UTF8StringTrait Builder
     */
    public static UTF8StringTraitBuilder<?, ?> builder() {
        return new UTF8StringTraitBuilderImpl()
                .traitId(TCGObjectIdentifier.tcgTrIdUtf8String);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class UTF8StringTraitBuilder<C extends UTF8StringTrait, B extends UTF8StringTraitBuilder<C, B>> extends ASN1UTF8StringTraitBuilder<C, B> {
    }
}
