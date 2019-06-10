package cli;

import cli.CliHelper.x509type;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import operator.PcBcContentVerifierProviderBuilder;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;

public class ValidatorCli {
    private ValidatorArgs argList;
    
    public ValidatorCli() {
        argList = new ValidatorArgs();
    }
    
    public boolean handleCommandLine(String[] args) throws IOException {
        JCommander jCommanderBuild = JCommander.newBuilder().addObject(argList).build();
        jCommanderBuild.setProgramName("validator");
        jCommanderBuild.setAcceptUnknownOptions(true);
        jCommanderBuild.parse(args);
        if (argList.isHelp()) {
            jCommanderBuild.usage();
            System.exit(1);
        }

        // load the public certificate of the key that signed the attribute cert
        X509CertificateHolder signingKeyPublicCert = (X509CertificateHolder)CliHelper.loadCert(argList.getPublicKeyCert(), x509type.CERTIFICATE);

        // load the signed attribute certificate which requires signature verification 
        X509AttributeCertificateHolder attributeCert = (X509AttributeCertificateHolder)CliHelper.loadCert(argList.getX509v2AttrCert(), x509type.ATTRIBUTE_CERTIFICATE);
        boolean valid = false;
        try {
            // Choose the verifier based on the public certificate signature algorithm
            ContentVerifierProvider cvp =
                    new PcBcContentVerifierProviderBuilder(
                           new DefaultDigestAlgorithmIdentifierFinder())
                    .build(signingKeyPublicCert);            
            // Verify the signature
            valid = attributeCert.isSignatureValid(cvp);
        } catch (OperatorCreationException | CertException e) {
            throw new IllegalArgumentException(e);
        }
        
        return valid;
    }
    
    public static final void main(String[] args) throws Exception {
        ValidatorCli cli = new ValidatorCli();
        try {
            if(!cli.handleCommandLine(args)) {
                System.exit(1);
            }
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
        }
    }
}
