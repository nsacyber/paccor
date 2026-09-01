using System.Diagnostics;

namespace Pcie;
public static class ShellHelper {
    public static Task<Tuple<int, string, string>> Ethtool(string arguments) {
        ProcessStartInfo info = new() {
            FileName = "bash",
            Arguments = $"-c \"ethtool {arguments}\"",
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        return Execute(info);
    }
    public static Task<Tuple<int, string, string>> Powershell(string arguments) {
        char ch = '"'; // couldn't get escaping to work properly without this method
        ProcessStartInfo info = new() {
            FileName = "powershell.exe",
            Arguments = "-NoProfile -ExecutionPolicy Bypass -Command " + ch + arguments  + ch,
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
