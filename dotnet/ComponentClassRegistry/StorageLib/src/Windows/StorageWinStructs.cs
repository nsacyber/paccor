using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Windows;

[SupportedOSPlatform("windows")]
public class StorageWinStructs {
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
        [MarshalAs(UnmanagedType.U1)] public byte BusType; // StorageBusType is an enum (4 bytes), first 3 bytes should be truncated here
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
        [MarshalAs(UnmanagedType.U4)] public StorageWinConstants.StorageBusType BusType;
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
        public static bool operator ==(StorageDeviceNumber c1, StorageDeviceNumber c2) {
            return c1.Equals(c2);
        }

        public static bool operator !=(StorageDeviceNumber c1, StorageDeviceNumber c2) {
            return !c1.Equals(c2);
        }

        public override bool Equals(object? obj) {
            if (obj is not StorageDeviceNumber other) { // is not performs null check
                return false;
            }

            return DeviceType == other.DeviceType && DeviceNumber == other.DeviceNumber && PartitionNumber == other.PartitionNumber;
        }

        public override int GetHashCode() => (DeviceType, DeviceNumber, PartitionNumber).GetHashCode();
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct StorageProtocolSpecificData {
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
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct StorageProtocolDataDescriptor {
        [MarshalAs(UnmanagedType.U4)] public uint Version;
        [MarshalAs(UnmanagedType.U4)] public uint Size;
        public StorageProtocolSpecificData ProtocolSpecificData;
    }
}
