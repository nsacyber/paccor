using StorageLib.Windows;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageNvme.Windows;

[SupportedOSPlatform("windows")]
public class StorageNvmeWinStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct IntelNvmePassthroughParameters {
        // NVME_PASS_THROUGH_PARAMETERS
        public StorageNvmeStructs.NvmeCommand Command;
        public byte IsIOCommandSet; // False/0 for Admin queue

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)] // IsIOCommandSet must conform to DWORD alignment.
        public byte[]
            IsIOCommandSetPadding; // If the size of IsIOCommandSet changes, this padding might need to be adjusted.

        public StorageNvmeStructs.CompletionQueueEntry Completion;

        [MarshalAs(UnmanagedType.U4)]
        public uint DataBufferOffset; // Must be DWORD aligned offset from beginning of SRB_IO_CONTROL

        [MarshalAs(UnmanagedType.U4)] public uint DataBufferLength;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 40)]
        public byte[] Reserved;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct IntelNvmeIoctlPassthrough {
        // Intel NVME_IOCTL_PASS_THROUGH
        public StorageWinStructs.SrbIoControl Header;
        public byte Version;
        public byte PathId;
        public byte TargetId;
        public byte Lun;
        public IntelNvmePassthroughParameters Parameters;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4096)]
        public byte[] data;
    }
}
