package cli;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.security.auth.DestroyFailedException;
import java.security.cert.Certificate;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.encoders.Base64;

import key.SignerCredential;

public class CliHelper {    
	/**
	 * The types of PEM files supported by this program.
	 */
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
    
    /**
     * Read file, convert data to PEM, and blindly assign it the supplied PEM type.
     * If the file data appears to already be in PEM format,
     * the file data will remain unchanged. 
     * @param filename File name including path
     * @param type {@link x509type} enumerated option
     * @return byte array of the file data in PEM format
     * @throws IOException If the file could not be read,
     * or any other reason from {@link Files#readAllBytes(Path)}.
     */
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
    
    protected static KeyStore.PasswordProtection getPassword(String prompt) {
    	Console c = System.console();
    	return new KeyStore.PasswordProtection(c.readPassword(prompt));
    }
    
    public static final SignerCredential getKeyFromPkcs12(final String filename) throws IOException {
    	SignerCredential cred = null;
    	
    	KeyStore.PasswordProtection pw = null;
    	FileInputStream fis = null;
        
    	try {
    		fis = new FileInputStream(filename);
    		pw = getPassword("Enter password for " + filename + ":");
    		KeyStore p12 = KeyStore.getInstance("pkcs12");
	    	p12.load(fis, pw.getPassword());
	        Enumeration<String> e = p12.aliases();
	        boolean keyFound = false;
	        while (e.hasMoreElements()) {
	            String alias = (String) e.nextElement();
	            if (p12.isKeyEntry(alias)) {
	            	if (keyFound) {
	            		throw new IOException("The key store is too complex. Looking for one key. See user guide or github for discussion.");
	            	} else {
	            		keyFound = true;
	            		PrivateKey key = (PrivateKey)p12.getKey(alias, pw.getPassword());
	            		cred = SignerCredential.createCredential(key.getAlgorithm());
		    	        cred.loadPrivateKey(key);
		            	Certificate[] chain = p12.getCertificateChain(alias);
		            	if (chain != null && chain.length > 0 && chain[0] instanceof X509Certificate) {
		            	    cred.loadCertificate((X509Certificate)chain[0]);
		            	}
	            	}
	            }
	        }
	        fis.close();
	        pw.destroy();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | DestroyFailedException e) {
        	throw new IOException(e);
        } finally {
        	if (fis != null) {
        		fis.close();
        	}
        	try {
				if (pw != null) {
					pw.destroy();
				}
			} catch (DestroyFailedException e) {
			}
        }
    	return cred;
    }
    
    @SuppressWarnings("unchecked")
    public static final <T extends Encodable> T loadCert(final String filename, final x509type type) throws IOException {
        T cert = null;
        // Read and parse file
        InputStreamReader isr = null;
        PEMParser p = null;
        Object readObject = null;
        try {
        	isr = new InputStreamReader(new ByteArrayInputStream(CliHelper.derToPem(filename, type)));
        	p = new PEMParser(isr);
        	readObject = p.readObject();
        } finally {
	        if (p != null) {
	        	p.close();
	        }
	        if (isr != null) {
	        	isr.close();
	        }
        }
        
        // Retrieve desired type
        if (type.getClazz().isInstance(readObject)) {
            cert = (T)readObject;
        } else if ((type == x509type.RSA_PRIVATE_KEY || type == x509type.EC_PRIVATE_KEY || type == x509type.DSA_PRIVATE_KEY) && PEMKeyPair.class.isInstance(readObject)) {
            cert = (T)((PEMKeyPair)readObject).getPrivateKeyInfo();
        } else {
            throw new IOException(filename + " was not a valid " + type.getPemObjectType());
        }
        return cert;
    }
    
    public static final String bytesToPem(final byte[] array, final x509type type) throws Exception {
        final String pem =  type.getPemHeader()
                            + Base64.toBase64String(array)
                            + type.getPemFooter();
        return pem;
    }
}