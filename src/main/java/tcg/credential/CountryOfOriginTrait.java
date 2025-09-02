package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * CountryOfOriginTrait TRAIT ::= {
 *      SYNTAX OriginComposition
 *      IDENTIFIED BY tcg-tr-ID-countryOfOrigin }
 *
 * A Trait that specifies a country of origin using the CountryOfOriginTrait SHALL use tcg-tr-cat-countryOfOrigin in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CountryOfOriginTrait extends Trait<OriginComposition, CountryOfOriginTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return CountryOfOriginTrait
     */
    public static CountryOfOriginTrait getInstance(Object obj) {
        return Trait.getInstance(obj, CountryOfOriginTrait.class, CountryOfOriginTrait::fromASN1Sequence);
    }

    @Override
    public CountryOfOriginTrait createInstance(Object obj) {
        return CountryOfOriginTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return CountryOfOriginTrait
     */
    public static CountryOfOriginTrait fromASN1Sequence(ASN1Sequence seq) {
        return CountryOfOriginTrait.builder()
                .traitType(CountryOfOriginTrait.class)
                .fromASN1Sequence(seq, OriginComposition::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected CountryOfOriginTrait metadata.
     * @return CountryOfOriginTrait Builder
     */
    public static CountryOfOriginTraitBuilder<?, ?> builder() {
        return new CountryOfOriginTraitBuilderImpl()
                .traitType(CountryOfOriginTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdCountryOfOrigin)
                .traitCategory(TCGObjectIdentifier.tcgTrCatCountryOfOrigin);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class CountryOfOriginTraitBuilder<C extends CountryOfOriginTrait, B extends CountryOfOriginTraitBuilder<C, B>> extends TraitBuilder<OriginComposition, CountryOfOriginTrait, C, B> {
    }
}
