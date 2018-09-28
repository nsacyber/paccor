package cli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.encoders.Base64;

public class CliHelper {    
    public enum x509type {
        CERTIFICATE("CERTIFICATE", X509CertificateHolder.class),
        ATTRIBUTE_CERTIFICATE("ATTRIBUTE CERTIFICATE", X509AttributeCertificateHolder.class),
        PRIVATE_KEY("PRIVATE KEY", PrivateKeyInfo.class),
        RSA_PRIVATE_KEY("RSA PRIVATE KEY", PrivateKeyInfo.class),
        DSA_PRIVATE_KEY("DSA PRIVATE KEY", PrivateKeyInfo.class),
        EC_PRIVATE_KEY("EC PRIVATE KEY", PrivateKeyInfo.class);
        
        private String pemType;
        private Class<? extends Encodable> clazz;
        
        private x509type(final String pemType, final Class<? extends Encodable> clazz) {
            this.pemType = pemType;
            this.clazz = clazz;
        }
        
        public final String getPemHeader() {
            return "-----BEGIN " + pemType + "-----" + System.getProperty("line.separator");
        }
        
        public final String getPemFooter() {
            return System.getProperty("line.separator") + "-----END " + pemType + "-----";
        }
        
        public final String getPemObjectType() {
            return pemType;
        }
        
        public final Class<? extends Encodable> getClazz() {
            return clazz;
        }
    }
    
    public static final byte[] derToPem(final String filename, final x509type type) throws IOException {
        byte[] buffer = Files.readAllBytes(Paths.get(filename));
        
        if ((char)buffer[0] != '-') { // DER can start with a digit
            final String header = type.getPemHeader();
            final int headerLength = header.length();
            final String footer = type.getPemFooter();
            final int footerLength = footer.length();
            final byte[] encoded = Base64.encode(buffer);
            final int encodedLength = encoded.length;
            
            final byte[] pemBuffer = new byte[headerLength + encodedLength + footerLength];
            for (int i = 0; i < headerLength; i++) {
                pemBuffer[i] = (byte)header.charAt(i);
            }
            for (int i = 0; i < encodedLength; i++) {
                pemBuffer[i+headerLength] = encoded[i];
            }
            for (int i = 0; i < footerLength; i++) {
                pemBuffer[i+headerLength+encodedLength] = (byte)footer.charAt(i);
            }
            buffer = pemBuffer;
        }
        
        return buffer;
    }
    
    @SuppressWarnings("unchecked")
    public static final <T extends Encodable> T loadCert(final String filename, final x509type type) throws IOException {
        T cert = null;
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(CliHelper.derToPem(filename, type)));
        PEMParser p = new PEMParser(isr);
        Object readObject = p.readObject();
        p.close();
        if (type.getClazz().isInstance(readObject)) {
            cert = (T)readObject;
        } else if ((type == x509type.RSA_PRIVATE_KEY || type == x509type.EC_PRIVATE_KEY || type == x509type.DSA_PRIVATE_KEY) && PEMKeyPair.class.isInstance(readObject)) {
            cert = (T)((PEMKeyPair)readObject).getPrivateKeyInfo();
        } else {
            throw new IllegalArgumentException(filename + " was not a valid " + type.getPemObjectType());
        }
        isr.close();
        return cert;
    }
    
    public static final String bytesToPem(final byte[] array, final x509type type) throws Exception {
        final String pem =  type.getPemHeader()
                            + Base64.toBase64String(array)
                            + type.getPemFooter();
        return pem;
    }
}