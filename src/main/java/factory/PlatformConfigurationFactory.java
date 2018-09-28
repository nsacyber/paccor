package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.bouncycastle.asn1.DERUTF8String;
import tcg.credential.ComponentIdentifier;
import tcg.credential.PlatformConfiguration;
import tcg.credential.PlatformProperties;
import tcg.credential.URIReference;

/**
 * Functions to help manage the creation of the platform configuration attribute.
 */
public class PlatformConfigurationFactory {
    /**
     * field names immediately under the platform configuration object
     */
    public enum Json {
        COMPONENTS,
        PROPERTIES,
        PROPERTIESURI,
        // these options are under the properties field.
        // not worth creating a factory for a simple key+value pair
        PROPERTYNAME,
        PROPERTYVALUE;
    }

    private ArrayList<ComponentIdentifier> platformConfigComponents;
    private ArrayList<PlatformProperties> platformConfigProperties;
    private URIReference uriReference;
    
    private PlatformConfigurationFactory() {
        this.platformConfigComponents = new ArrayList<>();
        this.platformConfigProperties = new ArrayList<>();
        this.uriReference = null;
    }
    
    /**
     * Begin defining the platform configuration attribute.
     */
    public static final PlatformConfigurationFactory create() {
        return new PlatformConfigurationFactory();
    }
    
    /**
     * Add a component definition.
     * @param component {@link ComponentIdentifier}
     * @see ComponentIdentifierFactory
     */
    public final PlatformConfigurationFactory addComponent(final ComponentIdentifier component) {
        platformConfigComponents.add(component);
        return this;
    }
    
    /**
     * Add a new platform property.
     * @param name String property name
     * @param value String property value
     * @see PlatformProperties
     */
    public final PlatformConfigurationFactory addProperty(final String name, final String value) {
        platformConfigProperties.add(new PlatformProperties(new DERUTF8String(name), new DERUTF8String(value)));
        return this;
    }
    
    /**
     * Set the properties URI.
     * @param uriRef {@link URIReference}
     */
    public final PlatformConfigurationFactory setPropertiesUri(final URIReference uriRef) {
        uriReference = uriRef;
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link PlatformConfiguration}
     */
    public final PlatformConfiguration build() {
        // All parameters to this object are optional.
        // Go with whatever has been given to the factory
        PlatformConfiguration pConfig = new PlatformConfiguration(
                                              this.platformConfigComponents,
                                              this.platformConfigProperties,
                                              this.uriReference);
        return pConfig;
    }
    
    /**
     * Read a file for JSON data to incorporate into the platform configuration.
     * @param jsonFile String file to read containing JSON data
     * @see PlatformConfigurationFactory.Json
     */
    public final PlatformConfigurationFactory addDataFromJsonFile(final String jsonFile) {
        try {
            final String jsonData = new String(Files.readAllBytes(Paths.get(jsonFile)));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonData);
            if (root.has(Json.COMPONENTS.name())) {
                final JsonNode componentsNode = root.get(Json.COMPONENTS.name());
                addComponentsFromJsonNode(componentsNode);
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
    public final PlatformConfigurationFactory addComponentsFromJsonNode(final JsonNode refNode) {
        if (refNode.isArray()) {
            for (final JsonNode part : refNode) {
                ComponentIdentifierFactory component = ComponentIdentifierFactory.fromJsonNode(part);
                addComponent(component.build());
            }
        }
        return this;
    }
    
    /**
     * Parse the JSON objects for properties data.
     * @param refNode JsonNode with {@link PlatformProperties} data
     * @see PlatformConfigurationFactory.Json
     */
    public final PlatformConfigurationFactory addPropertiesFromJsonNode(final JsonNode refNode) {
        if (refNode.isArray()) {
            for (final JsonNode property : refNode) {
                if (property.has(Json.PROPERTYNAME.name()) && property.has(Json.PROPERTYVALUE.name())) {
                    final JsonNode nameNode = property.get(Json.PROPERTYNAME.name());
                    final JsonNode valueNode = property.get(Json.PROPERTYVALUE.name());
                    
                    final String name = nameNode.asText();
                    final String value = valueNode.asText();
                    
                    if (!name.isEmpty() && !value.isEmpty()) {
                        addProperty(name, value);
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
    public final PlatformConfigurationFactory setPropertiesUriFromJsonNode(final JsonNode refNode) {
        URIReferenceFactory urif = URIReferenceFactory.fromJsonNode(refNode);   
        setPropertiesUri(urif.build());
        return this;
    }
}
