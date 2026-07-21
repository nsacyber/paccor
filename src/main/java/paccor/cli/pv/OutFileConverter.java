package paccor.cli.pv;

import java.io.File;
import java.nio.file.Files;
import picocli.CommandLine;

public class OutFileConverter implements CommandLine.ITypeConverter<File> {
    @Override
    public File convert(String value) throws CommandLine.TypeConversionException {
        if (value == null || value.isBlank()) {
            throw new CommandLine.TypeConversionException("No file path given. Specify where to save the output.");
        }

        File file = new File(value);
        if (file.exists()) {
            if (!file.isFile()) {
                throw new CommandLine.TypeConversionException("Output path is not a file: " + value);
            }
            if (!Files.isWritable(file.toPath())) {
                throw new CommandLine.TypeConversionException("File access permissions were denied: " + value);
            }
        } else if (file.getParentFile() != null && !Files.isWritable(file.getParentFile().toPath())) {
            throw new CommandLine.TypeConversionException("Output directory is not writable: " + file.getParentFile());
        }
        return file;
    }
}
