package cli;

import java.lang.reflect.Method;
import java.security.KeyStore;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import key.SignerCredential;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CliHelper.class)
public class CliHelperTest {
	private static final String IN_TMPPKCS12 = "src/test/resources/TestCA2.cert.example.pkcs12";
	
	@Test
    public void testPkcs12() throws Exception {
		Method method = Whitebox.getMethod(CliHelper.class, "getPassword", String.class);
    	PowerMockito.stub(method).toReturn(new KeyStore.PasswordProtection("password".toCharArray()));
    	
        SignerCredential pk = PowerMockito.mock(CliHelper.class).getKeyFromPkcs12(IN_TMPPKCS12);
        Assert.assertNotNull(pk);
        Assert.assertTrue(pk.hasKey());
        Assert.assertTrue(pk.hasCertificate());
	}
}
