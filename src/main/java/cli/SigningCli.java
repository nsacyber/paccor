package cli;

import cli.CliHelper.x509type;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import factory.AuthorityInfoAccessFactory;
import factory.CertificatePoliciesFactory;
import factory.PlatformCertificateFactory;
import factory.TargetingInformationFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import json.ExtensionsJsonHelper;
import key.SignerCredential;
import operator.PcBcContentSignerBuilder;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.TargetInformation;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.Base64;


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
        
        // Retrieve CA signing credentials from the command line
        SignerCredential caCred = null;
        
        // Get the CA public key certificate
        if (argList.getPublicKeyCert() != null) { // If not set on cmd line, the private key file might contain the cert
        	X509CertificateHolder publicKeyCert = (X509CertificateHolder)CliHelper.loadCert(argList.getPublicKeyCert(), x509type.CERTIFICATE);
        	caCred = SignerCredential.createCredential(publicKeyCert.getSubjectPublicKeyInfo().getAlgorithm().getAlgorithm().getId());
        	caCred.loadCertificate(publicKeyCert);
        } 
        
        // Get the CA private key
        PrivateKeyInfo privateKey = null;
        try { // Try to read as unprotected PKCS8
            privateKey = (PrivateKeyInfo)CliHelper.loadCert(argList.getPrivateKeyFile(), x509type.PRIVATE_KEY);
            
        } catch (IllegalArgumentException | IOException e) { // try to read as PKCS1
        	try {
	        	if (caCred != null && caCred.hasCertificate()) {
	            	privateKey = (PrivateKeyInfo)CliHelper.loadCert(argList.getPrivateKeyFile(), caCred.getKeyType());
	            }
        	} catch (IllegalArgumentException | IOException e2) {
        	}
        }
        
        if (privateKey != null && caCred == null) {
        	throw new IllegalArgumentException("CA Certificate not provided.");
        } else if (privateKey != null && caCred != null) {
        	caCred.loadPrivateKey(privateKey);
        } else if (privateKey == null) { // Attempt to load private key file as PKCS12
        	SignerCredential keyCreds = CliHelper.getKeyFromPkcs12(argList.getPrivateKeyFile());
        	if (keyCreds == null) {
        		throw new IllegalArgumentException("No private key provided.");
        	} else if (caCred == null) {
        		caCred = keyCreds;
        	}
        	
        	if (!caCred.hasKey() && keyCreds.hasKey()) {
        		caCred.loadPrivateKey(keyCreds.getPrivateKey());
        	}
        	
        	if (!caCred.hasCertificate() && keyCreds.hasCertificate()) {
        		caCred.loadCertificate(keyCreds.getCertificate());
        	}
        }
        
        // Initialize the Platform Credential data
        // One option is to use the output from the AttributeCertInfo program
        // Another is to give this program all of the input that would have been given to the first program
        PlatformCertificateFactory pcf = null;
        boolean hasObserverFile = argList.getObserverJsonFile() != null;
        boolean hasComponentFile = argList.getComponentJsonFile() != null;
        boolean hasEkFile = argList.getHolderCertFile() != null;
        boolean hasPolicyFile = argList.getPolicyRefJsonFile() != null;
        if (hasObserverFile) { 
            pcf = PlatformCertificateFactory.loadIntermediateInfofromJson(argList.getObserverJsonFile());
        } else if(hasComponentFile && hasEkFile && hasPolicyFile) {
            DeviceObserverCli acic = new DeviceObserverCli();
            pcf = acic.collateCertDetails(argList.getComponentJsonFile(), argList.getHolderCertFile(), argList.getPolicyRefJsonFile());
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
        pcf.issuer(new AttributeCertificateIssuer(caCred.getCertificate().getSubject()));
        
        // Build other extensions
        ExtensionsJsonHelper ext = ExtensionsJsonHelper.read(argList.getExtensionsJsonFile());
        BcX509ExtensionUtils extUtils = new BcX509ExtensionUtils();
        AuthorityKeyIdentifier aki = extUtils.createAuthorityKeyIdentifier(caCred.getCertificate());
        
        pcf.addExtension(Extension.authorityKeyIdentifier, aki);
        pcf.addExtension(Extension.certificatePolicies, ext.getCertificatePolicies());
        if (ext.getAuthorityInformationAccess() != null) {
            pcf.addExtension(Extension.authorityInfoAccess, ext.getAuthorityInformationAccess());
        }
        if (ext.getCrlDistPoint() != null) {
            pcf.addExtension(Extension.cRLDistributionPoints, ext.getCrlDistPoint());
        }
        if (ext.getTargetingInformation() != null) {
            pcf.addExtension(Extension.targetInformation, ext.getTargetingInformation());
        }
        
        // Build the cert & sign it using the private key
        X509AttributeCertificateHolder ach = null;
        try {
            AlgorithmIdentifier sigAlgId = caCred.getCertificate().getSignatureAlgorithm();
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            ContentSigner signer;
            signer = new PcBcContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(caCred.getPrivateKey()));
            ach = pcf.build(signer);
        } catch (OperatorCreationException e) {
            throw new IllegalArgumentException(e);
        }
        
        // Convert the platform credential encoded bytes to a string for output
        final byte[] output = argList.isPemOutput() ? (x509type.ATTRIBUTE_CERTIFICATE.getPemHeader() + Base64.toBase64String(ach.getEncoded()) + x509type.ATTRIBUTE_CERTIFICATE.getPemFooter()).getBytes() : ach.getEncoded();
        if (argList.getOutFile() != null && !argList.getOutFile().isEmpty()) {
            File file = new File(argList.getOutFile());
            if (file.exists()) {
                throw new IllegalArgumentException("File " + argList.getOutFile() + " already exists.  Will not overwrite.");
            } else if (!file.createNewFile()) {
                throw new IllegalArgumentException("Could not create a new file " + argList.getOutFile() + ".");
            }
            
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(output);
                stream.flush();
            } finally {
                stream.close();
            }
        } else if (!argList.isQuiet()) {
            System.out.write(output);
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
