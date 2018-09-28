package cli;

import com.beust.jcommander.Parameter;
import cli.pv.FileExistsParameterValidator;
import cli.pv.ReadFileParameterValidator;

public class ValidatorArgs {

    @Parameter(required=true, names={"-P", "--publicKeyCert"}, order=0, description="the public key certificate of the signing key", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String publicKeyCert;
    
    @Parameter(required=true, names={"-X", "--x509v2AttrCert"}, order=1, description="the certificate containing a signature to validate", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String x509v2AttrCert;
    
    @Parameter(names={"-h", "--help"}, order=2, help = true, description="print this help message")
    private boolean help;

    public String getPublicKeyCert() {
        return publicKeyCert;
    }

    public String getX509v2AttrCert() {
        return x509v2AttrCert;
    }

    public boolean isHelp() {
        return help;
    }
}
