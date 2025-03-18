using System.Runtime.Versioning;

namespace StorageLib.Windows;

[SupportedOSPlatform("windows")]
public class StorageWinConstants {// Windows Constants
    public static readonly string DISK_HANDLE_PD = @"\\.\PhysicalDrive{0}"; // PD
    public static readonly string DISK_HANDLE_SCSI = @"\\.\Scsi{0}:"; // SCSI
    public static readonly uint NVME_PASS_THROUGH_SRB_IO_CODE = 0xe0002000;
    public static readonly uint IOCTL_SCSI_MINIPORT = 0x0004D008;
    public static readonly uint NVME_STORPORT_DRIVER = 0xE000;
    public static readonly uint FILE_DEVICE_CONTROLLER = 0x00000004; // winioctl.h
    public static readonly uint FILE_DEVICE_MASS_STORAGE = 0x0000002d; // winioctl.h
    public static readonly uint FILE_DEVICE_UNKNOWN = 0x00000022; // winioctl.h
    public static readonly uint IOCTL_STORAGE_BASE = FILE_DEVICE_MASS_STORAGE; // winioctl.h
    public static readonly uint IOCTL_STORAGE_QUERY_PROPERTY = StorageWin.CTL_CODE(IOCTL_STORAGE_BASE, 0x0500, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS); // winioctl.h
    public static readonly uint IOCTL_STORAGE_GET_DEVICE_NUMBER = StorageWin.CTL_CODE(IOCTL_STORAGE_BASE, 0x0420, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS); // winioctl.h
    public static readonly uint IOCTL_SCSI_BASE = FILE_DEVICE_CONTROLLER; // ntddscsi.h
    public static readonly uint IOCTL_PCI_READ_CONFIG = StorageWin.CTL_CODE(FILE_DEVICE_UNKNOWN, 0x800, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS);
    public static readonly uint IOCTL_SCSI_GET_ADDRESS = StorageWin.CTL_CODE(IOCTL_SCSI_BASE, 0x0406, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS); // ntddscsi.h

    // Windows IOCTL Buffered Methods: winioctl.h
    public enum IoctlMethodCodes {
        METHOD_BUFFERED = 0,
        METHOD_IN_DIRECT = 1,
        METHOD_OUT_DIRECT = 2,
        METHOD_NEITHER = 3
    }
    // Windows IOCTL File Access: winioctl.h
    [Flags]
    public enum IoctlFileAccess {
        FILE_ANY_ACCESS = 0,
        FILE_READ_ACCESS = 1,
        FILE_WRITE_ACCESS = 2
    }

    // Windows STORAGE_PROPERTY_ID: winioctl.h
    public enum StoragePropertyId : uint {
        StorageDeviceProperty = 0,
        StorageAdapterProperty,
        StorageDeviceIdProperty,
        StorageDeviceUniqueIdProperty,
        StorageDeviceWriteCacheProperty,
        StorageMiniportProperty,
        StorageAccessAlignmentProperty,
        StorageDeviceSeekPenaltyProperty,
        StorageDeviceTrimProperty,
        StorageDeviceWriteAggregationProperty,
        StorageDeviceDeviceTelemetryProperty,
        StorageDeviceLBProvisioningProperty,
        StorageDevicePowerProperty,
        StorageDeviceCopyOffloadProperty,
        StorageDeviceResiliencyProperty,
        StorageDeviceMediumProductType,
        StorageAdapterRpmbProperty,
        StorageAdapterCryptoProperty,
        StorageDeviceIoCapabilityProperty = 48,
        StorageAdapterProtocolSpecificProperty,
        StorageDeviceProtocolSpecificProperty,
        StorageAdapterTemperatureProperty,
        StorageDeviceTemperatureProperty,
        StorageAdapterPhysicalTopologyProperty,
        StorageDevicePhysicalTopologyProperty,
        StorageDeviceAttributesProperty,
        StorageDeviceManagementStatus,
        StorageAdapterSerialNumberProperty,
        StorageDeviceLocationProperty,
        StorageDeviceNumaProperty,
        StorageDeviceZonedDeviceProperty,
        StorageDeviceUnsafeShutdownCount,
        StorageDeviceEnduranceProperty,
        StorageDeviceLedStateProperty,
        StorageDeviceSelfEncryptionProperty = 64,
        StorageFruIdProperty,
        StorageStackProperty,
        StorageAdapterProtocolSpecificPropertyEx,
        StorageDeviceProtocolSpecificPropertyEx
    }

    // Windows STORAGE_QUERY_TYPE: winioctl.h
    public enum StorageQueryType : uint {
        PropertyStandardQuery = 0,
        PropertyExistsQuery,
        PropertyMaskQuery,
        PropertyQueryMaxDefined
    }

    // Windows STORAGE_BUS_TYPE: winioctl.h
    public enum StorageBusType : uint {
        BusTypeUnknown = 0x00,
        BusTypeScsi,
        BusTypeAtapi,
        BusTypeAta,
        BusType1394,
        BusTypeSsa,
        BusTypeFibre,
        BusTypeUsb,
        BusTypeRAID,
        BusTypeiScsi,
        BusTypeSas,
        BusTypeSata,
        BusTypeSd,
        BusTypeMmc,
        BusTypeVirtual,
        BusTypeFileBackedVirtual,
        BusTypeSpaces,
        BusTypeNvme,
        BusTypeSCM,
        BusTypeUfs,
        BusTypeNvmeof,
        BusTypeMax,
        BusTypeMaxReserved = 0x7F
    }

    // Windows STORAGE_PROTOCOL_TYPE: winioctl.h
    public enum StorageProtocolType : uint {
        ProtocolTypeUnknown = 0x00,
        ProtocolTypeScsi,
        ProtocolTypeAta,
        ProtocolTypeNvme,
        ProtocolTypeSd,
        ProtocolTypeUfs,
        ProtocolTypeProprietary = 0x7E,
        ProtocolTypeMaxReserved = 0x7F
    }

    public enum BlockType : uint {
        NOT_SUPPORTED,
        ATA,
        SCSI,
        NVME,
        RAID
    }
}
