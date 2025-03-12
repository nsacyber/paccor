using StorageLib.Windows;
using System.Runtime.Versioning;
using static StorageLib.Windows.StorageWinConstants;

namespace StorageScsi.Windows;

[SupportedOSPlatform("windows")]
public class StorageScsiWinConstants {
    //public static readonly uint IOCTL_SCSI_PASS_THROUGH_DIRECT = StorageWin.CTL_CODE(IOCTL_SCSI_BASE, 0x0405, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS); // ntddscsi.h
    public static readonly uint IOCTL_SCSI_PASS_THROUGH_DIRECT = 0x4D014; // ntddscsi.h

    // ntddscsi.h
    public enum ScsiDataIn : byte {
        SCSI_IOCTL_DATA_OUT = 0,
        SCSI_IOCTL_DATA_IN = 1,
        SCSI_IOCTL_DATA_UNSPECIFIED = 2,
        SCSI_IOCTL_DATA_BIDIRECTIONAL = 3
    }
}