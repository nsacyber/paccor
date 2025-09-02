package factory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import tcg.credential.ComponentAddress;
import tcg.credential.ComponentIdentifier;
import tcg.credential.TCGObjectIdentifier;

/**
 * Functions to help manage the creation of a component identifier field.
 */
public class ComponentIdentifierFactory {
    /**
     * field names immediately under the component identifier object
     */
    public enum Json { 
        MANUFACTURER,
        MODEL,
        SERIAL,
        REVISION,
        MANUFACTURERID,
        FIELDREPLACEABLE,
        ADDRESSES,
        MAC;
    }
    
    private DERUTF8String componentManufacturer;
    private DERUTF8String componentModel;
    private DERUTF8String componentSerial;
    private DERUTF8String componentRevision;
    private ASN1ObjectIdentifier componentManufacturerId;
    private ASN1Boolean fieldReplaceable; 
    private ArrayList<ComponentAddress> componentAddress;
    
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
    
    private ComponentIdentifierFactory() {
        componentManufacturer = null;
        componentModel = null;
        componentSerial = null;
        componentRevision = null;
        componentManufacturerId = null;
        fieldReplaceable = ASN1Boolean.FALSE; // default to FALSE
        componentAddress = new ArrayList<>();
    }

    /**
     * Begin defining the component identifier object.
     * @return A new ComponentIdentifierFactory builder.
     */
    public static final ComponentIdentifierFactory create() {
        return new ComponentIdentifierFactory();
    }
    
    /**
     * Set the component manufacturer. Required field.
     * @param manufacturer String
     * @return The ComponentIdentifierFactory object with the component manufacturer set.
     */
    public final ComponentIdentifierFactory componentManufacturer(final String manufacturer) {
        componentManufacturer = new DERUTF8String(manufacturer);
        return this;
    }
    
    /**
     * Set the component model. Required field.
     * @param model String
     * @return The ComponentIdentifierFactory object with the component model set.
     */
    public final ComponentIdentifierFactory componentModel(final String model) {
        componentModel = new DERUTF8String(model);
        return this;
    }
    
    /**
     * Set the component serial number. Optional field.
     * @param serial String
     * @return The ComponentIdentifierFactory object with the component serial number set.
     */
    public final ComponentIdentifierFactory componentSerial(final String serial) {
        componentSerial = serial != null && !serial.trim().isEmpty() ? new DERUTF8String(serial) : null;
        return this;
    }
    
    /**
     * Set the component revision field. Optional field.
     * @param revision String
     * @return The ComponentIdentifierFactory object with the component revision set.
     */
    public final ComponentIdentifierFactory componentRevision(final String revision) {
        componentRevision = revision != null && !revision.trim().isEmpty() ? new DERUTF8String(revision) : null;
        return this;
    }
    
    /**
     * Set the component manufacturer oid. Optional field.
     * @param manufacturerId String
     * @return The ComponentIdentifierFactory object with the component manufacturer ID set.
     */
    public final ComponentIdentifierFactory componentManufacturerId(final String manufacturerId) {
        componentManufacturerId = manufacturerId != null && !manufacturerId.trim().isEmpty() ? new ASN1ObjectIdentifier(manufacturerId) : null;
        return this;
    }
    
    /**
     * Set the field replaceable flag. Optional field.
     * @param fieldReplaceable boolean
     * @return The ComponentIdentifierFactory object with the field replaceable bit set.
     */
    public final ComponentIdentifierFactory fieldReplaceable(final boolean fieldReplaceable) {
        this.fieldReplaceable = ASN1Boolean.getInstance(fieldReplaceable);
        return this;
    }
    
    /**
     * Since it is an optional field, the field replaceable flag could be unset.
     * @return The ComponentIdentifierFactory object with the field replaceable bit unset.
     */
    public final ComponentIdentifierFactory unsetFieldReplaceable() {
        this.fieldReplaceable = null;
        return this;
    }
    
    /**
     * Add a component address.
     * @param type {@link ComponentIdentifierFactory.ComponentAddressType} type of address as defined by the profile.
     * @param value String
     * @return The ComponentIdentifierFactory object with a component address added.
     */
    public final ComponentIdentifierFactory addComponentAddress(final ComponentAddressType type, final String value) {
        String filtered = value.toUpperCase();
        filtered.replaceAll("[:-]", "");
        componentAddress.add(new ComponentAddress(type.getOid(), new DERUTF8String(filtered)));
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link ComponentIdentifier}
     */
    public final ComponentIdentifier build() {
        ComponentIdentifier component =
                new ComponentIdentifier(
                        componentManufacturer,
                        componentModel,
                        componentSerial,
                        componentRevision,
                        componentManufacturerId,
                        fieldReplaceable,
                        componentAddress);
        return component;
    }
    
    /**
     * Create a new component description from a JSON object.
     * @param refNode JsonNode describing a component
     * @return {@link ComponentIdentifierFactory}
     */
    public static final ComponentIdentifierFactory fromJsonNode(final JsonNode refNode) {
        ComponentIdentifierFactory component = create();
        if (refNode.has(Json.MANUFACTURER.name()) && refNode.has(Json.MODEL.name())) {
            final JsonNode manufacturer = refNode.get(ComponentIdentifierFactory.Json.MANUFACTURER.name());
            final JsonNode model = refNode.get(ComponentIdentifierFactory.Json.MODEL.name());
            final JsonNode serial = refNode.get(ComponentIdentifierFactory.Json.SERIAL.name());
            final JsonNode revision = refNode.get(ComponentIdentifierFactory.Json.REVISION.name());
            final JsonNode manufacturerId = refNode.get(ComponentIdentifierFactory.Json.MANUFACTURERID.name());
            final JsonNode fieldReplaceable = refNode.get(ComponentIdentifierFactory.Json.FIELDREPLACEABLE.name());

            component
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
            
            JsonNode addresses = refNode.get(ComponentIdentifierFactory.Json.ADDRESSES.name());
            if (addresses != null && addresses.isArray()) {
                Iterator<JsonNode> addressMap = addresses.elements();
                while (addressMap.hasNext()) {
                    final JsonNode address = addressMap.next();
                    if (address.isObject()) {
                        final Iterator<Entry<String, JsonNode>> addressNodeMap = address.fields();
                        while (addressNodeMap.hasNext()) {
                            final Entry<String, JsonNode> addressNode = addressNodeMap.next();
                            ComponentIdentifierFactory.ComponentAddressType type = ComponentIdentifierFactory.ComponentAddressType.valueOf(addressNode.getKey());
                            String rawAddress = addressNode.getValue().asText().trim();
                            
                            if(rawAddress != null && !rawAddress.isEmpty()) {
                                String filtered = ComponentIdentifierV2Factory.standardizeMAC(rawAddress);
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
}
