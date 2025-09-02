package cli.pv;

import cert.CertSigEncoding;
import picocli.CommandLine;

public class CertSigEncodingConverter implements CommandLine.ITypeConverter<CertSigEncoding> {
    @Override
    public CertSigEncoding convert(String value) throws Exception {
        return CertSigEncoding.valueOf(value.toUpperCase());
    }
}
