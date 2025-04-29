namespace StorageScsi;

public class StorageScsiConstants {
    public const uint SCSI_INQUIRY_DATA_BUFFER_SIZE_CONST = 36;
    public static readonly uint SCSI_INQUIRY_DATA_BUFFER_SIZE = SCSI_INQUIRY_DATA_BUFFER_SIZE_CONST;
    
    public enum ScsiOpCode : byte {
        INQUIRY = 0x12
    }

    [Flags]
    public enum ScsiCdbByte1Flags : byte {
        NONE = 0x0,
        EVPD = 0x01
    }

    public enum ScsiPageCode : byte {
        SUPPORTED_VPD_PAGES = 0x0,
        UNIT_SERIAL_NUMBER = 0x80,
        DEVICE_IDENTIFICATION = 0x83,
        SOFTWARE_INTERFACE_IDENTIFICATION = 0x84,
        MANAGEMENT_NETWORK_ADDRESSES = 0x85,
        EXTENDED_INQUIRY_DATA = 0x86,
        MODE_PAGE_POLICY = 0x87,
        SCSI_PORTS = 0x88,
        ATA_INFORMATION = 0x89,
        POWER_CONDITION = 0x8A,
        DEVICE_CONSTITUENTS = 0x8B,
        CFA_PROFILE_INFORMATION = 0x8C,
        POWER_CONSUMPTION = 0x8D,
        THIRD_PARTY_COPY = 0x8F,
        PROTOCOL_SPECIFIC_LOGICAL_UNIT_INFORMATION = 0x90,
        PROTOCOL_SPECIFIC_PORT_INFORMATION = 0x91,
        SCSI_FEATURE_SETS = 0x92,
    }
}