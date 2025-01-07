using StorageLib.Windows;
using System.Runtime.Versioning;

namespace StorageNvme.Windows;

[SupportedOSPlatform("windows")]
public class StorageNvmeWinConstants {
    // Intel RST Constants
    public static readonly string INTELNVM_SIGNATURE = "IntelNvm";
    public static readonly uint IOCTL_INTEL_NVME_PASSTHROUGH = StorageWin.CTL_CODE(0xF000, 0xA02, StorageWinConstants.IoctlMethodCodes.METHOD_BUFFERED, StorageWinConstants.IoctlFileAccess.FILE_ANY_ACCESS);//0xf0002808;
    public static readonly byte INTEL_NVME_PASS_THROUGH_VERSION = 1;
}
