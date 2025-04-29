using System.Runtime.Versioning;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageLinuxConstants {
    public static readonly byte SCSI_SB_LEN_MAX = 0xFF; 
    /**
     * synchronous SCSI command ioctl, (only in version 3 interface)
     * similar effect as write() followed by read()
     */
    public static readonly uint SG_IOCTL = 0x2285;
    public enum InterfaceId : int {
        /**
         * parallel port generic driver (pg) uses the letter 'P' to identify itself
         */
        PARALLEL_GENERIC = 'P',
        /**
         * scsi port generic driver (sg) uses the letter 'S' to identify itself
         */
        SCSI_GENERIC = 'S'
    }
    
    public enum SgDxfer : int {
        /**
         * e.g. a SCSI Test Unit Ready command
         */
        SG_DXFER_NONE = -1,
        /**
         * e.g. a SCSI WRITE command
         */
        SG_DXFER_TO_DEV = -2,
        /**
         * e.g. a SCSI READ command
         */
        SG_DXFER_FROM_DEV = -3,
        /**
         * treated like SG_DXFER_FROM_DEV  with the additional property
         * than during indirect IO user buffer is copied into the kernel
         * buffers before the transfer */
        SG_DXFER_TO_FROM_DEV = -4,
        /**
         * Unknown data direction
         */
        SG_DXFER_UNKNOWN = -5
    }

    public enum CmdLen : byte {
        ATA_PT_CDB_12 = 12,
        ATA_PT_CDB_16 = 16,
        ATA_PT_CDB_32 = 32
    }

    [Flags]
    public enum SgFlag : uint {
        /**
         * default is indirect IO
         */
        SG_FLAG_DIRECT_IO = 1,
        /**
         * default is to put device's lun into the 2nd byte of SCSI command
         */
        SG_FLAG_LUN_INHIBIT = 2,
        /**
         * selects memory mapped IO. Introduced in ersion 3.1.22 . May not
         * be present in GNU library headders for some time
         */
        SG_FLAG_MMAP_IO = 4,
        /**
         * no transfer of kernel buffers to/from user space (debug indirect IO)
         */
        SG_FLAG_NO_DXFER = 0x10000
    }

    [Flags]
    public enum SgInfo : uint {
        SG_INFO_OK_MASK = 0x1,
        /**
         * no scsi sense data, host nor driver "noise"
         */
        SG_INFO_OK = 0x0,
        /**
         * something abnormal happened
         */
        SG_INFO_CHECK = 0x1,
        SG_INFO_DIRECT_IO_MASK = 0x6,
        /**
         * data xfer via kernel buffers (or no xfer)
         */
        SG_INFO_INDIRECT_IO = 0x0,
        SG_INFO_DIRECT_IO = 0x2,
        /**
         * part direct, part indirect IO
         */
        SG_INFO_MIXED_IO = 0x4
    }

    public enum LinuxAllocatedDevices : uint {
        SCSI = 8,
        NVME = 259,
        BLOCK_EXTENDED_MAJOR = 259
    }

    public enum BlockType : uint {
        NOT_SUPPORTED,
        ATA,
        SCSI,
        NVME
    }
}
