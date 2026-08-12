using System.Diagnostics;

namespace paccor_scripts {
    public static class ShellHelper {
        public static Task<Tuple<int, string, string>> Bash(this string cmd) {
            ProcessStartInfo info = new() {
                FileName = "/bin/bash",
                ArgumentList = { cmd, "/dev/stdout" },
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true
            };
            return Execute(info);
        }

        public static Task<Tuple<int, string, string>> Powershell(this string cmd, string outFile) {
            char ch = '"'; // couldn't get escaping to work properly without this method
            ProcessStartInfo info = new() {
                FileName = "powershell.exe",
                Arguments = "-NoProfile -ExecutionPolicy Bypass -File " + ch + cmd + ch + " " + ch + outFile + ch,
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
                
                string output = process.StandardOutput.ReadToEnd();
                string error = process.StandardError.ReadToEnd();
                
                process.WaitForExit();
                
                int exitCode = process.ExitCode;

                if (exitCode == 0) {
                    source.SetResult(new Tuple<int, string, string>(exitCode, error, output));
                } else {
                    source.SetException(new Exception($"Command `{info.FileName} {info.Arguments}` failed with exit code `{exitCode}`"));
                }
            } catch (Exception e) {
                source.SetException(e);
            }

            return source.Task;
        }
    }
}
