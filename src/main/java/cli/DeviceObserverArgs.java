package cli;

import com.beust.jcommander.Parameter;
import cli.pv.FileExistsParameterValidator;
import cli.pv.ReadFileParameterValidator;

public class DeviceObserverArgs {
    @Parameter(required=true, names={"-c", "--componentJsonFile"}, order=0, description="the path to the JSON file output from the component script", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String componentJsonFile;
    
    @Parameter(required=true, names={"-e" , "--ekCertFile"}, order=1, description="the path to a X509v3 EK Certificate", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String ekCertFile;
    
    @Parameter(required=true, names={"-p", "--policyRefJsonFile"}, order=2, description="the path to the JSON file output from the policy reference script", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String policyRefJsonFile;
    
    @Parameter(names={"-f", "--file"}, order=3, description="optional field\nthe output file path.  if not set, stdout will be used.")
    private String outFile;
    
    @Parameter(names={"-h", "--help"}, order=4, help = true, description="print this help message")
    private boolean help;
    
    @Parameter(names={"--quiet"}, order=5, description="no output")
    private boolean quiet = false;

    public String getComponentJsonFile() {
        return componentJsonFile;
    }
    
    public String getEkCertFile() {
        return ekCertFile;
    }

    public String getPolicyRefJsonFile() {
        return policyRefJsonFile;
    }

    public String getOutFile() {
        return outFile;
    }

    public boolean isHelp() {
        return help;
    }
    
    public boolean isQuiet() {
        return quiet;
    }
}
