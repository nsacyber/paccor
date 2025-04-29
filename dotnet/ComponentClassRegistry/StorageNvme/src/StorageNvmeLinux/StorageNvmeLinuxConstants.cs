using System.Runtime.Versioning;

namespace StorageNvme.Linux;

[SupportedOSPlatform("linux")]
public class StorageNvmeLinuxConstants {
    // Linux nvme_ioctl.h
    internal static readonly uint NVME_IOCTL_ID = 0xC0484E40;
    internal static readonly uint NVME_IOCTL_ADMIN_CMD = 0xC0484E41;
}
