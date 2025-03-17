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

    [StructLayout(LayoutKind.Sequential)]
    public struct NvmeStorageProtocolSpecificData {
        // STORAGE_PROPERTY_QUERY: winioctl.h
        [MarshalAs(UnmanagedType.U4)] public StorageWinConstants.StorageProtocolType ProtocolType;
        [MarshalAs(UnmanagedType.U4)] public uint DataType;
        [MarshalAs(UnmanagedType.U4)] public uint ProtocolDataRequestValue;
        [MarshalAs(UnmanagedType.U4)] public uint ProtocolDataRequestSubValue;
        [MarshalAs(UnmanagedType.U4)] public uint ProtocolDataOffset;
        [MarshalAs(UnmanagedType.U4)] public uint ProtocolDataLength;
        [MarshalAs(UnmanagedType.U4)] public uint FixedProtocolReturnData;
        [MarshalAs(UnmanagedType.U4)] public uint ProtocolDataRequestSubValue2;
        [MarshalAs(UnmanagedType.U4)] public uint ProtocolDataRequestSubValue3;
        [MarshalAs(UnmanagedType.U4)] public uint ProtocolDataRequestSubValue4;
        
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4096)]
        public byte[] data;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct NvmeStoragePropertyQuery {
        [MarshalAs(UnmanagedType.U4)] public StorageWinConstants.StoragePropertyId PropertyId;
        [MarshalAs(UnmanagedType.U4)] public StorageWinConstants.StorageQueryType QueryType;
        public NvmeStorageProtocolSpecificData ProtocolSpecificData;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct NvmeStorageDataDescriptor {
        [MarshalAs(UnmanagedType.U4)] public uint Version;
        [MarshalAs(UnmanagedType.U4)] public uint Size;
        public NvmeStorageProtocolSpecificData ProtocolSpecificData;
    }
}
