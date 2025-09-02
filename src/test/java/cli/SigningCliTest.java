package cli;

import java.io.File;
import java.security.KeyStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SigningCliTest {
    private static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "/";
    private static final String IN_EK = "src/test/resources/ek.cer";
    private static final String IN_DEV_JSON = "src/test/resources/deviceInfo.json";
    private static final String IN_POL_JSON = "src/test/resources/policyRef.json";
    private static final String IN_OXT_JSON = "src/test/resources/otherExt.json";
    public static final String IN_PUB_CERT = "src/test/resources/TestCA.cert.example.pem";
    private static final String IN_PRIV_KEY = "src/test/resources/TestCA.private.example.pem";
    public static final String OUT_FILE = TEMP_FOLDER + "blah.pem";
    
    public static final String IN_PKCS1_PUB = "src/test/resources/ca.pkcs1.pub.pem";
    private static final String IN_PKCS1_KEY = "src/test/resources/ca.pkcs1.pem";
    public static final String OUT_PKCS1_FILE = TEMP_FOLDER + "blah2.pem";
    
    private static final String SERIAL_NUMBER = "85748596854741335865214";
    private static final String NOT_BEFORE = "20180628";
    private static final String NOT_AFTER = "20280630";
    
    private static final String IN_EK_2187 = "src/test/resources/ek_cert_2187.der";
    private static final String IN_DEV_JSON_LARGE_2187 = "src/test/resources/comps_large_2187.json";
    private static final String IN_POL_JSON_2187 = "src/test/resources/refopts_2187.json";
    private static final String IN_OXT_JSON_2187 = "src/test/resources/otherext_2187.json";
    public static final String PUB_CERT_2187 = "src/test/resources/ca_2187.crt";
    private static final String IN_PRIV_KEY_2187 = "src/test/resources/ca_2187.key";
    private static final String SERIAL_NUMBER_LARGE_2187 = "34146254462154519453612545265143";
    public static final String OUT_FILE_LARGE_2187 = TEMP_FOLDER + "large_attribute_cert_2187.pem";
    
    private static final String IN_DEV_JSON_MEDIUM_2187 = "src/test/resources/comps_medium_2187.json";
    private static final String SERIAL_NUMBER_MEDIUM_2187 = "97643418218536546461465465475484";
    public static final String OUT_FILE_MEDIUM_2187 = TEMP_FOLDER + "medium_attribute_cert_2187.pem";
    
    private static final String IN_DEV_JSON_FLAWED_2187 = "src/test/resources/comps_flawed_2187.json";
    private static final String SERIAL_NUMBER_FLAWED_2187 = "1264412569842165127559455612352835923762345";
    public static final String OUT_FILE_FLAWED_2187 = TEMP_FOLDER + "flawed_attribute_cert_2187.pem";
    
    public static final String IN_PUB_CERT_PKCS12 = "src/test/resources/TestCA2.cert.example.der";
    private static final String IN_PRIV_PKCS12 = "src/test/resources/TestCA2.cert.example.pkcs12";
    public static final String OUT_FILE_PKCS12 = TEMP_FOLDER + "blah3.pem";
    
    @BeforeAll
    @AfterAll
    public static void removeOldOutFiles() throws Exception {
        String[] filenames = new String[]{OUT_FILE, OUT_PKCS1_FILE, OUT_FILE_LARGE_2187, OUT_FILE_MEDIUM_2187, OUT_FILE_FLAWED_2187, OUT_FILE_PKCS12};
        for (String filename : filenames) {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    @Test
    public void test1NoExceptions() throws Exception {
        String[] args = {"-e", IN_EK, "-c", IN_DEV_JSON, "-p", IN_POL_JSON,
                         "-x", IN_OXT_JSON, "-k", IN_PRIV_KEY, "-P", IN_PUB_CERT,
                         "-N", SERIAL_NUMBER, "-b", NOT_BEFORE, "-a", NOT_AFTER,
                         "-f", OUT_FILE, "--pem"};
        SigningCli cli = new SigningCli();
        cli.handleCommandLine(args);
        File file = new File(OUT_FILE);
        Assertions.assertTrue(file.exists());
    }
    
    @Test
    public void test1NoExceptionsPKCS1RSA() throws Exception {
        String[] args = {"-e", IN_EK, "-c", IN_DEV_JSON, "-p", IN_POL_JSON,
                         "-x", IN_OXT_JSON, "-k", IN_PKCS1_KEY, "-P", IN_PKCS1_PUB,
                         "-N", SERIAL_NUMBER, "-b", NOT_BEFORE, "-a", NOT_AFTER,
                         "-f", OUT_PKCS1_FILE, "--pem"};
        SigningCli cli = new SigningCli();
        cli.handleCommandLine(args);
        File file = new File(OUT_PKCS1_FILE);
        Assertions.assertTrue(file.exists());
    }
    
    @Test
    public void testLarge2187NoExceptions() throws Exception {
        String[] args = {"-e", IN_EK_2187, "-c", IN_DEV_JSON_LARGE_2187, "-p", IN_POL_JSON_2187,
                         "-x", IN_OXT_JSON_2187, "-k", IN_PRIV_KEY_2187, "-P", PUB_CERT_2187,
                         "-N", SERIAL_NUMBER_LARGE_2187, "-b", NOT_BEFORE, "-a", NOT_AFTER,
                         "-f", OUT_FILE_LARGE_2187, "--pem"};
        SigningCli cli = new SigningCli();
        cli.handleCommandLine(args);
        File file = new File(OUT_FILE_LARGE_2187);
        Assertions.assertTrue(file.exists());
    }
    
    @Test
    public void testMedium2187NoExceptions() throws Exception {
        String[] args = {"-e", IN_EK_2187, "-c", IN_DEV_JSON_MEDIUM_2187, "-p", IN_POL_JSON_2187,
                         "-x", IN_OXT_JSON_2187, "-k", IN_PRIV_KEY_2187, "-P", PUB_CERT_2187,
                         "-N", SERIAL_NUMBER_MEDIUM_2187, "-b", NOT_BEFORE, "-a", NOT_AFTER,
                         "-f", OUT_FILE_MEDIUM_2187, "--pem"};
        SigningCli cli = new SigningCli();
        cli.handleCommandLine(args);
        File file = new File(OUT_FILE_MEDIUM_2187);
        Assertions.assertTrue(file.exists());
    }
    
    @Test
    public void testFlawed2187NoExceptions() throws Exception {
        String[] args = {"-e", IN_EK_2187, "-c", IN_DEV_JSON_FLAWED_2187, "-p", IN_POL_JSON_2187,
                         "-x", IN_OXT_JSON_2187, "-k", IN_PRIV_KEY_2187, "-P", PUB_CERT_2187,
                         "-N", SERIAL_NUMBER_FLAWED_2187, "-b", NOT_BEFORE, "-a", NOT_AFTER,
                         "-f", OUT_FILE_FLAWED_2187, "--pem"};
        SigningCli cli = new SigningCli();
        cli.handleCommandLine(args);
        File file = new File(OUT_FILE_FLAWED_2187);
        Assertions.assertTrue(file.exists());
    }
    
    @Test
    public void testPKCS12() throws Exception {
        try(MockedStatic<CliHelper> mockedStatic = Mockito.mockStatic(CliHelper.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CliHelper.getPassword(Mockito.anyString())).thenReturn(new KeyStore.PasswordProtection("password".toCharArray()));

            String[] args = {"-e", IN_EK, "-c", IN_DEV_JSON, "-p", IN_POL_JSON,
                    "-x", IN_OXT_JSON, "-k", IN_PRIV_PKCS12,
                    "-N", SERIAL_NUMBER, "-b", NOT_BEFORE, "-a", NOT_AFTER,
                    "-f", OUT_FILE_PKCS12, "--pem"};

            Mockito.spy(new SigningCli()).handleCommandLine(args);
            File file = new File(OUT_FILE_PKCS12);
            Assertions.assertTrue(file.exists());
        }
    }
}
