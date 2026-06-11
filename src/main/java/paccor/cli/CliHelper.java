package paccor.cli;

import paccor.cert.CertKind;
import paccor.exception.CertificateLoadException;
import paccor.exception.PaccorException;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.encoders.Base64;

public class CliHelper {
    /**
	 * The types of PEM files supported by this program.
	 */
    @AllArgsConstructor
    @Getter
    public enum x509type {
        CERTIFICATE("CERTIFICATE", X509CertificateHolder.class),
        ATTRIBUTE_CERTIFICATE("ATTRIBUTE CERTIFICATE", X509AttributeCertificateHolder.class),
        PRIVATE_KEY("PRIVATE KEY", PrivateKeyInfo.class),
        RSA_PRIVATE_KEY("RSA PRIVATE KEY", PrivateKeyInfo.class),
        DSA_PRIVATE_KEY("DSA PRIVATE KEY", PrivateKeyInfo.class),
        EC_PRIVATE_KEY("EC PRIVATE KEY", PrivateKeyInfo.class);
        
        private final String pemType;
        private final Class<? extends Encodable> clazz;
        
        public final String getPemHeader() {
            return "-----BEGIN " + pemType + "-----" + System.lineSeparator();
        }
        
        public final String getPemFooter() {
            return System.lineSeparator() + "-----END " + pemType + "-----";
        }
        
        public final String getPemObjectType() {
            return pemType;
        }
    }
    
    /**
     * Read a file as bytes, convert to PEM, and blindly assign it the supplied PEM type.
     * If the file data appears to already be in PEM format,
     * the file data will remain unchanged. 
     * @param filename File name including the path
     * @param type {@link x509type} enumerated option
     * @return byte array of the file data in PEM format
     * @throws IOException If the file could not be read,
     * or any other reason from Files#readAllBytes(Path).
     */
    public static final byte[] derToPem(final String filename, final x509type type) throws IOException {
        byte[] buffer = Files.readAllBytes(Paths.get(filename));
        
        if ((char)buffer[0] != '-') { // DER can start with a digit
            buffer = bytesToPem(buffer, type).getBytes();
        }
        
        return buffer;
    }

    public static final boolean parsesAsPkc(final String b64) {
        try {
            ASN1Primitive obj = ASN1Primitive.fromByteArray(Base64.decode(b64));
            if (obj instanceof ASN1Sequence seq) {
                TBSCertificate.getInstance(seq);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static final boolean parsesAsAc(final String b64) {
        try {
            ASN1Primitive obj = ASN1Primitive.fromByteArray(Base64.decode(b64));
            if (obj instanceof ASN1Sequence seq) {
                AttributeCertificateInfo.getInstance(seq);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static final KeyStore.PasswordProtection getPassword(String prompt) {
        Console c = System.console();
        return new KeyStore.PasswordProtection(c.readPassword(prompt));
    }

    public static final KeyStore.PasswordProtection passwordProtection(char[] password) {
        return new KeyStore.PasswordProtection(password);
    }

    @SuppressWarnings("unchecked")
    public static final <T extends Encodable> T loadCert(final String filename, final x509type type) throws IOException {
        T cert = null;
        // Read and parse file
        Object readObject = readPemObject(CliHelper.derToPem(filename, type));
        
        // Retrieve desired type
        if (type.getClazz().isInstance(readObject)) {
            cert = (T)readObject;
        } else if ((type == x509type.RSA_PRIVATE_KEY || type == x509type.EC_PRIVATE_KEY || type == x509type.DSA_PRIVATE_KEY) && readObject instanceof PEMKeyPair pemKeyPairObj) {
            cert = (T)(pemKeyPairObj.getPrivateKeyInfo());
        } else {
            throw new IOException(filename + " was not a valid " + type.getPemObjectType());
        }
        return cert;
    }

    public static final <T extends Encodable> T loadCertSafe(final String filename, final x509type type) {
        try {
            return loadCertSafe(new File(filename), type);
        } catch (Exception e) {
            return null;
        }
    }
    public static final <T extends Encodable> T loadCertSafe(final File file, final x509type type) {
        try {
            return loadCert(file.getPath(), type);
        } catch (Exception e) {
            return null;
        }
    }
    public static final X509CertificateHolder loadPKC(final String filename) throws CertificateLoadException {
        try {
            return loadCert(filename, x509type.CERTIFICATE);
        } catch (IOException e) {
            throw new CertificateLoadException(filename, e);
        }
    }
    public static final X509AttributeCertificateHolder loadAC(final String filename) throws CertificateLoadException {
        try {
            return loadCert(filename, x509type.ATTRIBUTE_CERTIFICATE);
        } catch (IOException e) {
            throw new CertificateLoadException(filename, e);
        }
    }
    public static final X509CertificateHolder loadPKCSafe(final String filename) {
        try {
            return loadPKC(filename);
        } catch (Exception e) {
            return null;
        }
    }
    public static final X509AttributeCertificateHolder loadACSafe(final String filename) {
        try {
            return loadAC(filename);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object readPemObject(byte[] data) throws IOException {
        InputStreamReader isr = null;
        PEMParser p = null;
        Object readObject = null;
        try {
            isr = new InputStreamReader(new ByteArrayInputStream(data));
            p = new PEMParser(isr);
            readObject = p.readObject();
        } catch (Exception e) {
            // Check if it's "unexpected object: org.bouncycastle.asn1.ASN1Integer"
            if (e.getMessage() != null && e.getMessage().contains("ASN1Integer")) {
                throw new IOException("Failed to parse PEM object: likely trying to parse a PKC as an AC", e);
            }
            if (e instanceof IOException ioe) throw ioe;
            throw new IOException("Failed to read PEM object", e);
        } finally {
            if (p != null) {
                p.close();
            }
            if (isr != null) {
                isr.close();
            }
        }
        return readObject;
    }
    
    public static final String bytesToPem(final byte[] array, final x509type type) {
        return type.getPemHeader()
                + Base64.toBase64String(array)
                + type.getPemFooter();
    }

    public static byte[] bytesToPem(final byte[] array, CertKind type){
        return bytesToPem(array, type == CertKind.AC ? x509type.ATTRIBUTE_CERTIFICATE : x509type.CERTIFICATE).getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Assembles a DER-encoded sequence containing the provided data, algorithm identifier, and signature.
     * If an error occurs during encoding, a {@code PaccorException} is thrown.
     *
     * @param tbs the to-be-signed (TBS) data
     * @param algId the signature algorithm identifier
     * @param sig the digital signature
     * @return the DER-encoded ASN.1 sequence
     * @throws PaccorException if an I/O error occurs during DER encoding
     */
    public static final byte[] assembleDer(byte[] tbs, AlgorithmIdentifier algId, byte[] sig) throws PaccorException {
        ASN1Sequence seq;
        try {
            seq = new DERSequence(new ASN1Encodable[]{ ASN1Primitive.fromByteArray(tbs), algId, new DERBitString(sig) });
            return seq.getEncoded("DER");
        } catch (IOException e) {
            throw new PaccorException(ClientExitCodes.RUNTIME_ERROR, e);
        }
    }
}
