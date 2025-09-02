package cli;

import java.io.File;
import java.security.KeyStore;
import key.SignerCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
        try (MockedStatic<CliHelper> mockedStatic = mockStatic(CliHelper.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CliHelper.getPassword(anyString())).thenReturn(new KeyStore.PasswordProtection("password".toCharArray()));

            SignerCredential pk = CliHelper.getKeyFromPkcs12(IN_TMPPKCS12);
            Assertions.assertNotNull(pk);
            Assertions.assertTrue(pk.hasKey());
            Assertions.assertTrue(pk.hasCertificate());
        }
	}
}
