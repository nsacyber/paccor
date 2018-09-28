package cli;

import cli.CliHelper.x509type;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import factory.AuthorityInfoAccessFactory;
import factory.CertificatePoliciesFactory;
import factory.PlatformCredentialFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import json.OtherExtensionsJsonHelper;
import operator.PcBcContentSignerBuilder;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;


public class SigningCli {
    public static final String DATE_FORMAT = "yyyyMMdd";
    
    private SigningArgs argList;
    
    public SigningCli() {
        argList = new SigningArgs();
    }
    
    public void handleCommandLine(String[] args) throws IOException {
        JCommander jCommanderBuild = JCommander.newBuilder().addObject(argList).build();
        jCommanderBuild.setProgramName("signer");
        jCommanderBuild.setAcceptUnknownOptions(true);
        jCommanderBuild.parse(args);
        if (argList.isHelp()) {
            jCommanderBuild.usage();
            System.exit(1);
        }
        
        // Get the public key certificate
        X509CertificateHolder publicKeyCert = (X509CertificateHolder)CliHelper.loadCert(argList.getPublicKeyCert(), x509type.CERTIFICATE);
        
        // Get the private key
        PrivateKeyInfo privateKey = null;
        
        try {
            privateKey = (PrivateKeyInfo)CliHelper.loadCert(argList.getPrivateKeyFile(), x509type.PRIVATE_KEY);
        } catch (IllegalArgumentException | IOException e) {
            String keyAlgName = new DefaultAlgorithmNameFinder().getAlgorithmName(publicKeyCert.getSubjectPublicKeyInfo().getAlgorithm().getAlgorithm());
            keyAlgName = keyAlgName.toLowerCase();
            
            x509type keyType = null;
            switch (keyAlgName) {
                case "rsa": keyType = x509type.RSA_PRIVATE_KEY;
                            break;
                case "dsa": keyType = x509type.DSA_PRIVATE_KEY;
                            break;
                case "ec":  keyType = x509type.EC_PRIVATE_KEY;
                            break;
                default:
            }
            
            if (keyType == null) {
                throw new IllegalArgumentException("Unsupported key pair algorithm (" + keyType + ").  See the Supported Signing Algorithms section of the User Guide.");
            }
            
            privateKey = (PrivateKeyInfo)CliHelper.loadCert(argList.getPrivateKeyFile(), keyType);
        }
        
        // Initialize the Platform Credential data
        // One option is to use the output from the AttributeCertInfo program
        // Another is to give this program all of the input that would have been given to the first program
        PlatformCredentialFactory pcf = null;
        boolean hasObserverFile = argList.getObserverJsonFile() != null;
        boolean hasComponentFile = argList.getComponentJsonFile() != null;
        boolean hasEkFile = argList.getEkCertFile() != null;
        boolean hasPolicyFile = argList.getPolicyRefJsonFile() != null;
        if (hasObserverFile) { 
            pcf = PlatformCredentialFactory.loadIntermediateInfofromJson(argList.getObserverJsonFile());
        } else if(hasComponentFile && hasEkFile && hasPolicyFile) {
            DeviceObserverCli acic = new DeviceObserverCli();
            pcf = acic.handleCommandLine(argList.getComponentJsonFile(), argList.getEkCertFile(), argList.getPolicyRefJsonFile());
        } else {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Missing required input file(s).  Please include:");
            if (!hasEkFile && !hasComponentFile && !hasPolicyFile) {
                errorMsg.append("--observerJsonFile");
            } else {
                if (!hasComponentFile) {
                    errorMsg.append(" --componentJsonFile,");
                }
                if (!hasEkFile) {
                    errorMsg.append(" --ekCertFile,");
                }
                if (!hasPolicyFile) {
                    errorMsg.append(" --policyRefJsonFile,");
                }
                errorMsg.deleteCharAt(errorMsg.length()-1);
            }
            errorMsg.append(".  Use -h for details.");
            throw new IllegalArgumentException(errorMsg.toString());
        }
        
        // set up new fields: SN, validity, issuer
        BigInteger serialNumber = new BigInteger(argList.getSerialNumber());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date notBefore = null;
        Date notAfter = null;
        try {
            notBefore = sdf.parse(argList.getDateNotBefore());
            notAfter = sdf.parse(argList.getDateNotAfter());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Problem getting the dates.");
        }

        pcf.serialNumber(serialNumber);
        pcf.notAfter(notAfter);
        pcf.notBefore(notBefore);
        pcf.issuer(new AttributeCertificateIssuer(publicKeyCert.getSubject()));
        
        // Build other extensions
        BcX509ExtensionUtils extUtils = new BcX509ExtensionUtils();
        AuthorityKeyIdentifier aki = extUtils.createAuthorityKeyIdentifier(publicKeyCert);
        CertificatePoliciesFactory cpf = OtherExtensionsJsonHelper.policiesFromJsonFile(argList.getExtensionsJsonFile());
        AuthorityInfoAccessFactory aiaf = OtherExtensionsJsonHelper.accessesFromJsonFile(argList.getExtensionsJsonFile());
        AuthorityInformationAccess aia = aiaf.build();
        CRLDistPoint cdp = OtherExtensionsJsonHelper.crlFromJsonFile(argList.getExtensionsJsonFile());
        
        pcf.addExtension(Extension.authorityKeyIdentifier, aki);
        pcf.addExtension(Extension.certificatePolicies, cpf.build());
        if (aia != null) {
            pcf.addExtension(Extension.authorityInfoAccess, aia);
        }
        if (cdp != null) {
            pcf.addExtension(Extension.cRLDistributionPoints, cdp);
        }
        
        // Build the cert & sign it using the private key
        X509AttributeCertificateHolder ach = null;
        try {
            AlgorithmIdentifier sigAlgId = publicKeyCert.getSignatureAlgorithm();
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            ContentSigner signer;
            signer = new PcBcContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(privateKey));
            ach = pcf.build(signer);
        } catch (OperatorCreationException e) {
            throw new IllegalArgumentException(e);
        }
        
        // Convert the platform credential encoded bytes to a string for output
        if (argList.getOutFile() != null && !argList.getOutFile().isEmpty()) {
            File file = new File(argList.getOutFile());
            if (file.exists()) {
                throw new IllegalArgumentException("File " + argList.getOutFile() + " already exists.  Will not overwrite.");
            } else if (!file.createNewFile()) {
                throw new IllegalArgumentException("Could not create a new file " + argList.getOutFile() + ".");
            }
            
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(ach.getEncoded());
                stream.flush();
            } finally {
                stream.close();
            }
        } else if (!argList.isQuiet()) {
            System.out.write(ach.getEncoded());
        }
    }
    
    public static final void main(String[] args) throws Exception {
        SigningCli cli = new SigningCli();
        try {
            cli.handleCommandLine(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
        }
    }
}
