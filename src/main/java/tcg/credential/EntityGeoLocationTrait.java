package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * EntityGeoLocationTrait TRAIT ::= {
 *      SYNTAX EntityGeoLocation
 *      IDENTIFIED BY tcg-tr-ID-entGeoLocation }
 *
 * A Trait that specifies a geographic location of an entity using the EntityGeoLocationTrait SHALL use tcg-tr-cat-entGeoLocation in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class EntityGeoLocationTrait extends Trait<EntityGeoLocation, EntityGeoLocationTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return EntityGeoLocationTrait
     */
    public static EntityGeoLocationTrait getInstance(Object obj) {
        return Trait.getInstance(obj, EntityGeoLocationTrait.class, EntityGeoLocationTrait::fromASN1Sequence);
    }

    @Override
    public EntityGeoLocationTrait createInstance(Object obj) {
        return EntityGeoLocationTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return EntityGeoLocationTrait
     */
    public static EntityGeoLocationTrait fromASN1Sequence(ASN1Sequence seq) {
        return EntityGeoLocationTrait.builder()
                .traitType(EntityGeoLocationTrait.class)
                .fromASN1Sequence(seq, EntityGeoLocation::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected EntityGeoLocationTrait metadata.
     * @return EntityGeoLocationTrait Builder
     */
    public static EntityGeoLocationTraitBuilder<?, ?> builder() {
        return new EntityGeoLocationTraitBuilderImpl()
                .traitType(EntityGeoLocationTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdEntGeoLocation)
                .traitCategory(TCGObjectIdentifier.tcgTrCatEntGeoLocation);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class EntityGeoLocationTraitBuilder<C extends EntityGeoLocationTrait, B extends EntityGeoLocationTrait.EntityGeoLocationTraitBuilder<C, B>> extends TraitBuilder<EntityGeoLocation, EntityGeoLocationTrait, C, B> {
    }
}
