package cli;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidatorCliTest {
    // Requires tests within SigningCliTest to be complete prior to running these.
    @AfterAll
    public static void removeOldOutFiles() throws Exception {
        String[] filenames = new String[]{SigningCliTest.OUT_FILE, SigningCliTest.OUT_PKCS1_FILE, SigningCliTest.OUT_FILE_LARGE_2187, SigningCliTest.OUT_FILE_MEDIUM_2187, SigningCliTest.OUT_FILE_FLAWED_2187, SigningCliTest.OUT_FILE_PKCS12};
        for (String filename : filenames) {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    @Test
    public void test1NoExceptions() throws Exception {
    	SigningCliTest t = new SigningCliTest();
    	t.test1NoExceptions();
        String[] args = {"-P", SigningCliTest.IN_PUB_CERT, "-X", SigningCliTest.OUT_FILE};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assertions.assertTrue(result);
    }
    
    @Test
    public void test1NoExceptionsPKCS1RSATest() throws Exception {
    	SigningCliTest t = new SigningCliTest();
    	t.test1NoExceptionsPKCS1RSA();
        String[] args = {"-P", SigningCliTest.IN_PKCS1_PUB, "-X", SigningCliTest.OUT_PKCS1_FILE};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testLarge2187NoExceptions() throws Exception {
    	SigningCliTest t = new SigningCliTest();
    	t.testLarge2187NoExceptions();
        String[] args = {"-P", SigningCliTest.PUB_CERT_2187, "-X", SigningCliTest.OUT_FILE_LARGE_2187};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testMedium2187NoExceptions() throws Exception {
    	SigningCliTest t = new SigningCliTest();
    	t.testMedium2187NoExceptions();
        String[] args = {"-P", SigningCliTest.PUB_CERT_2187, "-X", SigningCliTest.OUT_FILE_MEDIUM_2187};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testFlawed2187NoExceptions() throws Exception {
    	SigningCliTest t = new SigningCliTest();
    	t.testFlawed2187NoExceptions();
        String[] args = {"-P", SigningCliTest.PUB_CERT_2187, "-X", SigningCliTest.OUT_FILE_FLAWED_2187};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assertions.assertTrue(result);
    }
    
    @Test
    public void test1NoExceptionsPKCS12() throws Exception {
    	SigningCliTest t = new SigningCliTest();
    	t.testPKCS12();
        String[] args = {"-P", SigningCliTest.IN_PUB_CERT_PKCS12, "-X", SigningCliTest.OUT_FILE_PKCS12};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assertions.assertTrue(result);
    }
}
