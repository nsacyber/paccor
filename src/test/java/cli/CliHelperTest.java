package cli;

import java.security.KeyStore;
import key.SignerCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CliHelperTest {
	private static final String IN_TMPPKCS12 = "src/test/resources/TestCA2.cert.example.pkcs12";

    @Test
    public void testPkcs12() throws Exception {
        try(MockedStatic<CliHelper> mockedStatic = Mockito.mockStatic(CliHelper.class)) {
            mockedStatic.when(() -> CliHelper.getPassword(Mockito.anyString())).thenReturn(new KeyStore.PasswordProtection("password".toCharArray()));

            SignerCredential pk = CliHelper.getKeyFromPkcs12(IN_TMPPKCS12);
            Assertions.assertNotNull(pk);
            Assertions.assertTrue(pk.hasKey());
            Assertions.assertTrue(pk.hasCertificate());
        }
	}
}
