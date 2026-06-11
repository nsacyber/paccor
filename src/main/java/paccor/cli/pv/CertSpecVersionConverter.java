package paccor.cli.pv;

import paccor.cert.CertSpecVersion;
import picocli.CommandLine;

/**
 * Picocli converter for CertSpecVersion enum.
 */
public class CertSpecVersionConverter implements CommandLine.ITypeConverter<CertSpecVersion> {
    @Override
    public CertSpecVersion convert(String value) throws Exception {
        return CertSpecVersion.fromString(value);
    }
}
