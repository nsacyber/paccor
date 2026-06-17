package paccor.cli.pv;

import java.io.File;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import picocli.CommandLine;
import picocli.CommandLine.TypeConversionException;

public class ReadableFileConverter implements CommandLine.ITypeConverter<File> {
    @Override
    public File convert(String value) throws TypeConversionException {
        File file = new File(value);
        try (InputStream ignored = Files.newInputStream(file.toPath())) {
            return file;
        } catch (AccessDeniedException e) {
            throw new TypeConversionException("File access permissions were denied: " + value);
        } catch (NoSuchFileException e) {
            throw new TypeConversionException("File does not exist: " + value);
        } catch (Exception e) {
            throw new TypeConversionException("Could not read file: " + value + " (" + e.getMessage() + ")");
        }
    }
}
