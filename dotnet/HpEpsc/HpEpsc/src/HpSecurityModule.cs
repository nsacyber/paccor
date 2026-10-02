using System.Diagnostics;
using System.Runtime.Versioning;
using System.Text;

namespace HpEpsc;

/// <summary>
/// Prototype bridge to HP's EpSC cmdlets
/// Took some work to get this to work with HP.Security or HP.Validator module
/// </summary>
[SupportedOSPlatform("windows")]
internal static class HpSecurityModule {
    private static readonly string PowerShellPath = Path.Combine(Environment.SystemDirectory, "WindowsPowerShell", "v1.0", "powershell.exe");
    private static readonly TimeSpan Timeout = TimeSpan.FromSeconds(60);

    public static bool TryGetCertificatesJson(out string json, out string error) {
        json = "";
        DirectoryInfo folder = Directory.CreateTempSubdirectory("paccor-epsc-");
        try {
            string outFile = Path.Combine(folder.FullName, "certs.json");
            // Some module releases emit one object per certificate, others one collection. foreach enumerates either into certificates.
            string commands =
                "$certs = @(foreach ($result in (Get-HPEpscCerts)) { foreach ($view in $result) { [pscustomobject]@{ Name = [string]$view.Name; Der = [Convert]::ToBase64String($view.Certificate.RawData) } } }); " +
                $"ConvertTo-Json -InputObject $certs -Compress | Set-Content -LiteralPath {Quote(outFile)} -Encoding Ascii";

            if (!Run(commands, out error) || !File.Exists(outFile)) {
                return false;
            }

            json = File.ReadAllText(outFile);
            return true;
        } finally {
            folder.Delete(true);
        }
    }

    public static bool TryGetBootlogEvidence(out byte[] evidence, out string error, byte[] nonce) {
        evidence = [];
        DirectoryInfo folder = Directory.CreateTempSubdirectory("paccor-epsc-");
        try {
            string outFile = Path.Combine(folder.FullName, "evidence.bin");
            string commands =
                $"$nonce = [byte[]]({string.Join(",", nonce)}); " +
                $"$null = Get-HPEpscBootlogEvidence -Nonce $nonce -OutFile {Quote(outFile)}";

            if (!Run(commands, out error) || !File.Exists(outFile)) {
                return false;
            }

            evidence = File.ReadAllBytes(outFile);
            return true;
        } finally {
            folder.Delete(true);
        }
    }

    private static bool Run(string commands, out string error) {
        string script = "$ErrorActionPreference = 'Stop'; $ProgressPreference = 'SilentlyContinue'; " +
            "try { $null = Get-Command Get-HPEpscCerts, Get-HPEpscBootlogEvidence } catch { [Console]::Out.WriteLine('The HP EpSC cmds were not found. Install HP.Security. Module search path: ' + $env:PSModulePath); exit 1 }; " +
            "try { " + commands + " } catch { [Console]::Out.WriteLine($_.Exception.Message); exit 1 }";
        ProcessStartInfo info = new() {
            FileName = PowerShellPath,
            ArgumentList = { "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-EncodedCommand", Convert.ToBase64String(Encoding.Unicode.GetBytes(script)) },
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        
        info.Environment["PSModulePath"] = WindowsPowerShellModulePath();

        using Process? process = Process.Start(info);
        if (process == null) {
            error = "Windows PowerShell could not be started";
            return false;
        }

        Task<string> stdout = process.StandardOutput.ReadToEndAsync();
        Task<string> stderr = process.StandardError.ReadToEndAsync();
        if (!process.WaitForExit(Timeout)) {
            process.Kill(entireProcessTree: true);
            error = "HP.Security did not respond within " + Timeout.TotalSeconds + " seconds";
            return false;
        }

        process.WaitForExit(); 
        error = string.IsNullOrWhiteSpace(stdout.Result) ? stderr.Result.Trim() : stdout.Result.Trim();
        return process.ExitCode == 0;
    }

    private static string WindowsPowerShellModulePath() {
        string[] paths = [
            Environment.GetEnvironmentVariable("PSModulePath", EnvironmentVariableTarget.User) ?? "",
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "WindowsPowerShell", "Modules"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "WindowsPowerShell", "Modules"),
            Environment.GetEnvironmentVariable("PSModulePath", EnvironmentVariableTarget.Machine) ?? ""
        ];
        return string.Join(';', paths.SelectMany(path => path.Split(';', StringSplitOptions.RemoveEmptyEntries)).Distinct(StringComparer.OrdinalIgnoreCase));
    }

    private static string Quote(string path) {
        return "'" + path.Replace("'", "''") + "'";
    }
}
