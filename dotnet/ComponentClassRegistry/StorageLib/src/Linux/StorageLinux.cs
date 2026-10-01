using System.Collections.Immutable;
using System.Runtime.Versioning;
using System.Text.RegularExpressions;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
internal static class StorageLinux {
    
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
    
    private static ImmutableList<StorageDiskDescriptor> AttemptPhysicalDevicePathResolution() {
        Dictionary<string, List<string>> disksById = ReadDisksById();
        List<StorageLinuxDiskDescriptor> matches = [];

        // Each entry in /sys/block is a whole disk. Its dev file holds major:minor. Sorted for a stable component order.
        foreach (string blockFolder in Directory.GetDirectories(StorageLinuxConstants.SYS_BLOCK_DIR).Order(StringComparer.Ordinal)) {
            string devFile = Path.Combine(blockFolder, "dev");
            if (!File.Exists(devFile)) {
                continue;
            }

            string majMin = File.ReadAllText(devFile).Trim();
            string devicePath = "/dev/" + Path.GetFileName(blockFolder).Replace('!', '/'); // sysfs encodes '/' in device names as '!'

            if (!Regex.IsMatch(majMin, "^[0-9]+:[0-9]+$") || !disksById.TryGetValue(devicePath, out List<string>? pathsById)) {
                continue;
            }

            int major = int.Parse(majMin.Split(':')[0]);
            matches.Add(new(devicePath, ClassifyBlockDevice(major, pathsById)));
        }

        return [.. matches]; // convert to ImmutableList
    }

    /// <summary>
    /// Maps each disk's device path to its /dev/disk/by-id links. Partitions and unsupported link types are skipped.
    /// </summary>
    private static Dictionary<string, List<string>> ReadDisksById() {
        Dictionary<string, List<string>> disksById = [];

        foreach (string link in Directory.EnumerateFileSystemEntries(StorageLinuxConstants.DISKS_BY_ID_DIR)) {
            string name = Path.GetFileName(link);
            if (name.Contains("-part") || !StorageLinuxConstants.SUPPORTED_BY_ID_PREFIXES.Any(name.StartsWith)) {
                continue;
            }

            FileSystemInfo? target = File.ResolveLinkTarget(link, returnFinalTarget: true);
            if (target == null) {
                continue;
            }

            if (!disksById.TryGetValue(target.FullName, out List<string>? links)) {
                links = [];
                disksById.Add(target.FullName, links);
            }
            links.Add(link);
        }

        return disksById;
    }

    private static StorageLinuxConstants.BlockType ClassifyBlockDevice(int major, List<string> pathsById) {
        return major switch {
            8 when HasByIdPrefix(pathsById, "ata-") => StorageLinuxConstants.BlockType.ATA,
            8 when HasByIdPrefix(pathsById, "scsi-") => StorageLinuxConstants.BlockType.SCSI,
            259 when HasByIdPrefix(pathsById, "nvme-") => StorageLinuxConstants.BlockType.NVME,
            _ => StorageLinuxConstants.BlockType.NOT_SUPPORTED
        };
    }

    private static bool HasByIdPrefix(List<string> pathsById, string prefix) {
        return pathsById.Any(path => path.StartsWith(StorageLinuxConstants.DISKS_BY_ID_DIR + "/" + prefix));
    }
}