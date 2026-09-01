using System.Collections.Immutable;
using System.Runtime.Versioning;
using System.Text.RegularExpressions;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public static class StorageLinux {
    
    public static string[] GetPhysicalDevicePaths(ImmutableList<StorageDiskDescriptor> paths, StorageLinuxConstants.BlockType type) {
        string[] matches = paths
                            .Where(x => (x is StorageLinuxDiskDescriptor descriptor) && descriptor.BlockType == type)
                            .Select(x => x.DiskId)
                            .Distinct()
                            .ToArray();
        return matches;
    }
    
    private const int DiscoveryAttempts = 3;

    public static ImmutableList<StorageDiskDescriptor> GetPhysicalDevicePaths() {
        Exception? lastException = null;

        for (int attempt = 0; attempt < DiscoveryAttempts; attempt++) {
            try {
                return AttemptPhysicalDevicePathResolution();
            }
            catch (Exception exception) {
                lastException = exception;

                if (attempt + 1 < DiscoveryAttempts) {
                    int delayMilliseconds = 100 * (1 << attempt);
                    Thread.Sleep(delayMilliseconds);
                }
            }
        }

        String errMsg = $"Unable to enumerate physical storage devices after {DiscoveryAttempts} attempts.";
        throw new InvalidOperationException(errMsg, lastException);
    }
    
    private static ImmutableList<StorageDiskDescriptor> AttemptPhysicalDevicePathResolution()
    {
        Tuple<int, string, string> lsblk =
            StorageLinuxImports.LsblkPhysicalDisks()
                .GetAwaiter()
                .GetResult();

        Tuple<int, string, string> byId =
            StorageLinuxImports.ListDisksById()
                .GetAwaiter()
                .GetResult();

        string lsblkOutput = lsblk.Item3; // lsblkOutput should have each PD on separate line with form: path maj:min
        string disksById = byId.Item3; // Custom format. Each line: path under /dev/,path under /dev/disk/by-id
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
        
        List<StorageLinuxDiskDescriptor> matches = [];
        string[] devs = lsblkOutput.Split('\n'); // each device in devs should have the form: path maj:min 
        foreach (string dev in devs) {
            string[] devInfo = dev.Split(' ', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);
            if (devInfo.Length >= 2 && Regex.IsMatch(devInfo[1], "^[0-9]+:[0-9]+$")) {
                if (!parsingDisksById.TryGetValue(devInfo[0], out List<string>? pathsById) || pathsById.Count == 0) {
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
                matches.Add(new (devInfo[0], type));
            }
        }
        
        return [.. matches]; // convert to ImmutableList
    }
}