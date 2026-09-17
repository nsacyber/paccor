using System.Diagnostics;
using System.Text.RegularExpressions;

namespace Pcie;
public static partial class ShellHelper {
    [GeneratedRegex(@"^[a-zA-Z0-9_-]{1,15}$")]
    private static partial Regex InterfaceNameRegex();

    public static Task<Tuple<int, string, string>> Ethtool(string interfaceName) {
        if (!InterfaceNameRegex().IsMatch(interfaceName)) {
            TaskCompletionSource<Tuple<int, string, string>> source = new();
            source.SetException(new Exception(""));
            return source.Task;
        }

        ProcessStartInfo info = new() {
            FileName = "ethtool",
            ArgumentList = { "-P", interfaceName },
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        return Execute(info);
    }

    public static Task<Tuple<int, string, string>> Powershell(string encodedCommand) {
        ProcessStartInfo info = new() {
            FileName = "powershell.exe",
            Arguments = $"-NoProfile -EncodedCommand {encodedCommand}",
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        return Execute(info);
    }

    private static Task<Tuple<int, string, string>> Execute(ProcessStartInfo info) {
        TaskCompletionSource<Tuple<int, string, string>> source = new();

        using Process process = new();
        process.StartInfo = info;
        process.EnableRaisingEvents = true;

        try {
            process.Start();

            Task<string> outputTask = process.StandardOutput.ReadToEndAsync();
            Task<string> errorTask = process.StandardError.ReadToEndAsync();

            process.WaitForExit();

            string output = outputTask.GetAwaiter().GetResult();
            string error = errorTask.GetAwaiter().GetResult();

            int exitCode = process.ExitCode;

            if (exitCode == 0) {
                source.SetResult(new Tuple<int, string, string>(exitCode, error, output));
            } else {
                if (error.IsWhiteSpace()) {
                    error = "<empty>";
                }
                error = "Error message: " + error;
                source.SetException(new Exception($"Command `{info.FileName} {info.Arguments}` failed with exit code `{exitCode}`. {error}"));
            }
        } catch (Exception e) {
            source.SetException(e);
        }

        return source.Task;
    }
}
