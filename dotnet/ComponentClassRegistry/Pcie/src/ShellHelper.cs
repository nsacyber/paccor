using System.Diagnostics;

namespace Pcie;
public static class ShellHelper {
    public static Task<Tuple<int, string, string>> Ethtool(string arguments) {
        Console.WriteLine("Run Ethtool");
        Console.WriteLine($"-c \"ethtool {arguments}\"");
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
        Process process = new() {
            StartInfo = info,
            EnableRaisingEvents = true
        };
        
        process.Exited += (sender, args) => {
            source.SetResult(new Tuple<int, string, string>(process.ExitCode, process.StandardError.ReadToEnd(), process.StandardOutput.ReadToEnd()));
            if (process.ExitCode != 0) {
                source.SetException(new Exception($"Command `{process.StartInfo.FileName} {process.StartInfo.Arguments}` failed with exit code `{process.ExitCode}`"));
            }

            process.Dispose();
        };

        try {
            Console.WriteLine("Before start");
            process.Start();
            Console.WriteLine("Before Wait");
            process.WaitForExit();
            Console.WriteLine("After Wait");
        } catch (Exception e) {
            source.SetException(e);
        } finally {
            process.Dispose();
        }

        return source.Task;
    }
}
