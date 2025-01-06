using StorageLib;
using System.Runtime.InteropServices;

namespace StorageWin;

public class StorageWinStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct CompletionQueueEntry {
        // COMPLETION_QUEUE_ENTRY
        [MarshalAs(UnmanagedType.U4)] public uint dw0;
        [MarshalAs(UnmanagedType.U4)] public uint dw1;
        [MarshalAs(UnmanagedType.U4)] public uint dw2;
        [MarshalAs(UnmanagedType.U4)] public uint dw3;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct IntelNvmePassthroughParameters {
        // NVME_PASS_THROUGH_PARAMETERS
        public StorageNvmeStructs.NvmeCommand Command;
        public byte IsIOCommandSet; // False/0 for Admin queue

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)] // IsIOCommandSet must conform to DWORD alignment.
        public byte[]
            IsIOCommandSetPadding; // If the size of IsIOCommandSet changes, this padding might need to be adjusted.

        public CompletionQueueEntry Completion;

        [MarshalAs(UnmanagedType.U4)]
        public uint DataBufferOffset; // Must be DWORD aligned offset from beginning of SRB_IO_CONTROL

        [MarshalAs(UnmanagedType.U4)] public uint DataBufferLength;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 40)]
        public byte[] Reserved;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct IntelNvmeIoctlPassthrough {
        // Intel NVME_IOCTL_PASS_THROUGH
        public SrbIoControl Header;
        public byte Version;
        public byte PathId;
        public byte TargetId;
        public byte Lun;
        public IntelNvmePassthroughParameters Parameters;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4096)]
        public byte[] data;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct SrbIoControl {
        // SRB_IO_CONTROL
        [MarshalAs(UnmanagedType.U4)] public uint HeaderLength;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] Signature;

        [MarshalAs(UnmanagedType.U4)] public uint Timeout;
        [MarshalAs(UnmanagedType.U4)] public uint ControlCode;
        [MarshalAs(UnmanagedType.U4)] public uint ReturnCode;
        [MarshalAs(UnmanagedType.U4)] public uint Length;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct StorageAdapterDescriptor {
        // STORAGE_ADAPTER_DESCRIPTOR: winioctl.h
        [MarshalAs(UnmanagedType.U4)] public uint Version;
        [MarshalAs(UnmanagedType.U4)] public uint Size;
        [MarshalAs(UnmanagedType.U4)] public uint MaximumTransferLength;
        [MarshalAs(UnmanagedType.U4)] public uint MaximumPhysicalPages;
        [MarshalAs(UnmanagedType.U4)] public uint AlignmentMask;
        public byte AdapterUsesPio;
        public byte AdapterScansDown;
        public byte CommandQueueing;
        public byte AcceleratedTransfer;
        [MarshalAs(UnmanagedType.U1)] public StorageWinConstants.StorageBusType BusType;
        [MarshalAs(UnmanagedType.U2)] public ushort BusMajorVersion;
        [MarshalAs(UnmanagedType.U2)] public ushort BusMinorVersion;
        public byte SrbType;
        public byte AddressType;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct StorageDeviceDescriptor {
        // STORAGE_DEVICE_DESCRIPTOR: winioctl.h
        [MarshalAs(UnmanagedType.U4)] public uint Version;
        [MarshalAs(UnmanagedType.U4)] public uint Size;
        [MarshalAs(UnmanagedType.U1)] public byte DeviceType;
        [MarshalAs(UnmanagedType.U1)] public byte DeviceTypeModifier;
        [MarshalAs(UnmanagedType.U1)] public byte RemovableMedia;
        [MarshalAs(UnmanagedType.U1)] public byte CommandQueueing;
        [MarshalAs(UnmanagedType.U4)] public uint VendorIdOffset;
        [MarshalAs(UnmanagedType.U4)] public uint ProductIdOffset;
        [MarshalAs(UnmanagedType.U4)] public uint ProductRevisionOffset;
        [MarshalAs(UnmanagedType.U4)] public uint SerialNumberOffset;
        [MarshalAs(UnmanagedType.U1)] public StorageWinConstants.StorageBusType BusType;
        [MarshalAs(UnmanagedType.U4)] public uint RawPropertiesLength;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 1)]
        public byte[] RawDeviceProperties;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct StoragePropertyQuery {
        // STORAGE_PROPERTY_QUERY: winioctl.h
        [MarshalAs(UnmanagedType.U4)] public StorageWinConstants.StoragePropertyId PropertyId;
        [MarshalAs(UnmanagedType.U4)] public StorageWinConstants.StorageQueryType QueryType;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 1)]
        public byte[] AdditionalParameters;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct StorageDescriptorHeader {
        // STORAGE_DESCRIPTOR_HEADER: winioctl.h
        [MarshalAs(UnmanagedType.U4)] public uint Version;
        [MarshalAs(UnmanagedType.U4)] public uint Size;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct ScsiAddress {
        [MarshalAs(UnmanagedType.U4)] public uint Length;
        public byte PortNumber;
        public byte PathId;
        public byte TargetId;
        public byte Lun;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct StorageDeviceNumber {
        // STORAGE_DEVICE_NUMBER: winioctl.h
        [MarshalAs(UnmanagedType.U4)] public uint DeviceType;
        [MarshalAs(UnmanagedType.U4)] public uint DeviceNumber;
        [MarshalAs(UnmanagedType.U4)] public uint PartitionNumber;
    }
}
