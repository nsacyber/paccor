using PcieLib;
using PcieWinCfgMgr;
using System.Runtime.InteropServices;

namespace Pcie;

public class Pcie {

    public IDictionary<int, IList<PcieDevice>> Devices {
        get;
        private init;
    } = new Dictionary<int, IList<PcieDevice>>();

    /// <summary>
    /// True if construction was successful. False if any errors found.
    /// </summary>
    public bool Valid {
        get;
        private set;
    }

    public static Pcie GetPcie() {
        Pcie pcie = new();

        if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            pcie.Valid = CollectPcieWindows(pcie.Devices);
        } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
            pcie.Valid = CollectPcieLinux(pcie.Devices);
        }

        // Parse Data should be the same on Windows and Linux if I can get raw PCIe config data on Windows.

        return pcie;
    }

    private static bool CollectPcieWindows(IDictionary<int, IList<PcieDevice>> devices) {
        if (!RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            return false;
        }

        bool gotInstanceIds = PciWinCfgMgr.GetAllPciDeviceInstanceIds(out List<string> pciDeviceInstanceIds);

        foreach (string pciDeviceInstanceId in pciDeviceInstanceIds) {
            bool gotConfig = PciWinCfgMgr.CreateMockConfigBufferFromPciDeviceInstanceId(out byte[] config, out bool isLittleEndian, pciDeviceInstanceId);

            if (!gotConfig) {
                continue;
            }

            PcieDevice device = new(config, Array.Empty<byte>(), isLittleEndian);

            // For Network Adapters, attempt to get the MAC
            switch (device.ClassCode.Hex[..4]) {
                case "0200":
                case "0280":
                case "0D11":
                    // Ask NetAdapter for the permanent address.
                    Task<Tuple<int, string, string>> task = Task.Run(() => PowershellMAC(pciDeviceInstanceId));
                    task.Wait(5000);
                    bool foundMac = ParseMacAddressFromResults(out string mac, task);
                    if (foundMac) {
                        device.NetworkMac = Convert.FromHexString(mac);
                    }
                    break;
            }

            if (!devices.ContainsKey(device.ClassCode.Class)) {
                devices.Add(device.ClassCode.Class, new List<PcieDevice>());
            }
            devices[device.ClassCode.Class].Add(device);
        }

        return true;
    }

    private static bool CollectPcieLinux(IDictionary<int, IList<PcieDevice>> devices) {
        if (!RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
            return false;
        }
        // Is this program running with elevated privileges?
        if (LinuxImports.geteuid() != 0) {
            return false;
        }

        const string pciBusFolderRoot = "/sys/bus/pci/devices";
        string[] deviceFolders = Directory.GetDirectories(pciBusFolderRoot);
        foreach (string folder in deviceFolders) {
            string folderPath = Path.GetFullPath(Path.Combine(pciBusFolderRoot, folder));
            
            // Define paths to collect PCIe data made available by the Linux Kernel
            string configFile = Path.GetFullPath(Path.Combine(folderPath, "config"));
            string vpdFile = Path.GetFullPath(Path.Combine(folderPath, "vpd"));
            string netFolder = Path.GetFullPath(Path.Combine(folderPath, "net"));
            byte[] configBytes = Array.Empty<byte>();
            byte[] vpdBytes = Array.Empty<byte>();

            // Read bytes from these files. If there's no config header, don't collect vpd.
            if (File.Exists(configFile)) {
                FileInfo configFileInfo = new(configFile);
                configBytes = File.ReadAllBytes(configFile);
                if (File.Exists(vpdFile)) {
                    vpdBytes = File.ReadAllBytes(vpdFile);
                }
            }

            if (configBytes.Length <= 0) {
                continue;
            }

            PcieDevice device = new(configBytes, vpdBytes);

            // For Network Adapters, attempt to get the MAC
            if (Directory.Exists(netFolder)) {
                string[] interfaceFolders = Directory.GetDirectories(netFolder);
                
                if (interfaceFolders.Length >= 1) {
                    string interfaceName = Path.GetRelativePath(netFolder, interfaceFolders[0]);
                    switch (device.ClassCode.Hex[..4]) {
                        case "0200":
                        case "0280":
                        case "0D11":
                            // Ask ethtool for the permanent address.
                            Task<Tuple<int, string, string>> task = Task.Run(() => EthtoolP(interfaceName));
                            task.Wait(5000);
                            bool foundMac = ParseMacAddressFromResults(out string mac, task);
                            if (foundMac) {
                                device.NetworkMac = Convert.FromHexString(mac);
                            }
                            break;
                    }
                }
            }
            
            if (!devices.ContainsKey(device.ClassCode.Class)) {
                devices.Add(device.ClassCode.Class, new List<PcieDevice>());
            }
            devices[device.ClassCode.Class].Add(device);
        }

        return true;
    }

    private static bool ParseMacAddressFromResults(out string mac, Task<Tuple<int, string, string>> task) {
        mac = "";
        bool result = false;

        Console.WriteLine("Task Status: " + task.Status);
        if (task.IsCompleted) {
            if (task.IsCompletedSuccessfully) {
                Console.WriteLine("Before result");
                Tuple<int, string, string> results = task.Result;
                Console.WriteLine("Before Item");
                mac = results.Item3;
                Console.WriteLine("After Item");
                // Parse results of  output
                if (!string.IsNullOrWhiteSpace(mac)) {
                    mac = CleanMacAddress(mac);
                    result = true;
                }
            }
        }

        return result;
    }

    public static string CleanMacAddress(string mac) {
        mac = mac.Replace("Permanent address", "");
        mac = mac.Replace(":", "");
        mac = mac.Replace("-", "");
        mac = mac.Trim();
        return mac;
    }

    private static async Task<Tuple<int, string, string>> EthtoolP(string interfaceName) {
        return await ShellHelper.Ethtool("-P " + interfaceName);
    }
    private static async Task<Tuple<int, string, string>> PowershellMAC(string interfaceId) {
        return await ShellHelper.Powershell("Get-NetAdapter | where PNPDeviceID -eq '" + interfaceId + "' | select MacAddress -ExpandProperty MacAddress");
    }
}