package cli;

import org.junit.Test;

import factory.PlatformCertificateFactory;

public class DeviceObserverTest {
    private final String IN_EK = "src/test/resources/ek.cer";
    private final String IN_DEV_JSON = "src/test/resources/deviceInfo.json";
    private final String IN_POL_JSON = "src/test/resources/policyRef.json";
    
    @Test
    public void testNoExceptions() throws Exception {
        String[] args = {"-e", IN_EK, "-c", IN_DEV_JSON, "-p", IN_POL_JSON};
        DeviceObserverCli cli = new DeviceObserverCli();
        PlatformCertificateFactory pcf = cli.handleCommandLine(args);
    }
}
