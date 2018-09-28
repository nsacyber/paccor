package cli;

import cli.CliHelper.x509type;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import factory.PlatformConfigurationFactory;
import factory.PlatformCredentialFactory;
import factory.SubjectAlternativeNameFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;


public class DeviceObserverCli {
    private DeviceObserverArgs argList;
    
    public DeviceObserverCli() {
        argList = new DeviceObserverArgs();
    }
    
    public PlatformCredentialFactory handleCommandLine(String[] args) throws IOException {
        JCommander jCommanderBuild = JCommander.newBuilder().addObject(argList).build();
        jCommanderBuild.setProgramName("observer");
        jCommanderBuild.setAcceptUnknownOptions(true);
        jCommanderBuild.parse(args);
        if (argList.isHelp()) {
            jCommanderBuild.usage();
            System.exit(1);
        }
        
        PlatformCredentialFactory pcf = 
            handleCommandLine(argList.getComponentJsonFile(), argList.getEkCertFile(), argList.getPolicyRefJsonFile());
        
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
    
    public PlatformCredentialFactory handleCommandLine(String componentFile, String ekCertFile, String policyFile) throws IOException {
        X509CertificateHolder ekCert;
        Holder holder = null;
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(CliHelper.derToPem(ekCertFile, x509type.CERTIFICATE)));
        PEMParser p = new PEMParser(isr);
        Object readObject = p.readObject();
        p.close();
        if (readObject instanceof X509CertificateHolder) {
            ekCert = (X509CertificateHolder)readObject;
            holder = new Holder(new IssuerSerial(ekCert.getIssuer(), ekCert.getSerialNumber()));
        } else {
            // file labeled the ekCert was not an X509 Certificate!!
        }
        
        PlatformConfigurationFactory pConfig = 
                PlatformConfigurationFactory.create()
                .addDataFromJsonFile(componentFile);
        SubjectAlternativeNameFactory sanf = 
                SubjectAlternativeNameFactory
                .fromJsonFile(componentFile);
        PlatformCredentialFactory pcf =
                PlatformCredentialFactory.newPolicyRefJson(policyFile);
        pcf.platformConfiguration(pConfig.build());
        pcf.holder(holder);
        pcf.addExtension(Extension.subjectAlternativeName, sanf.build());
        
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
