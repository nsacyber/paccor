package tcg.credential;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * Trait ::= SEQUENCE {
 *      traitId TRAIT.&amp;id({TraitSet}), -- Specifies the traitValue encoding
 *      traitCategory OBJECT IDENTIFIER, -- Identifies the information category contained in traitValue
 *      traitRegistry OBJECT IDENTIFIER, -- Identifies the registry used to match against the traitValue
 *      description [0] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      descriptionURI [1] IMPLICIT IA5String (SIZE (1..URIMAX)) OPTIONAL,
 *      traitValue OCTET STRING ( CONTAINING TRAIT.&amp;TraitValueType({TraitSet}{&#064;traitId } ) ENCODED BY der) }
 * }
 *
 * TRAIT ::= CLASS {
 *      &amp;id OBJECT IDENTIFIER UNIQUE,
 *      &amp;TraitValueType }
 *      WITH SYNTAX {
 *      SYNTAX &amp;TraitValueType
 *      IDENTIFIED BY &amp;id }
 *
 * TraitSet TRAIT ::= {...}
 * </pre>
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor(force = true)
public abstract class Trait<TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> extends ASN1Object {
    public static final int MIN_SEQUENCE_SIZE = 4;
    private static final int MAX_SEQUENCE_SIZE = 6;

    @NonNull
    private final Class<TraitType> traitType;
    @NonNull
    private final ASN1ObjectIdentifier traitId;
    @NonNull
    private final ASN1ObjectIdentifier traitCategory;
    @NonNull
    private final ASN1ObjectIdentifier traitRegistry;
    private final ASN1UTF8String description;
    private final ASN1IA5String descriptionURI;
    @NonNull
    private final TraitValueType traitValue;

    protected Trait(TraitBuilder<TraitValueType, TraitType, ?, ?> b) {
        this.traitType = b.traitType;
        this.traitId = b.traitId;
        this.traitCategory = b.traitCategory;
        this.traitRegistry = b.traitRegistry;
        this.description = b.description;
        this.descriptionURI = b.descriptionURI;
        this.traitValue = b.traitValue;
    }

    /**
     * Attempts to transform the provided object into a subclass of Trait given the supplied methods.
     *
     * @param obj              the object to parse
     * @param traitType        Class object type of Trait
     * @param conversionMethod method to convert generic traitValue to expected TraitValueType
     * @param <TraitValueType> the type of the traitValue
     * @param <TraitType>      the subclass of Trait expected that defines its traitValue to have type TraitValueType
     * @return Trait
     */
    public static final <TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> TraitType getInstance(Object obj, @NonNull Class<TraitType> traitType, @NonNull Function<ASN1Sequence, TraitType> conversionMethod) {
        if (obj == null) {
            return null;
        }

        if (traitType.isInstance(obj)) {
            return traitType.cast(obj);
        } else if (obj instanceof ASN1Sequence seq) {
            return conversionMethod.apply(seq);
        }

        throw new IllegalArgumentException("Cannot cast object to " + traitType.getName());
    }

    public static <TraitType extends Trait<?, ?>> TraitType getInstance(Object obj, Class<TraitType> clazz) {
        try {
            TraitType instance = clazz.getDeclaredConstructor().newInstance();
            return clazz.cast(instance.createInstance(obj));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz, e);
        }
    }


    /**
     * Common method to getInstance.
     * @param obj Object
     * @return Trait type
     */
    public abstract TraitType createInstance(Object obj);

    /**
     * @return This object as an ASN1Sequence
     */
    @Override
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(this.traitId);
        vec.add(this.traitCategory);
        vec.add(this.traitRegistry);
        if (this.description != null) {
            vec.add(new DERTaggedObject(false, 0, this.description));
        }
        if (this.descriptionURI != null) {
            vec.add(new DERTaggedObject(false, 1, this.descriptionURI));
        }
        vec.add(this.traitValue);
        return new DERSequence(vec);
    }

    public static abstract class TraitBuilder<TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>, C extends Trait<TraitValueType, TraitType>, B extends TraitBuilder<TraitValueType, TraitType, C, B>> {
        private @NonNull Class<TraitType> traitType;
        private @NonNull ASN1ObjectIdentifier traitId;
        private @NonNull ASN1ObjectIdentifier traitCategory;
        private @NonNull ASN1ObjectIdentifier traitRegistry;
        private ASN1UTF8String description;
        private ASN1IA5String descriptionURI;
        private @NonNull TraitValueType traitValue;

        private static <TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> void $fillValuesFromInstanceIntoBuilder(Trait<TraitValueType, TraitType> instance, TraitBuilder<TraitValueType, TraitType, ?, ?> b) {
            b.traitType(instance.traitType);
            b.traitId(instance.traitId);
            b.traitCategory(instance.traitCategory);
            b.traitRegistry(instance.traitRegistry);
            b.description(instance.description);
            b.descriptionURI(instance.descriptionURI);
            b.traitValue(instance.traitValue);
        }

        /**
         * Add any object to traitValue using the supplied conversion method.
         *
         * @param obj              the object to parse
         * @param conversionMethod method to convert generic traitValue to expected TraitValueType
         * @return TraitBuilder
         */
        public final B traitValueGetInstance(final Object obj, @NonNull final Function<Object, TraitValueType> conversionMethod) {
            this.traitValue = conversionMethod.apply(obj);
            return self();
        }

        /**
         * Parse elements from the given sequence. Convert the traitValue using the supplied method.
         *
         * @param seq              ASN1Sequence to parse
         * @param conversionMethod Function to convert the trait value in the sequence to a relevant TraitValueType.
         * @return TraitBuilder
         */
        public final B fromASN1Sequence(@NonNull final ASN1Sequence seq, @NonNull final Function<Object, TraitValueType> conversionMethod) {
            if (seq.size() < Trait.MIN_SEQUENCE_SIZE) {
                throw new IllegalArgumentException("Bad sequence size: " + seq.size());
            }

            List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

            this.traitId = ASN1ObjectIdentifier.getInstance(untaggedElements.get(0));
            this.traitCategory = ASN1ObjectIdentifier.getInstance(untaggedElements.get(1));
            this.traitRegistry = ASN1ObjectIdentifier.getInstance(untaggedElements.get(2));
            this.traitValueGetInstance(untaggedElements.get(3), conversionMethod);

            ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
                switch (key) {
                    case 0 -> this.description(DERUTF8String.getInstance(value));
                    case 1 -> this.descriptionURI(DERIA5String.getInstance(value));
                    default -> {
                    }
                }
            });
            return self();
        }

        /**
         * Copy all standard elements from the given trait.
         *
         * @param trait Trait object
         * @return TraitBuilder
         */
        public final B cloneTraitDescriptors(@NonNull final Trait<?, ?> trait) {
            this.traitId = trait.getTraitId();
            this.traitCategory = trait.getTraitCategory();
            this.traitRegistry = trait.getTraitRegistry();
            this.description = trait.getDescription();
            this.descriptionURI = trait.getDescriptionURI();
            return self();
        }

        public B traitType(@NonNull Class<TraitType> traitType) {
            this.traitType = traitType;
            return self();
        }

        public B traitId(@NonNull ASN1ObjectIdentifier traitId) {
            this.traitId = traitId;
            return self();
        }

        public B traitCategory(@NonNull ASN1ObjectIdentifier traitCategory) {
            this.traitCategory = traitCategory;
            return self();
        }

        public B traitRegistry(@NonNull ASN1ObjectIdentifier traitRegistry) {
            this.traitRegistry = traitRegistry;
            return self();
        }

        public B description(ASN1UTF8String description) {
            this.description = description;
            return self();
        }

        public B descriptionURI(ASN1IA5String descriptionURI) {
            this.descriptionURI = descriptionURI;
            return self();
        }

        public B traitValue(@NonNull TraitValueType traitValue) {
            this.traitValue = traitValue;
            return self();
        }

        protected B $fillValuesFrom(C instance) {
            TraitBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
            return self();
        }

        protected abstract B self();

        public abstract C build();

        public String toString() {
            return "Trait.TraitBuilder(super=" + super.toString() + ", traitType=" + this.traitType + ", traitId=" + this.traitId + ", traitCategory=" + this.traitCategory + ", traitRegistry=" + this.traitRegistry + ", description=" + this.description + ", descriptionURI=" + this.descriptionURI + ", traitValue=" + this.traitValue + ")";
        }
    }
}
