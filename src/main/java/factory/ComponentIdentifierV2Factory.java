package factory;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.util.encoders.Base64;
import tcg.credential.AttributeCertificateIdentifier;
import tcg.credential.AttributeStatus;
import tcg.credential.CertificateIdentifier;
import tcg.credential.ComponentAddress;
import tcg.credential.ComponentClass;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.URIReference;

/**
 * Functions to help manage the creation of a component identifier field.
 */
public class ComponentIdentifierV2Factory {
    /**
     * field names immediately under the component identifier object
     */
    public enum Json { 
        COMPONENTCLASS,
        COMPONENTCLASSREGISTRY,
        COMPONENTCLASSVALUE,
        MANUFACTURER,
        MODEL,
        SERIAL,
        REVISION,
        MANUFACTURERID,
        FIELDREPLACEABLE,
        ADDRESSES,
        MAC,
        PLATFORMCERT,
        ATTRIBUTECERTIDENTIFIER,
        HASHALGORITHM,
        HASH,
        GENERICCERTIDENTIFIER,
        ISSUER,
        PLATFORMCERTURI,
        STATUS;
    }
    
    private ComponentClass componentClass;
    private DERUTF8String componentManufacturer;
    private DERUTF8String componentModel;
    private DERUTF8String componentSerial;
    private DERUTF8String componentRevision;
    private ASN1ObjectIdentifier componentManufacturerId;
    private ASN1Boolean fieldReplaceable; 
    private ArrayList<ComponentAddress> componentAddress;
    private CertificateIdentifier componentPlatformCert;
    private URIReference componentPlatformCertUri;
    private AttributeStatus status;
    
    public enum ComponentAddressType {
        ETHERNETMAC(TCGObjectIdentifier.tcgAddressEthernetMac),
        WLANMAC(TCGObjectIdentifier.tcgAddressWlanMac),
        BLUETOOTHMAC(TCGObjectIdentifier.tcgAddressBluetoothMac);
        
        private final ASN1ObjectIdentifier oid;
        
        private ComponentAddressType(final ASN1ObjectIdentifier oid) {
            this.oid = oid;
        }
        
        public final ASN1ObjectIdentifier getOid() {
            return oid;
        }
    }
    
    private ComponentIdentifierV2Factory() {
        componentClass = null;
        componentManufacturer = null;
        componentModel = null;
        componentSerial = null;
        componentRevision = null;
        componentManufacturerId = null;
        fieldReplaceable = ASN1Boolean.FALSE; // default to FALSE
        componentAddress = new ArrayList<>();
        componentPlatformCert = null;
        componentPlatformCertUri = null;
        status = null;
    }

    /**
     * Begin defining the component identifier object.
     * @return A new ComponentIdentifierV2Factory builder.
     */
    public static final ComponentIdentifierV2Factory create() {
        return new ComponentIdentifierV2Factory();
    }
    
    /**
     * Set the component class. Required field.
     * @param componentClass ComponentClass
     * @return The ComponentIdentifierV2Factory object with the component class set.
     */
    public final ComponentIdentifierV2Factory componentClass(final ComponentClass componentClass) {
        this.componentClass = componentClass;
        return this;
    }
    
    /**
     * Set the component manufacturer. Required field.
     * @param manufacturer String
     * @return The ComponentIdentifierV2Factory object with the component manufacturer set.
     */
    public final ComponentIdentifierV2Factory componentManufacturer(final String manufacturer) {
        componentManufacturer = new DERUTF8String(manufacturer);
        return this;
    }
    
    /**
     * Set the component model. Required field.
     * @param model String
     * @return The ComponentIdentifierV2Factory object with the component model set.
     */
    public final ComponentIdentifierV2Factory componentModel(final String model) {
        componentModel = new DERUTF8String(model);
        return this;
    }
    
    /**
     * Set the component serial number. Optional field.
     * @param serial String
     * @return The ComponentIdentifierV2Factory object with the component serial number set.
     */
    public final ComponentIdentifierV2Factory componentSerial(final String serial) {
        componentSerial = serial != null && !serial.trim().isEmpty() ? new DERUTF8String(serial) : null;
        return this;
    }
    
    /**
     * Set the component revision field. Optional field.
     * @param revision String
     * @return The ComponentIdentifierV2Factory object with the component revision set.
     */
    public final ComponentIdentifierV2Factory componentRevision(final String revision) {
        componentRevision = revision != null && !revision.trim().isEmpty() ? new DERUTF8String(revision) : null;
        return this;
    }
    
    /**
     * Set the component manufacturer oid. Optional field.
     * @param manufacturerId String
     * @return The ComponentIdentifierV2Factory object with the component manufacturer ID set.
     */
    public final ComponentIdentifierV2Factory componentManufacturerId(final String manufacturerId) {
        componentManufacturerId = manufacturerId != null && !manufacturerId.trim().isEmpty() ? new ASN1ObjectIdentifier(manufacturerId) : null;
        return this;
    }
    
    /**
     * Set the field replaceable flag. Optional field.
     * @param fieldReplaceable boolean
     * @return The ComponentIdentifierV2Factory object with the field replaceable bit set.
     */
    public final ComponentIdentifierV2Factory fieldReplaceable(final boolean fieldReplaceable) {
        this.fieldReplaceable = ASN1Boolean.getInstance(fieldReplaceable);
        return this;
    }
    
    /**
     * Since it is an optional field, the field replaceable flag could be unset.
     * @return The ComponentIdentifierV2Factory object with the field replaceable bit unset.
     */
    public final ComponentIdentifierV2Factory unsetFieldReplaceable() {
        this.fieldReplaceable = null;
        return this;
    }
    
    /**
     * Add a component address.
     * @param type {@link ComponentIdentifierV2Factory.ComponentAddressType} type of address as defined by the profile.
     * @param value String
     * @return The ComponentIdentifierV2Factory object with a component address added.
     */
    public final ComponentIdentifierV2Factory addComponentAddress(final ComponentAddressType type, final String value) {
        componentAddress.add(new ComponentAddress(type.getOid(), new DERUTF8String(value)));
        return this;
    }
    
    /**
     * Add the component platform certificate. Optional field.
     * @param ci {@link CertificateIdentifier}
     * @return The ComponentIdentifierV2Factory object with the component platform certificate set.
     */
    public final ComponentIdentifierV2Factory componentPlatformCert(CertificateIdentifier ci) {
        componentPlatformCert = ci;
        return this;
    }
    
    /**
     * Add the component platform certificate. Optional field.
     * @param ref {@link URIReference}
     * @return The ComponentIdentifierV2Factory object with the component platform certificate uri set.
     */
    public final ComponentIdentifierV2Factory componentPlatformCertUri(URIReference ref) {
        componentPlatformCertUri = ref;
        return this;
    }
    
    /**
     * Set the attribute status. Optional field.
     * @param option {@link AttributeStatus}
     * @return The ComponentIdentifierV2Factory object with the status set.
     */
    public final ComponentIdentifierV2Factory status(final AttributeStatus option) {
        status = option;
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link ComponentIdentifierV2}
     */
    public final ComponentIdentifierV2 build() {
        ComponentIdentifierV2 component =
                new ComponentIdentifierV2(componentClass,
                        componentManufacturer,
                        componentModel,
                        componentSerial,
                        componentRevision,
                        componentManufacturerId,
                        fieldReplaceable,
                        componentAddress.toArray(new ComponentAddress[componentAddress.size()]),
                        componentPlatformCert,
                        componentPlatformCertUri,
                        status);
        return component;
    }
    
    /**
     * Create a new component description from a JSON object.
     * @param refNode JsonNode describing a component
     * @return {@link ComponentIdentifierV2Factory}
     */
    public static final ComponentIdentifierV2Factory fromJsonNode(final JsonNode refNode) {
        ComponentIdentifierV2Factory component = create();
        if (refNode.has(Json.COMPONENTCLASS.name()) && refNode.has(Json.MANUFACTURER.name()) && refNode.has(Json.MODEL.name())) {
            final JsonNode componentClass = refNode.get(ComponentIdentifierV2Factory.Json.COMPONENTCLASS.name());
            final JsonNode componentClassRegistry = componentClass.get(ComponentIdentifierV2Factory.Json.COMPONENTCLASSREGISTRY.name());
            final JsonNode componentClassValue = componentClass.get(ComponentIdentifierV2Factory.Json.COMPONENTCLASSVALUE.name());
            final JsonNode manufacturer = refNode.get(ComponentIdentifierV2Factory.Json.MANUFACTURER.name());
            final JsonNode model = refNode.get(ComponentIdentifierV2Factory.Json.MODEL.name());
            final JsonNode serial = refNode.get(ComponentIdentifierV2Factory.Json.SERIAL.name());
            final JsonNode revision = refNode.get(ComponentIdentifierV2Factory.Json.REVISION.name());
            final JsonNode manufacturerId = refNode.get(ComponentIdentifierV2Factory.Json.MANUFACTURERID.name());
            final JsonNode fieldReplaceable = refNode.get(ComponentIdentifierV2Factory.Json.FIELDREPLACEABLE.name());
            final JsonNode platformCert = refNode.get(ComponentIdentifierV2Factory.Json.PLATFORMCERT.name());
            final JsonNode platformCertUri = refNode.get(ComponentIdentifierV2Factory.Json.PLATFORMCERTURI.name());
            final JsonNode status = refNode.get(ComponentIdentifierV2Factory.Json.STATUS.name());

            component
            .componentClass(new ComponentClass(new ASN1ObjectIdentifier(componentClassRegistry.asText()), componentClassValue.asText()))
            .componentManufacturer(manufacturer.asText())
            .componentModel(model.asText())
            // all other fields are optional or have default values
            .componentSerial(serial != null ? serial.asText() : null)
            .componentRevision(revision != null ? revision.asText() : null)
            .componentManufacturerId(manufacturerId != null ? manufacturerId.asText() : null);
            
            
            if (fieldReplaceable == null) {
                component.unsetFieldReplaceable();
            } else {
                component.fieldReplaceable(fieldReplaceable != null ? fieldReplaceable.asBoolean() : false);
            }
            
            if (platformCert != null && platformCert.has(ComponentIdentifierV2Factory.Json.ATTRIBUTECERTIDENTIFIER.name()) && platformCert.has(ComponentIdentifierV2Factory.Json.GENERICCERTIDENTIFIER.name())) {
                final JsonNode attributeCertNode = platformCert.get(ComponentIdentifierV2Factory.Json.ATTRIBUTECERTIDENTIFIER.name());
                final JsonNode hashAlgNode = attributeCertNode.get(ComponentIdentifierV2Factory.Json.HASHALGORITHM.name());
                final JsonNode hashNode = attributeCertNode.get(ComponentIdentifierV2Factory.Json.HASH.name());
                final String hashAlg = hashAlgNode != null ? hashAlgNode.asText() : "";
                final String hash = hashNode != null ? hashNode.asText() : "";
                AttributeCertificateIdentifier aci = null;
                if (!hashAlg.isEmpty() && !hash.isEmpty()) {
                    aci = new AttributeCertificateIdentifier(
                            new AlgorithmIdentifier(new ASN1ObjectIdentifier(hashAlg)),
                            new DEROctetString(Base64.decode(hash)));
                }
                
                final JsonNode genericCertNode = platformCert.get(ComponentIdentifierV2Factory.Json.GENERICCERTIDENTIFIER.name());
                final JsonNode issuerNode = genericCertNode.get(ComponentIdentifierV2Factory.Json.ISSUER.name());
                final JsonNode serialNode = genericCertNode.get(ComponentIdentifierV2Factory.Json.SERIAL.name());
                final String issuer = issuerNode != null ? issuerNode.asText() : "";
                final String genericCertSerial = serialNode != null ? serialNode.asText() : "";
                IssuerSerial issuerSerial = new IssuerSerial(new X500Name(issuer), new BigInteger(genericCertSerial));
                component.componentPlatformCert(new CertificateIdentifier(aci, issuerSerial));
            }
            
            if (platformCertUri != null) {
                URIReferenceFactory urif = URIReferenceFactory.fromJsonNode(platformCertUri);
                component.componentPlatformCertUri(urif.build());
            }
            
            if (status != null) {
                component.status(new AttributeStatus(status.asText()));
            }
            
            JsonNode addresses = refNode.get(ComponentIdentifierV2Factory.Json.ADDRESSES.name());
            if (addresses != null && addresses.isArray()) {
                Iterator<JsonNode> addressMap = addresses.elements();
                while (addressMap.hasNext()) {
                    final JsonNode address = addressMap.next();
                    if (address.isObject()) {
                        final Iterator<Entry<String, JsonNode>> addressNodeMap = address.fields();
                        while (addressNodeMap.hasNext()) {
                            final Entry<String, JsonNode> addressNode = addressNodeMap.next();
                            ComponentIdentifierV2Factory.ComponentAddressType type = ComponentIdentifierV2Factory.ComponentAddressType.valueOf(addressNode.getKey());
                            String rawAddress = addressNode.getValue().asText().trim();
                            
                            if(serial != null && rawAddress != null && !rawAddress.isEmpty()) {
                                String filtered = standardizeMAC(rawAddress);
                                component.addComponentAddress(type, filtered);
                                if(rawAddress.equalsIgnoreCase(serial.asText())) {
                                    component.componentSerial(filtered);
                                }
                            }
                            break; // remove the break to loosen rules regarding json structure of MAC addrs
                            // as it is, there should be one address definition per object
                        }
                    }
                }
            }
        }
        
        return component;
    }
    
    public static final String standardizeMAC(final String address) {
        String filtered = address.toUpperCase();
        filtered = filtered.replaceAll("[:-]", "");
        return filtered;
    }
}
