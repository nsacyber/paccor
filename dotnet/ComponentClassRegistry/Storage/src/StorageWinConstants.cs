namespace StorageWin;

public class StorageWinConstants {
    // NVMe Constants
    // NVM Express® Base Specification, Revision 2.1, 5.1.13.1 Figure 310: Identify – CNS Values
    public enum NvmeCnsValue : uint {
        IDENTIFY_NAMESPACE_FOR_NSID = 0x0, // Identify Namespace data structure for the specified NSID
        IDENTIFY_CONTROLLER = 0x01, // Identify Controller data structure for the controller processing the command
        IDENTIFY_ACTIVE_NAMESPACE_LIST = 0x02, // Active Namespace ID list
        IDENTIFY_NSID_DESCRIPTOR_FOR_NSID = 0x03 // Namespace Identification Descriptor list for the specified NSID.
    }

    // NVM Express® Base Specification, Revision 2.1, 3.1.3.4 Command Support Requirements
    public enum NvmeAdminCommandOpcode : byte {
        GET_LOG_PAGE = 0x02,
        IDENTIFY = 0x06
    }

    // Windows Constants
    public static readonly string DISK_HANDLE_PD = @"\\.\PhysicalDrive{0}"; // PD
    public static readonly string DISK_HANDLE_SCSI = @"\\.\Scsi{0}:"; // SCSI
    public static readonly uint NVME_PASS_THROUGH_SRB_IO_CODE = 0xe0002000;
    public static readonly uint IOCTL_SCSI_MINIPORT = 0x0004D008;
    public static readonly uint NVME_STORPORT_DRIVER = 0xE000;
    public static readonly uint FILE_DEVICE_CONTROLLER = 0x00000004; // winioctl.h
    public static readonly uint FILE_DEVICE_MASS_STORAGE = 0x0000002d; // winioctl.h
    public static readonly uint FILE_DEVICE_UNKNOWN = 0x00000022; // winioctl.h
    public static readonly uint IOCTL_STORAGE_BASE = FILE_DEVICE_MASS_STORAGE; // winioctl.h
    public static readonly uint IOCTL_STORAGE_QUERY_PROPERTY = StorageWinHelpers.CTL_CODE(IOCTL_STORAGE_BASE, 0x0500, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS); // winioctl.h
    public static readonly uint IOCTL_STORAGE_GET_DEVICE_NUMBER = StorageWinHelpers.CTL_CODE(IOCTL_STORAGE_BASE, 0x0420, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS); // winioctl.h
    public static readonly uint IOCTL_SCSI_BASE = FILE_DEVICE_CONTROLLER; // ntddscsi.h
    public static readonly uint IOCTL_PCI_READ_CONFIG = StorageWinHelpers.CTL_CODE(FILE_DEVICE_UNKNOWN, 0x800, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS);
    public static readonly uint IOCTL_SCSI_GET_ADDRESS = StorageWinHelpers.CTL_CODE(IOCTL_SCSI_BASE, 0x0406, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS); // ntddscsi.h

    // Intel RST Constants
    public static readonly string INTELNVM_SIGNATURE = "IntelNvm";
    public static readonly uint IOCTL_INTEL_NVME_PASSTHROUGH = StorageWinHelpers.CTL_CODE(0xF000, 0xA02, IoctlMethodCodes.METHOD_BUFFERED, IoctlFileAccess.FILE_ANY_ACCESS);//0xf0002808;
    public static readonly byte INTEL_NVME_PASS_THROUGH_VERSION = 1;

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
        StorageAdapterProperty
    }

    // Windows STORAGE_QUERY_TYPE: winioctl.h
    public enum StorageQueryType : uint {
        PropertyStandardQuery = 0,
        PropertyExistsQuery,
        PropertyMaskQuery,
        PropertyQueryMaxDefined
    }

    // Windows STORAGE_BUS_TYPE: winioctl.h
    public enum StorageBusType : byte {
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
}
