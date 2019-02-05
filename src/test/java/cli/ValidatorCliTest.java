package cli;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import java.io.File;

public class ValidatorCliTest {
    // Requires tests within SigningCliTest to be complete prior to running these.
    @AfterClass
    public void removeOldOutFiles() throws Exception {
        String[] filenames = new String[]{SigningCliTest.OUT_FILE, SigningCliTest.OUT_PKCS1_FILE, SigningCliTest.OUT_FILE_LARGE_2187, SigningCliTest.OUT_FILE_MEDIUM_2187, SigningCliTest.OUT_FILE_FLAWED_2187, SigningCliTest.OUT_FILE_PKCS12};
        for (String filename : filenames) {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    @Test(dependsOnGroups="1")
    public void test1NoExceptions() throws Exception {
        String[] args = {"-P", SigningCliTest.IN_PUB_CERT, "-X", SigningCliTest.OUT_FILE};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assert.assertTrue(result);
    }
    
    @Test(dependsOnGroups="pkcs1")
    public void test1NoExceptionsPKCS1RSATest() throws Exception {
        String[] args = {"-P", SigningCliTest.IN_PKCS1_PUB, "-X", SigningCliTest.OUT_PKCS1_FILE};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assert.assertTrue(result);
    }
    
    @Test(dependsOnGroups="Large2187")
    public void testLarge2187NoExceptions() throws Exception {
        String[] args = {"-P", SigningCliTest.PUB_CERT_2187, "-X", SigningCliTest.OUT_FILE_LARGE_2187};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assert.assertTrue(result);
    }
    
    @Test(dependsOnGroups="Medium2187")
    public void testMedium2187NoExceptions() throws Exception {
        String[] args = {"-P", SigningCliTest.PUB_CERT_2187, "-X", SigningCliTest.OUT_FILE_MEDIUM_2187};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assert.assertTrue(result);
    }
    
    @Test(dependsOnGroups="Flawed2187")
    public void testFlawed2187NoExceptions() throws Exception {
        String[] args = {"-P", SigningCliTest.PUB_CERT_2187, "-X", SigningCliTest.OUT_FILE_FLAWED_2187};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assert.assertTrue(result);
    }
    
    @Test(dependsOnGroups="pkcs12")
    public void test1NoExceptionsPKCS12() throws Exception {
        String[] args = {"-P", SigningCliTest.IN_PUB_CERT_PKCS12, "-X", SigningCliTest.OUT_FILE_PKCS12};
        ValidatorCli cli = new ValidatorCli();
        boolean result = cli.handleCommandLine(args);
        Assert.assertTrue(result);
    }
}
