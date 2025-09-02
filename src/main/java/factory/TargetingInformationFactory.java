package factory;

import cli.CliHelper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import json.JsonUtils;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.Target;
import org.bouncycastle.asn1.x509.TargetInformation;
import org.bouncycastle.cert.X509CertificateHolder;
import tcg.credential.ASN1Utils;

/**
 * Functions to help manage the creation of the AC targeting extension.
 */
public class TargetingInformationFactory {
    public enum TargetInformationJson {
        FILE;
    }
    
    private final List<Target> elements;
    
    private TargetingInformationFactory() {
        elements = new ArrayList<>();
    }
    
    /**
     * Begin defining the target information extension.
     * @return A new TargetingInformationFactory builder.
     */
    public static final TargetingInformationFactory create() {
        return new TargetingInformationFactory();
    }
    
    /**
     * Add a target element.
     * @param element {@link Target}
     * @return The TargetingInformationFactory object with a target element added.
     */
    public final TargetingInformationFactory addElement(final Target element) {
        elements.add(element);
        return this;
    }
    
    /**
     * Add a certificate.
     * @param cert {@link X509CertificateHolder}
     * @return The TargetingInformationFactory object with a certificate added.
     */
    public final TargetingInformationFactory addCertificate(final X509CertificateHolder cert) {
        X500Name subjectName = cert.getSubject();
        
        if (subjectName == null || cert.getSerialNumber() == null) {
            throw new IllegalArgumentException("The target information extension cannot use "
                    + "the provided certificate.  It is missing vital information.");
        }
        
        // The EKC serial number MUST be included as an RDN at oid id-at-serialnumber
        if (subjectName.getRDNs(BCStyle.SERIALNUMBER).length == 0) {
            // Add the certificate serial number
            
            // Convert the serial number from BigInteger to a RDN
            DERUTF8String serialNumber = new DERUTF8String(cert.getSerialNumber().toString());
            AttributeTypeAndValue serialNumberATV = new AttributeTypeAndValue(BCStyle.SERIALNUMBER, serialNumber);
            RDN serialNumberRdn = new RDN(serialNumberATV);
            
            // Add the RDN to the X500Name
            List<RDN> rdnList = new ArrayList<>(Arrays.asList(subjectName.getRDNs()));
            rdnList.add(serialNumberRdn);
            
            subjectName = new X500Name(rdnList.toArray(new RDN[0]));
        }
        
        // Add the new Target to the extension
        Target target = new Target(Target.targetName, new GeneralName(subjectName));
        addElement(target);
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link TargetInformation}
     */
    public final TargetInformation build() {
        return TargetInformation.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(elements)));
    }
    
    /**
     * Create a new certificate policies object from a JSON node.
     * @param refNode JsonNode representing a project relevant certificate policies JSON object
     * @return The CertificatePoliciesFactory object with new information from the JSON data.
     */
    public static final TargetingInformationFactory fromJsonNode(final JsonNode refNode) {
        TargetingInformationFactory tif = TargetingInformationFactory.create();
        boolean caseSens = false;
        if (refNode.isArray()) {
            JsonUtils.asStream(refNode.spliterator())
                    .filter(target -> JsonUtils.has(target, caseSens, TargetInformationJson.FILE.name()))
                    .forEach(target -> {
                        Optional<JsonNode> targetOpt = JsonUtils.get(target, caseSens, TargetInformationJson.FILE.name());

                        String targetFilename = targetOpt.orElseThrow().asText();

                        try {
                            X509CertificateHolder cert = CliHelper.loadCert(targetFilename, CliHelper.x509type.CERTIFICATE);
                            tif.addCertificate(cert);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }); // forEach
        }
        
        return tif;
    }
}
