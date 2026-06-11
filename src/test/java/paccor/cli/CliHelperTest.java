package paccor.cli;

import paccor.crypto.Pkcs12Helper;
import java.io.File;
import java.security.KeyStore;
import paccor.crypto.X509Credential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CliHelperTest {
	private static final String IN_TMPPKCS12 = CliHelperTest.class.getClassLoader().getResource("TestCA2.cert.example.pkcs12").getPath();

    @Test
    public void testFileExists() {
        File file = new File(IN_TMPPKCS12);
        Assertions.assertTrue(file.exists(), "The test file does not exist at the specified path.");
    }

    @Test
    public void testPkcs12() throws Exception {
        try (MockedStatic<CliHelper> mockedStatic = Mockito.mockStatic(CliHelper.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CliHelper.getPassword(ArgumentMatchers.anyString())).thenReturn(new KeyStore.PasswordProtection("password".toCharArray()));

            X509Credential pkcs12 = Pkcs12Helper.loadPkcs12Key(IN_TMPPKCS12);
            Assertions.assertNotNull(pkcs12);
            Assertions.assertNotNull(pkcs12.privateKey());
            Assertions.assertNotNull(pkcs12.jcePrivateKey());
            Assertions.assertNotNull(pkcs12.certificate());
        }
	}
}
