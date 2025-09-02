package factory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.GeneralName;
import tcg.credential.ASN1Utils;

/**
 * Functions to help manage the creation of the authority info access extension.
 */
public class AuthorityInfoAccessFactory {
    /**
     * fields of the authority info access object
     */
    public enum ElementJson {
        ACCESSMETHOD,
        ACCESSLOCATION;
    }
    
    /**
     * access method options
     */
    @AllArgsConstructor
    @Getter
    public enum MethodJson {
        OCSP(AccessDescription.id_ad_ocsp),
        CAISSUERS(AccessDescription.id_ad_caIssuers);
        
        private final ASN1ObjectIdentifier oid;
    }
    
    private final List<AccessDescription> elements;
    
    private AuthorityInfoAccessFactory() {
        elements = new ArrayList<>();
    }
    
    /**
     * Begin defining the authority info access extension.
     * @return A new AuthorityInfoAccessFactory builder.
     */
    public static final AuthorityInfoAccessFactory create() {
        return new AuthorityInfoAccessFactory();
    }
    
    /**
     * Add a method and location element.
     * @param element {@link AccessDescription}
     * @return The AuthorityInfoAccessFactory object with a new element added.
     */
    public final AuthorityInfoAccessFactory addElement(final AccessDescription element) {
        elements.add(element);
        return this;
    }
    
    /**
     * Add an element using the enumerated methods.
     * @param type {@link AuthorityInfoAccessFactory.MethodJson}
     * @param location {@link GeneralName}
     * @return The AuthorityInfoAccessFactory object with a new element added.
     */
    public final AuthorityInfoAccessFactory addElement(final MethodJson type, final GeneralName location) {
        elements.add(new AccessDescription(type.getOid(), location));
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link AuthorityInformationAccess}
     */
    public final AuthorityInformationAccess build() {
        return AuthorityInformationAccess.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(elements)));
    }

    public static final AuthorityInfoAccessFactory fromJsonNode(final JsonNode refNode) {
        AuthorityInfoAccessFactory aia = AuthorityInfoAccessFactory.create();
        boolean caseSens = false;

        if (refNode.isArray()) {
            JsonUtils.asStream(refNode.spliterator())
                .filter(elementNode -> JsonUtils.has(elementNode, caseSens, ElementJson.ACCESSMETHOD.name(), ElementJson.ACCESSLOCATION.name()))
                .forEach(elementNode -> {
                    Optional<JsonNode> methodNodeOpt = JsonUtils.get(elementNode, caseSens, ElementJson.ACCESSMETHOD.name());
                    Optional<JsonNode> locationNodeOpt = JsonUtils.get(elementNode, caseSens, ElementJson.ACCESSLOCATION.name());

                    methodNodeOpt.ifPresent(methodNode ->
                        locationNodeOpt.ifPresent(locationNode ->
                                aia.addElement(MethodJson.valueOf(methodNode.asText()), new GeneralName(new X500Name(locationNode.asText()))
                    )));
                });
        }

        return aia;
    }
}
