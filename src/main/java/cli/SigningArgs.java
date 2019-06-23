package cli;

import com.beust.jcommander.Parameter;
import cli.pv.DateParameterValidator;
import cli.pv.FileExistsParameterValidator;
import cli.pv.IntStringParameterValidator;
import cli.pv.ReadFileParameterValidator;

public class SigningArgs {
    // observerJson or ek+devJson+polJson
    @Parameter(hidden=true, names={"-o", "--observerJsonFile"}, order=0, description="the JSON output from the observer program", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String observerJsonFile;
    
    @Parameter(names={"-c", "--componentJsonFile"}, order=1, description="the path to the JSON file output from the component script", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String componentJsonFile;
    
    @Parameter(names={"-e" , "--holderCertFile"}, order=2, description="if making a base certificate, provide the path to a X509v3 EK Certificate.  For delta certificate, provide the path to a platform certificate", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String holderCertFile;
    
    @Parameter(names={"-p", "--policyRefJsonFile"}, order=3, description="the path to the JSON file output from the policy reference script", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String policyRefJsonFile;
    
    // required
    @Parameter(required=true, names={"-N", "--serialNumber"}, order=4, description="the serial number of the new certificate", validateWith={IntStringParameterValidator.class})
    private String serialNumber;
    
    @Parameter(required=true, names={"-b", "--dateNotBefore"}, order=5, description="the begin date of the validity period for the certificate, YYYYMMDD", validateWith={DateParameterValidator.class})
    private String dateNotBefore;
    
    @Parameter(required=true, names={"-a", "--dateNotAfter"}, order=6, description="the end date of the validity period for the certificate, YYYYMMDD", validateWith={DateParameterValidator.class})
    private String dateNotAfter;
    
    @Parameter(required=true, names={"-k", "--privateKeyFile"}, order=7, description="the private key used for signing the certificate", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String privateKeyFile;
    
    @Parameter(names={"-P", "--publicKeyCert"}, order=8, description="the public key certificate corresponding to the private key", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String publicKeyCert;
    
    @Parameter(required=true, names={"-x", "--extensionsJsonFile"}, order=9, description="the JSON structure describing certificate policies, crl distribution, and authority key access extensions", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String extensionsJsonFile;
    
    @Parameter(names={"-f", "--file"}, order=10, description="(optional field) the output file path.  if not set, stdout will be used.")
    private String outFile;
    
    @Parameter(names={"--pem"}, order=11, description="platform certificate will be output in PEM format")
    private boolean pem = false;
    
    @Parameter(names={"-h", "--help"}, order=12, help = true, description="print this help message")
    private boolean help;
    
    @Parameter(names={"--quiet"}, order=13, description="no output")
    private boolean quiet = false;
    
    public String getHolderCertFile() {
        return holderCertFile;
    }

    public String getComponentJsonFile() {
        return componentJsonFile;
    }

    public String getPolicyRefJsonFile() {
        return policyRefJsonFile;
    }

    public String getObserverJsonFile() {
        return observerJsonFile;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getDateNotBefore() {
        return dateNotBefore;
    }

    public String getDateNotAfter() {
        return dateNotAfter;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public String getPublicKeyCert() {
        return publicKeyCert;
    }

    public String getExtensionsJsonFile() {
        return extensionsJsonFile;
    }
    
    public String getOutFile() {
        return outFile;
    }
    
    public boolean isPemOutput() {
        return pem;
    }

    public boolean isHelp() {
        return help;
    }
    
    public boolean isQuiet() {
        return quiet;
    }
}
