package paccor.cli.pv;

import paccor.cert.CertKind;
import picocli.CommandLine;

public class CertKindConverter implements CommandLine.ITypeConverter<CertKind> {
    @Override
    public CertKind convert(String value) throws Exception {
        return CertKind.valueOf(value.toUpperCase());
    }
}
