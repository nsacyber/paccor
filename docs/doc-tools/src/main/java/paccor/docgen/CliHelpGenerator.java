package paccor.docgen;

import cli.RootCmd;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import picocli.CommandLine;

public final class CliHelpGenerator {

    private CliHelpGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path outputDir = Paths.get(System.getProperty("cli.help.output.dir", "build/schema/docs/cli-help"));
        Files.createDirectories(outputDir);

        CommandLine root = new CommandLine(new RootCmd());
        Map<String, CommandLine> commands = new LinkedHashMap<>();
        commands.put("root", root);
        commands.put("certgen", root.getSubcommands().get("certgen"));
        commands.put("assemble", root.getSubcommands().get("assemble"));
        commands.put("validate", root.getSubcommands().get("validate"));
        commands.put("view", root.getSubcommands().get("view"));

        for (Map.Entry<String, CommandLine> entry : commands.entrySet()) {
            writeUsage(entry.getValue(), outputDir.resolve(entry.getKey() + ".md"));
        }
    }

    private static void writeUsage(CommandLine cmd, Path outputFile) throws IOException {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        cmd.usage(writer);
        writer.flush();

        String markdown = "```text\n" + buffer + "```\n";
        Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, markdown, StandardCharsets.UTF_8);
    }
}
