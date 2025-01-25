using StorageLib.Windows;
using System.Runtime.Versioning;

namespace StorageAta.Windows;

[SupportedOSPlatform("windows")]
public class StorageAtaWinConstants {
    public static readonly uint IOCTL_ATA_PASS_THROUGH = StorageWin.CTL_CODE(StorageWinConstants.IOCTL_SCSI_BASE, 0x040B, StorageWinConstants.IoctlMethodCodes.METHOD_BUFFERED, StorageWinConstants.IoctlFileAccess.FILE_READ_ACCESS | StorageWinConstants.IoctlFileAccess.FILE_WRITE_ACCESS);
    public static readonly uint IOCTL_ATA_PASS_THROUGH_DIRECT = StorageWin.CTL_CODE(StorageWinConstants.IOCTL_SCSI_BASE, 0x040C, StorageWinConstants.IoctlMethodCodes.METHOD_BUFFERED, StorageWinConstants.IoctlFileAccess.FILE_READ_ACCESS | StorageWinConstants.IoctlFileAccess.FILE_WRITE_ACCESS);
    public static readonly uint IOCTL_ATA_MINIPORT = StorageWin.CTL_CODE(StorageWinConstants.IOCTL_SCSI_BASE, 0x040D, StorageWinConstants.IoctlMethodCodes.METHOD_BUFFERED, StorageWinConstants.IoctlFileAccess.FILE_READ_ACCESS | StorageWinConstants.IoctlFileAccess.FILE_WRITE_ACCESS);

    // Windows ATA Pass Through Flags: ntddscsi.h
    [Flags]
    public enum AtaPassThroughFlags {
        ATA_FLAGS_DRDY_REQUIRED = 0x01,
        ATA_FLAGS_DATA_IN = 0x02,
        ATA_FLAGS_DATA_OUT = 0x04,
        ATA_FLAGS_48BIT_COMMAND = 0x08,
        ATA_FLAGS_USE_DMA = 0x10,
        ATA_FLAGS_NO_MULTIPLE = 0x020
    }
}
