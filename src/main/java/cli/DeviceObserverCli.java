package cli;

import cli.CliHelper.x509type;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import factory.PlatformConfigurationV2Factory;
import factory.PlatformCertificateFactory;
import factory.SubjectAlternativeNameFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;


public class DeviceObserverCli {
    private DeviceObserverArgs argList;
    
    public DeviceObserverCli() {
        argList = new DeviceObserverArgs();
    }
    
    public PlatformCertificateFactory handleCommandLine(String[] args) throws IOException {
        JCommander jCommanderBuild = JCommander.newBuilder().addObject(argList).build();
        jCommanderBuild.setProgramName("observer");
        jCommanderBuild.setAcceptUnknownOptions(true);
        jCommanderBuild.parse(args);
        if (argList.isHelp()) {
            jCommanderBuild.usage();
            System.exit(1);
        }
        
        PlatformCertificateFactory pcf = 
            collateCertDetails(argList.getComponentJsonFile(), argList.getHolderCertFile(), argList.getPolicyRefJsonFile());
        
        String result = pcf.toJson();
        if (argList.getOutFile() != null && !argList.getOutFile().isEmpty()) {
            File file = new File(argList.getOutFile());
            if (file.exists()) {
                throw new IllegalArgumentException("File " + argList.getOutFile() + " already exists.  Will not overwrite.");
            } else if (!file.createNewFile()) {
                throw new IllegalArgumentException("Could not create a new file " + argList.getOutFile() + ".");
            }
            
            FileWriter writer = new FileWriter(file);
            writer.write(result);
            writer.flush();
            writer.close();
        } else if (!argList.isQuiet()) {
            System.out.println(result);
        }
        
        return pcf;
    }
    
    public PlatformCertificateFactory collateCertDetails(String componentFile, String ekCertFile, String policyFile) throws IOException {
        // Base Platform Certificates must use an EKC (Certificate) as the Holder
        // Delta Platform Certificates must use a PC (Attribute Certificate) as the Holder
        X509CertificateHolder ekCert = null;
        X509AttributeCertificateHolder pCert = null;
        IssuerSerial issuerSerial = null;
        boolean delta = false;
        
        try {// Attempt to parse holder as a PK certificate
            ekCert = (X509CertificateHolder)CliHelper.loadCert(ekCertFile, x509type.CERTIFICATE);
            issuerSerial = new IssuerSerial(ekCert.getIssuer(), ekCert.getSerialNumber());
        } catch (Exception e) {
            
        }
        try {// Attempt to parse holder as an attribute certificate. If so, create a delta certificate.
            pCert = (X509AttributeCertificateHolder)CliHelper.loadCert(ekCertFile, x509type.ATTRIBUTE_CERTIFICATE);
            delta = true;
            // X509AttributeCertificateHolder does not extract a compatible IssuerSerial object 
            X500Name[] parts = pCert.getIssuer().getNames();
            GeneralName[] gns = new GeneralName[parts.length];
            for (int i = 0; i < parts.length; i++) {
                gns[i] = new GeneralName(parts[i]);
            }
            issuerSerial = new IssuerSerial(new GeneralNames(gns), pCert.getSerialNumber());
        } catch (Exception e) {
            
        }
        
        if (ekCert == null && pCert == null) {
            throw new IOException("Invalid holder provided: " + ekCertFile);
        }
        
        Holder holder = new Holder(issuerSerial);
        
        PlatformConfigurationV2Factory pConfig = 
                PlatformConfigurationV2Factory.create()
                .addDataFromJsonFile(componentFile);
        SubjectAlternativeNameFactory sanf = 
                SubjectAlternativeNameFactory
                .fromJsonFile(componentFile);
        PlatformCertificateFactory pcf =
                PlatformCertificateFactory.readAttributesJson(policyFile);
        final GeneralNames san = sanf.build();
        if (delta) {
            pcf.setDeltaCertificate();
            
            // Proposed SubjectAlternativeName should match the SAN of the holder platform certificate
            ASN1ObjectIdentifier sanOid = Extension.subjectAlternativeName;
            boolean isCritical = PlatformCertificateFactory.criticalExtensions.get(sanOid).booleanValue();
            Extension sanExt = new Extension(sanOid, isCritical, san.getEncoded());
            if (!sanExt.equals(pCert.getExtension(sanOid))) {
                throw new IllegalArgumentException("Subject alternative name did not match holder.");
            }
        } else {
            // Base certificates cannot use AttributeStatus in PlatformConfiguration Components and Properties
            if (pConfig.isStatusUsed()) {
                throw new IllegalArgumentException("AttributeStatus cannot be used for Components or PlatformProperties in a Base Platform Certificate.");
            }
        }
        pcf.platformConfiguration(pConfig.build());
        pcf.holder(holder);
        pcf.addExtension(Extension.subjectAlternativeName, san);
        
        return pcf;
    }
    
    public static final void main(String[] args) throws Exception {
        DeviceObserverCli cli = new DeviceObserverCli();
        try {
            cli.handleCommandLine(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
        }
    }
}
