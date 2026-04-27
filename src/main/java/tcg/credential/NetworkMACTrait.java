package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * NetworkMACTrait TRAIT ::= {
 *      SYNTAX ComponentAddress
 *      IDENTIFIED BY tcg-tr-ID-networkMAC }
 *
 * A Trait that contains a MAC address using the NetworkMACTrait SHALL use tcg-tr-cat-networkMAC in its traitCategory field.
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class NetworkMACTrait extends Trait<ComponentAddress, NetworkMACTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return NetworkMACTrait
     */
    public static NetworkMACTrait getInstance(Object obj) {
        return Trait.getInstance(obj, NetworkMACTrait.class, NetworkMACTrait::fromASN1Sequence, NetworkMACTrait::fromJsonNode);
    }

    @Override
    public NetworkMACTrait createInstance(Object obj) {
        return NetworkMACTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return NetworkMACTrait
     */
    public static NetworkMACTrait fromASN1Sequence(ASN1Sequence seq) {
        return NetworkMACTrait.builder()
                .traitType(NetworkMACTrait.class)
                .fromASN1Sequence(seq, ComponentAddress::getInstance)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return NetworkMACTrait
     */
    public static NetworkMACTrait fromJsonNode(JsonNode node) {
        return NetworkMACTrait.builder()
                .traitType(NetworkMACTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ComponentAddress.class)
                .build();
    }

    /**
     * Initializes a builder with expected NetworkMACTrait metadata.
     * @return NetworkMACTrait Builder
     */
    public static NetworkMACTraitBuilder<?, ?> builder() {
        return new NetworkMACTraitBuilderImpl()
                .traitType(NetworkMACTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdNetworkMac)
                .traitCategory(TCGObjectIdentifier.tcgTrCatNetworkMac)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class NetworkMACTraitBuilder<C extends NetworkMACTrait, B extends NetworkMACTrait.NetworkMACTraitBuilder<C, B>> extends TraitBuilder<ComponentAddress, NetworkMACTrait, C, B> {
    }
}
