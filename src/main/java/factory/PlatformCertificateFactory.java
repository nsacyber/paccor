package factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.encoders.Base64;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import json.IntermediateInfoBean;
import json.PolicyReferenceJsonHelper;
import json.PolicyReferenceJsonHelper.PolicyRefJson;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.TBBSecurityAssertions;
import tcg.credential.TCGCredentialType;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TCGPlatformSpecification;
import tcg.credential.TCGSpecificationVersion;
import tcg.credential.URIReference;

/**
 * Functions to help manage the construction of a platform credential.
 */
public class PlatformCertificateFactory {
    /**
     * Extension criticality as defined in the profile.
     */
    public static final Map<ASN1ObjectIdentifier, Boolean> criticalExtensions =
            Collections.unmodifiableMap(new Hashtable<ASN1ObjectIdentifier, Boolean>()
            {
                private static final long serialVersionUID = 1L;
            {
                put(Extension.authorityKeyIdentifier, Boolean.FALSE); // Authority Key Identifier
                put(Extension.certificatePolicies, Boolean.FALSE); // Certificate Policies
                put(Extension.authorityInfoAccess, Boolean.FALSE); // Authority Info Access
                put(Extension.cRLDistributionPoints, Boolean.FALSE); // CRL Distribution
                put(Extension.subjectAlternativeName, Boolean.FALSE); // Subject Alternative Names
                put(Extension.targetInformation, Boolean.TRUE); // Targeting Information
            }});
    
    private boolean delta;
    private Holder holder;
    private AttributeCertificateIssuer issuer;
    private BigInteger serialNumber;
    private Date notBefore;
    private Date notAfter;
    private Hashtable<ASN1ObjectIdentifier, ASN1Object> attributes;
    private Hashtable<ASN1ObjectIdentifier, Extension> extensions;
    
    private PlatformCertificateFactory() {
        delta = false;
        holder = null;
        issuer = null;
        serialNumber = null;
        attributes = new Hashtable<ASN1ObjectIdentifier, ASN1Object>();
        extensions = new Hashtable<ASN1ObjectIdentifier, Extension>();
    }
    
    /**
     * Begin creating a new platform certificate.
     * @return A new PlatformCertificateFactory builder.
     */
    public static final PlatformCertificateFactory create() {
        return new PlatformCertificateFactory();
    }
    
    /**
     * Reply with whether the factory will produce a delta platform certificate.
     * @return True if the object contains data representing a delta platform certificate.
     */
    public final boolean isDeltaCertificate() {
        return delta;
    }
    
    /**
     * Create a delta platform certificate.
     * @return The PlatformCertificateFactory object with the delta certificate flag set.
     */
    public final PlatformCertificateFactory setDeltaCertificate() {
        delta = true;
        return this;
    }
    
    /**
     * Set the holder.
     * @param holder {@link Holder}
     * @return The PlatformCertificateFactory object with the holder set.
     */
    public final PlatformCertificateFactory holder(final Holder holder) {
        this.holder = holder;
        return this;
    }
    
    /**
     * Set the issuer of the platform credential.
     * @param issuer {@link AttributeCertificateIssuer}
     * @return The PlatformCertificateFactory object with the issuer set.
     */
    public final PlatformCertificateFactory issuer(final AttributeCertificateIssuer issuer) {
        this.issuer = issuer;
        return this;
    }
    
    /**
     * Set the serial number.
     * @param serialNumber {@link BigInteger}
     * @return The PlatformCertificateFactory object with the serial number set.
     */
    public final PlatformCertificateFactory serialNumber(final BigInteger serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }
    
    /**
     * Set the valid not before date.
     * @param notBefore {@link Date}
     * @return The PlatformCertificateFactory object with the not before date set.
     */
    public final PlatformCertificateFactory notBefore(final Date notBefore) {
        this.notBefore = notBefore;
        return this;
    }
    
    /**
     * Set the valid not after date.
     * @param notAfter {@link Date}
     * @return The PlatformCertificateFactory object with the not after date set.
     */
    public final PlatformCertificateFactory notAfter(final Date notAfter) {
        this.notAfter = notAfter;
        return this;
    }
    
    /**
     * Set the TCG Platform Specification attribute.
     * @param tps {@link TCGPlatformSpecification}
     * @return The PlatformCertificateFactory object with the TCG platform specification set.
     */
    public final PlatformCertificateFactory tcgPlatformSpecification(final TCGPlatformSpecification tps) {
        attributes.put(TCGObjectIdentifier.tcgAtTcgPlatformSpecification, tps);
        return this;
    }
    
    /**
     * Set the TCG Certificate Specification attribute.
     * @param tcs {@link TCGSpecificationVersion}
     * @return The PlatformCertificateFactory object with the TCG certificate specification set.
     */
    public final PlatformCertificateFactory tcgCertificateSpecification(final TCGSpecificationVersion tcs) {
        attributes.put(TCGObjectIdentifier.tcgAtTcgCertificateSpecification, tcs);
        return this;
    }
    
    /**
     * @deprecated Use tcgCertificateSpecification
     * Set the TCG Credential Specification attribute.
     * @param tcs {@link TCGSpecificationVersion}
     * @return The PlatformCertificateFactory object with the TCG certificate specification set.
     */
    public final PlatformCertificateFactory tcgCredentialSpecification(final TCGSpecificationVersion tcs) {
        attributes.put(TCGObjectIdentifier.tcgAtTcgCredentialSpecification, tcs);
        return this;
    }
    
    /**
     * Set the platform configuration attribute.
     * @param pConfig {@link PlatformConfigurationV2}
     * @see PlatformConfigurationFactory
     * @return The PlatformCertificateFactory object with the platform configuration set.
     */
    public final PlatformCertificateFactory platformConfiguration(final PlatformConfigurationV2 pConfig) {
        attributes.put(TCGObjectIdentifier.tcgAtPlatformConfigurationV2, pConfig);
        return this;
    }
    
    /**
     * Set the TBB Security Assertions attribute.
     * @param assertions {@link TBBSecurityAssertions}
     * @see TBBSecurityAssertionsFactory
     * @return The PlatformCertificateFactory object with the TBB security assertions set.
     */
    public final PlatformCertificateFactory tbbSecurityAssertions(final TBBSecurityAssertions assertions) {
        attributes.put(TCGObjectIdentifier.tcgAtTbbSecurityAssertions, assertions);
        return this;
    }
    
    /**
     * Set the platform configuration URI attribute.
     * @param uri {@link URIReference}
     * @return The PlatformCertificateFactory object with the platform config URI set.
     */
    public final PlatformCertificateFactory platformConfigUri(final URIReference uri) {
        attributes.put(TCGObjectIdentifier.tcgAtPlatformConfigUri, uri);
        return this;
    }
    
    /**
     * Add an extension.
     * @param ext {@link Extension}
     * @return The PlatformCertificateFactory object with an extension added.
     */
    public final PlatformCertificateFactory addExtension(Extension ext) {
        extensions.put(ext.getExtnId(), ext);
        return this;
    }
    
    /**
     * Create a new extension.  The criticality will be automatically set according to the profile.  
     * If the oid is not known, the extension will be set to non-critical.
     * @param oid {@link ASN1ObjectIdentifier} of the extension
     * @param ext {@link Encodable} the extension object
     * @return The PlatformCertificateFactory object with an extension added.
     * @throws IOException see {@link Encodable#getEncoded}
     */
    public final PlatformCertificateFactory addExtension(final ASN1ObjectIdentifier oid, final Encodable ext) throws IOException {
        boolean isCritical = false;
        if (criticalExtensions.containsKey(oid)) {
            isCritical = criticalExtensions.get(oid).booleanValue();
        }
        extensions.put(oid, new Extension(oid, isCritical, ext.getEncoded()));
        return this;
    }
    
    /**
     * Base Platform Attribute Certificate structure:
     *   Holder (required)
     *   Issuer (required)
     *   Serial Number (required)
     *   Validity not before (required)
     *   Validity not after (required)
     *   Attributes:
     *    TCG Platform Specification (should)
     *    TCG Certificate Specification (should)
     *    TBB Security Assertions (should)
     *    TCG Certificate Type (should)
     *    Platform Configuration (may)
     *    Platform Configuration URI (may)
     *   Extensions: 
     *    Certificate Policies (must, non-critical) 
     *    Subject Alternative Names (must, non-critical)
     *    Authority Key Identifier (must, non-critical)
     *    Authority Info Access (should, non-critical)
     *    CRL Distribution (may, non-critical)
     *    Targeting Information (may, critical)
     *   Issuer Unique ID (SHOULD NOT)
     *   Signature Algorithm (required)
     *   Signature (required)
     * @param signer something that implements {@link ContentSigner}
     * @return {@link X509AttributeCertificateHolder}
     */
    public final X509AttributeCertificateHolder build(ContentSigner signer) {
        AttributeCertificateHolder ach = null;
        
        // TODO
        // Replace this with another way to convert Holder to AttributeCertificateHolder.
        // AttributeCertificateHolder converts the data and stores it as a holder anyway
        // The constructor is simply private
        try {
            Class<AttributeCertificateHolder> clazz = AttributeCertificateHolder.class;
            Constructor<AttributeCertificateHolder> constructor;
        
            constructor = clazz.getDeclaredConstructor(ASN1Sequence.class);
            constructor.setAccessible(true); 
            ach = constructor.newInstance((ASN1Sequence)holder.toASN1Primitive());
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // Begin building the cert with values stored in this class
        X509v2AttributeCertificateBuilder xacb = new X509v2AttributeCertificateBuilder(ach, issuer, serialNumber, notBefore, notAfter);
        
        ASN1ObjectIdentifier credentialType = TCGObjectIdentifier.tcgKpPlatformAttributeCertificate;
        if (delta) {
            credentialType = TCGObjectIdentifier.tcgKpDeltaPlatformAttributeCertificate;
            
            // Remove attributes which cannot be inside a delta platform certificate
            attributes.remove(TCGObjectIdentifier.tcgAtTbbSecurityAssertions);
            attributes.remove(TCGObjectIdentifier.tcgAtTcgPlatformSpecification);
        }
        attributes.put(TCGObjectIdentifier.tcgAtTcgCertificateType, new TCGCredentialType(credentialType));
        
        // add in all attributes
        for (final ASN1ObjectIdentifier oid : attributes.keySet()) {
            xacb.addAttribute(oid, attributes.get(oid));
        }
        
        // add in all extensions
        for (final ASN1ObjectIdentifier oid : extensions.keySet()) {
            try {
                xacb.addExtension(extensions.get(oid));
            } catch (CertIOException e) {
                // ignored, worthless exception
            }
        }
        
        // Build the cert and sign it
        X509AttributeCertificateHolder xach = xacb.build(signer);

        return xach;
    }
    
    /**
     * Serializes the PlatformCertificateFactory into a JSON String.
     * @throws JsonProcessingException If there is an issue with serialization.
     * @return The PlatformCertificateFactory object as a String.
     */
    public final String toJson() throws JsonProcessingException {
        IntermediateInfoBean iib = new IntermediateInfoBean();
        
        try {
            iib.setHolder(new String(Base64.encode(holder.getEncoded("DER"))));
            iib.setDelta(delta);
            for (ASN1ObjectIdentifier oid : attributes.keySet()) {
                iib.addAttribute(oid, attributes.get(oid));
            }
            for (ASN1ObjectIdentifier oid : extensions.keySet()) {
                iib.addExtension(oid, extensions.get(oid));
            }
        } catch (IOException e) {
            
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter();
        
        return mapper.writeValueAsString(iib);
    }
    
    /**
     * Converts data from JSON into a PlatformCertificateFactory builder.
     * @param filename The filename of a file containing JSON data.
     * @return A new PlatformCertificateFactory filled out with information from the file.
     * @throws IOException If there is a problem reading the file or with the JSON data inside the file.
     */
    public static final PlatformCertificateFactory loadIntermediateInfofromJson(final String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        IntermediateInfoBean iib = mapper.readValue(new File(filename), IntermediateInfoBean.class);
        PlatformCertificateFactory pcf = PlatformCertificateFactory.create();
        pcf.holder = Holder.getInstance(Base64.decode(new String(iib.getHolder()).getBytes()));
        pcf.delta = iib.isDelta();
        pcf.attributes = iib.convertAttributes();
        pcf.extensions = iib.convertExtentions();
        return pcf;
    }

    /**
     * Creates a new PlatformCertificateFactory builder starting with policy reference data stored as JSON 
     * in a file.
     * @param filename The filename of a file containing JSON data.
     * @return A new PlatformCertificateFactory filled out with information from the file.
     */
    public static final PlatformCertificateFactory newPolicyRefJson(final String filename) {
        PlatformCertificateFactory pcf = PlatformCertificateFactory.create();
        
        try {
            final String jsonData = new String(Files.readAllBytes(Paths.get(filename)));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonData);
            if (root.has(PolicyRefJson.TCGPLATFORMSPECIFICATION.name())) {
                JsonNode platformSpecNode = root.get(PolicyRefJson.TCGPLATFORMSPECIFICATION.name());
                pcf.tcgPlatformSpecification(PolicyReferenceJsonHelper.platformSpec(platformSpecNode));
            }
            
            if (root.has(PolicyRefJson.TCGCREDENTIALSPECIFICATION.name())) {
                JsonNode credentialSpecNode = root.get(PolicyRefJson.TCGCREDENTIALSPECIFICATION.name());
                pcf.tcgCertificateSpecification(PolicyReferenceJsonHelper.credentialSpec(credentialSpecNode));
            }
            
            if (root.has(PolicyRefJson.TBBSECURITYASSERTIONS.name())) {
                JsonNode tbbSecAssertNode = root.get(PolicyRefJson.TBBSECURITYASSERTIONS.name());
                TBBSecurityAssertionsFactory tsaf = TBBSecurityAssertionsFactory
                                                    .create()
                                                    .fromJsonNode(tbbSecAssertNode);
                pcf.tbbSecurityAssertions(tsaf.build());
            }
            
            if (root.has(PolicyRefJson.PLATFORMCONFIGURI.name())) {
                JsonNode platformConfigUriNode = root.get(PolicyRefJson.PLATFORMCONFIGURI.name());
                URIReferenceFactory urf = URIReferenceFactory.fromJsonNode(platformConfigUriNode);
                pcf.platformConfigUri(urf.build());
            }
        } catch (IOException e) {
            // catch file read error
        }
        
        return pcf;
    }
}
