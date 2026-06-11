package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * CountryOfOriginTrait TRAIT ::= {
 *      SYNTAX OriginComposition
 *      IDENTIFIED BY tcg-tr-ID-countryOfOrigin }
 *
 * A Trait that specifies a country of origin using the CountryOfOriginTrait SHALL use tcg-tr-cat-countryOfOrigin in its traitCategory field.
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public class CountryOfOriginTrait extends Trait<OriginComposition, CountryOfOriginTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return CountryOfOriginTrait
     */
    public static CountryOfOriginTrait getInstance(Object obj) {
        return Trait.getInstance(obj, CountryOfOriginTrait.class, CountryOfOriginTrait::fromASN1Sequence, CountryOfOriginTrait::fromJsonNode);
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
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return CountryOfOriginTrait
     */
    public static CountryOfOriginTrait fromJsonNode(JsonNode node) {
        return CountryOfOriginTrait.builder()
                .traitType(CountryOfOriginTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, OriginComposition.class)
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
