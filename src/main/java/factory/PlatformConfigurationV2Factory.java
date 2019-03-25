package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.bouncycastle.asn1.DERUTF8String;
import tcg.credential.AttributeStatus;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.PlatformPropertiesV2;
import tcg.credential.URIReference;

/**
 * Functions to help manage the creation of the platform configuration attribute.
 */
public class PlatformConfigurationV2Factory {
    /**
     * field names immediately under the platform configuration object
     */
    public enum Json {
        COMPONENTS,
        PROPERTIES,
        COMPONENTSURI,
        PROPERTIESURI,
        // these options are under the properties field.
        PROPERTYNAME,
        PROPERTYVALUE,
        PROPERTYSTATUS;
    }

    private ArrayList<ComponentIdentifierV2> platformConfigComponents;
    private ArrayList<PlatformPropertiesV2> platformConfigProperties;
    private URIReference componentIdentifiersUri;
    private URIReference platformPropertiesUri;
    
    private PlatformConfigurationV2Factory() {
        this.platformConfigComponents = new ArrayList<>();
        this.platformConfigProperties = new ArrayList<>();
        this.platformPropertiesUri = null;
        this.componentIdentifiersUri = null;
    }
    
    /**
     * Begin defining the platform configuration attribute.
     */
    public static final PlatformConfigurationV2Factory create() {
        return new PlatformConfigurationV2Factory();
    }
    
    /**
     * Add a component definition.
     * @param component {@link ComponentIdentifier}
     * @see ComponentIdentifierFactory
     */
    public final PlatformConfigurationV2Factory addComponent(final ComponentIdentifierV2 component) {
        platformConfigComponents.add(component);
        return this;
    }
    
    /**
     * Add a new platform property.
     * @param name String property name
     * @param value String property value
     * @see PlatformProperties
     */
    public final PlatformConfigurationV2Factory addProperty(final String name, final String value) {
        return addProperty(name, value, null);
    }
    
    /**
     * Add a new platform property.
     * @param name String property name
     * @param value String property value
     * @param status AttributeStatus property status
     * @see PlatformProperties
     */
    public final PlatformConfigurationV2Factory addProperty(final String name, final String value, final AttributeStatus status) {
        platformConfigProperties.add(new PlatformPropertiesV2(new DERUTF8String(name), new DERUTF8String(value), status));
        return this;
    }
    
    /**
     * Set the components URI.
     * @param uriRef {@link URIReference}
     */
    public final PlatformConfigurationV2Factory setComponentsUri(final URIReference uriRef) {
        componentIdentifiersUri = uriRef;
        return this;
    }
    
    /**
     * Set the properties URI.
     * @param uriRef {@link URIReference}
     */
    public final PlatformConfigurationV2Factory setPropertiesUri(final URIReference uriRef) {
        platformPropertiesUri = uriRef;
        return this;
    }
    
    public final boolean isStatusUsed() {
        boolean status = false;
        for (int i = 0; i < platformConfigComponents.size() && !status; i++) {
            status = platformConfigComponents.get(i).getStatus() != null;
        }
        for (int i = 0; i < platformConfigProperties.size() && !status; i++) {
            status = platformConfigProperties.get(i).getStatus() != null;
        }
        
        return status;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link PlatformConfiguration}
     */
    public final PlatformConfigurationV2 build() {
        // All parameters to this object are optional.
        // Go with whatever has been given to the factory
        PlatformConfigurationV2 pConfig = new PlatformConfigurationV2(
                                              this.platformConfigComponents,
                                              this.componentIdentifiersUri,
                                              this.platformConfigProperties,
                                              this.platformPropertiesUri);
        return pConfig;
    }
    
    /**
     * Read a file for JSON data to incorporate into the platform configuration.
     * @param jsonFile String file to read containing JSON data
     * @see PlatformConfigurationV2Factory.Json
     */
    public final PlatformConfigurationV2Factory addDataFromJsonFile(final String jsonFile) {
        try {
            final String jsonData = new String(Files.readAllBytes(Paths.get(jsonFile)));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonData);
            if (root.has(Json.COMPONENTS.name())) {
                final JsonNode componentsNode = root.get(Json.COMPONENTS.name());
                addComponentsFromJsonNode(componentsNode);
            }
            
            if (root.has(Json.COMPONENTSURI.name())) {
                final JsonNode componentsUriNode = root.get(Json.COMPONENTSURI.name());
                setComponentsUriFromJsonNode(componentsUriNode);
            }
            
            if (root.has(Json.PROPERTIES.name())) {
                final JsonNode propertiesNode = root.get(Json.PROPERTIES.name());
                addPropertiesFromJsonNode(propertiesNode);
            }
            
            if (root.has(Json.PROPERTIESURI.name())) {
                final JsonNode propertiesUriNode = root.get(Json.PROPERTIESURI.name());
                setPropertiesUriFromJsonNode(propertiesUriNode);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } 
        return this;
    }
    
    /**
     * Parse the JSON objects for component data.
     * @param refNode JsonNode with {@link ComponentIdentifier} data
     * @see ComponentIdentifierFactory.Json
     */
    public final PlatformConfigurationV2Factory addComponentsFromJsonNode(final JsonNode refNode) {
        if (refNode.isArray()) {
            for (final JsonNode part : refNode) {
                ComponentIdentifierV2Factory component = ComponentIdentifierV2Factory.fromJsonNode(part);
                addComponent(component.build());
            }
        }
        return this;
    }
    
    /**
     * Parse the JSON object for components URI data.
     * @param refNode JsonNode with the properties URI data
     * @see URIReferenceFactory.Json
     */
    public final PlatformConfigurationV2Factory setComponentsUriFromJsonNode(final JsonNode refNode) {
        URIReferenceFactory urif = URIReferenceFactory.fromJsonNode(refNode);   
        setPropertiesUri(urif.build());
        return this;
    }
    
    /**
     * Parse the JSON objects for properties data.
     * @param refNode JsonNode with {@link PlatformProperties} data
     * @see PlatformConfigurationV2Factory.Json
     */
    public final PlatformConfigurationV2Factory addPropertiesFromJsonNode(final JsonNode refNode) {
        if (refNode.isArray()) {
            for (final JsonNode property : refNode) {
                if (property.has(Json.PROPERTYNAME.name()) && property.has(Json.PROPERTYVALUE.name())) {
                    final JsonNode nameNode = property.get(Json.PROPERTYNAME.name());
                    final JsonNode valueNode = property.get(Json.PROPERTYVALUE.name());
                    final JsonNode statusNode = property.has(Json.PROPERTYSTATUS.name()) ? property.get(Json.PROPERTYSTATUS.name()) : null;
                    
                    final String name = nameNode.asText();
                    final String value = valueNode.asText();
                    
                    if (!name.isEmpty() && !value.isEmpty()) {
                        if (statusNode != null) {
                            AttributeStatus status = new AttributeStatus(statusNode.asText());
                            addProperty(name, value, status);
                        } else {
                            addProperty(name, value);
                        }
                    }
                }
            }
        }
        return this;
    }
    
    /**
     * Parse the JSON object for properties URI data.
     * @param refNode JsonNode with the properties URI data
     * @see URIReferenceFactory.Json
     */
    public final PlatformConfigurationV2Factory setPropertiesUriFromJsonNode(final JsonNode refNode) {
        URIReferenceFactory urif = URIReferenceFactory.fromJsonNode(refNode);   
        setPropertiesUri(urif.build());
        return this;
    }
}
