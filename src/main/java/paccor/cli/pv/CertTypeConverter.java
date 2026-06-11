package paccor.cli.pv;

import paccor.cert.CertType;
import picocli.CommandLine;

/**
 * Picocli converter for CertType enum.
 */
public class CertTypeConverter implements CommandLine.ITypeConverter<CertType> {
    @Override
    public CertType convert(String value) throws Exception {
        return CertType.valueOf(value.toUpperCase());
    }
}
