using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using System.Text.RegularExpressions;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageLinux {
    public static string[] GetPhysicalDevicePaths(StorageLinuxConstants.BlockType type) {
        List<Tuple<StorageLinuxConstants.BlockType, string>> paths = GetPhysicalDevicePaths();
        string[] matches = paths
                            .Where(x => x.Item1 == type)
                            .Select(x => x.Item2)
                            .Distinct()
                            .ToArray();
        return matches;
    }

    public static List<Tuple<StorageLinuxConstants.BlockType, string>> GetPhysicalDevicePaths() {
        // Lsblk is asked to output columns NAME,MAJ:MIN with paths in place of NAME and without headers 
        Task<Tuple<int, string, string>> task = Task.Run(StorageLinuxImports.LsblkPhysicalDisks);

        Tuple<int, string, string> results = task.Result;
        if (task.Exception != null) {
            return [];
        }

        string lsblkOutput = results.Item3; // lsblkOutput should have each PD on separate line with form: path maj:min

        task = Task.Run(StorageLinuxImports.ListDisksById);
        results = task.Result;
        if (task.Exception != null) {
            return [];
        }
        
        string disksById = results.Item3; // Custom format. Each line: path under /dev/,path under /dev/disk/by-id
        Dictionary<string, List<string>> parsingDisksById = [];
        foreach (string disk in disksById.Split(Environment.NewLine, StringSplitOptions.TrimEntries)) {
            string[] line = disk.Split(',', StringSplitOptions.TrimEntries);

            if (line.Length < 2) {
                continue;
            }
            
            if (!parsingDisksById.ContainsKey(line[0])) {
                parsingDisksById.Add(line[0], []);
            }

            List<string> value = parsingDisksById[line[0]];
            value.Add(line[1]);
            parsingDisksById[line[0]] = value;
        }
        
        List<Tuple<StorageLinuxConstants.BlockType, string>> matches = [];
        string[] devs = lsblkOutput.Split('\n'); // each device in devs should have the form: path maj:min 
        foreach (string dev in devs) {
            string[] devInfo = dev.Split(' ', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);
            if (devInfo.Length >= 2 && Regex.IsMatch(devInfo[1], "^[0-9]+:[0-9]+$")) {
                List<string> pathsById = parsingDisksById[devInfo[0]];
                if (pathsById.Count == 0) {
                    continue;
                }
                
                int maj = int.Parse(devInfo[1].Split(':')[0]);
                StorageLinuxConstants.BlockType type = StorageLinuxConstants.BlockType.NOT_SUPPORTED;
                switch (maj) {
                    case 8:
                        if (pathsById.Any(x => x.StartsWith("/dev/disk/by-id/ata-"))) {
                            type = StorageLinuxConstants.BlockType.ATA;
                        } else if (pathsById.Any(x => x.StartsWith("/dev/disk/by-id/scsi-"))) {
                            type = StorageLinuxConstants.BlockType.SCSI;
                        }
                        break;
                    case 259:
                        if (pathsById.Any(x => x.StartsWith("/dev/disk/by-id/nvme-"))) {
                            type = StorageLinuxConstants.BlockType.NVME;
                        }
                        break;
                }
                matches.Add(new Tuple<StorageLinuxConstants.BlockType, string>(type, devInfo[0]));
            }
        }
        
        return matches;
    }
}