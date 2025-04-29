using Microsoft.Win32.SafeHandles;
using StorageLib;
using StorageLib.Linux;
using System.Collections.Immutable;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageAta.Linux;

[SupportedOSPlatform("linux")]
public class StorageAtaLinux : IStorageAta {
    public bool CollectAtaData(out List<StorageAtaData> list, ImmutableList<StorageDiskDescriptor> disks) {
        list = new();
        bool noProblems = true;
        string[] matches = StorageLinux.GetPhysicalDevicePaths(disks, StorageLinuxConstants.BlockType.ATA);

        foreach (string devName in matches) {
            using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(devName);

            if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
                continue;
            }
            
            bool readPage03 = StorageAtaLinux.QueryAtaLogPage(
                                out byte[] page03Data, 
                                handle,
                                StorageAtaConstants.AtaLogAddress.IdentifyDeviceDataLog,
                                StorageAtaConstants.AtaIdentifyDeviceLogPage.SupportedCapabilities);
            bool useDma = true;
            if (!readPage03) { // Maybe DMA didn't work
                readPage03 = StorageAtaLinux.QueryAtaLogPage(
                                out page03Data,
                                handle,
                                StorageAtaConstants.AtaLogAddress.IdentifyDeviceDataLog,
                                StorageAtaConstants.AtaIdentifyDeviceLogPage.SupportedCapabilities,
                                StorageAtaConstants.AtaCommand.ReadLogExt);
                useDma = false;
            }

            if (!readPage03) {
                noProblems = false;
                continue;
            }

            bool readPage01 = StorageAtaLinux.QueryAtaLogPage(
                                out byte[] page01Data,
                                handle,
                                StorageAtaConstants.AtaLogAddress.IdentifyDeviceDataLog,
                                StorageAtaConstants.AtaIdentifyDeviceLogPage.IdentifyDeviceData,
                                useDma
                                    ? StorageAtaConstants.AtaCommand.ReadLogDmaExt
                                    : StorageAtaConstants.AtaCommand.ReadLogExt);

            if (!readPage01) {
                noProblems = false;
                continue;
            }

            bool readPage05 = StorageAtaLinux.QueryAtaLogPage(
                                out byte[] page05Data,
                                handle,
                                StorageAtaConstants.AtaLogAddress.IdentifyDeviceDataLog,
                                StorageAtaConstants.AtaIdentifyDeviceLogPage.AtaStrings,
                                useDma
                                    ? StorageAtaConstants.AtaCommand.ReadLogDmaExt
                                    : StorageAtaConstants.AtaCommand.ReadLogExt);

            if (!readPage05) {
                noProblems = false;
                continue;
            }

            bool build = StorageAtaData.Build(out StorageAtaData ataData, page01Data, page03Data, page05Data);


            if (!build) {
                continue;
            }

            list.Add(ataData);
        }

        return noProblems;
    }

    public static bool QueryAtaLogPage(out byte[] pageData, string devName,
        StorageAtaConstants.AtaLogAddress logNumber, StorageAtaConstants.AtaIdentifyDeviceLogPage pageNumber,
        StorageAtaConstants.AtaCommand command = StorageAtaConstants.AtaCommand.ReadLogDmaExt) {
        
        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(devName);

        return QueryAtaLogPage(out pageData, handle, logNumber, pageNumber, command);
    }

    public static bool QueryAtaLogPage(out byte[] pageData, SafeFileHandle handle,
        StorageAtaConstants.AtaLogAddress logNumber, StorageAtaConstants.AtaIdentifyDeviceLogPage pageNumber,
        StorageAtaConstants.AtaCommand command = StorageAtaConstants.AtaCommand.ReadLogDmaExt) {
        
        pageData = [];

        if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
            return false;
        }
        
        StorageAtaLinuxStructs.AtaPassThroughCdb16 cdb = StorageCommonHelpers.CreateStruct<StorageAtaLinuxStructs.AtaPassThroughCdb16>();
        StorageLinuxStructs.SgIoHdr sgIoHdr = StorageCommonHelpers.CreateStruct<StorageLinuxStructs.SgIoHdr>();

        StorageAtaLinuxConstants.AtaPassThroughProtocol cdbProtocol = StorageAtaLinuxConstants.AtaPassThroughProtocol.AtaPioExtend;
        StorageLinuxConstants.SgFlag sgFlags = 0;

        if (command == StorageAtaConstants.AtaCommand.ReadLogDmaExt) {
            cdbProtocol = StorageAtaLinuxConstants.AtaPassThroughProtocol.AtaDmaExtend;
            sgFlags |= StorageLinuxConstants.SgFlag.SG_FLAG_DIRECT_IO;
        }

        cdb.opcode = (byte)StorageAtaLinuxConstants.AtaPassThroughOpCodes.AtaPassThrough16;
        cdb.protocol = (byte)cdbProtocol;
        cdb.flags = (byte)StorageAtaLinuxConstants.AtaPassThroughFlags.AtaReadOneBlockWithCk;
        cdb.sector_count = 1;
        cdb.lba_low = (byte)logNumber;
        cdb.lba_mid = (byte)pageNumber;
        cdb.command = (byte)command;
        
        sgIoHdr.interface_id    = StorageLinuxConstants.InterfaceId.SCSI_GENERIC;
        sgIoHdr.dxfer_direction = StorageLinuxConstants.SgDxfer.SG_DXFER_FROM_DEV;
        sgIoHdr.cmd_len         = (byte)Marshal.SizeOf(cdb);
        sgIoHdr.mx_sb_len       = StorageLinuxConstants.SCSI_SB_LEN_MAX;
        sgIoHdr.iovec_count     = 0;
        sgIoHdr.dxfer_len       = StorageAtaConstants.ATA_LOG_SIZE_BYTES;
        sgIoHdr.timeout         = 15000;
        sgIoHdr.flags           = sgFlags;

        bool endResult = false;
        IntPtr dxferPtr = IntPtr.Zero;
        IntPtr cdbPtr = IntPtr.Zero;
        IntPtr sensePtr = IntPtr.Zero;
        IntPtr sgIoHdrPtr = IntPtr.Zero;
        try {
            // Allocate memory for the data buffers
            dxferPtr = Marshal.AllocHGlobal((int)sgIoHdr.dxfer_len );
            sensePtr = Marshal.AllocHGlobal(sgIoHdr.mx_sb_len);
            cdbPtr = Marshal.AllocHGlobal(Marshal.SizeOf(cdb));
            sgIoHdrPtr = Marshal.AllocHGlobal(Marshal.SizeOf(sgIoHdr));
            
            // Further set up
            Marshal.StructureToPtr(cdb, cdbPtr, true);
            StorageCommonHelpers.ZeroMemory(sensePtr, sgIoHdr.mx_sb_len);
            StorageCommonHelpers.ZeroMemory(dxferPtr, (int)sgIoHdr.dxfer_len);
            
            // Connect ptrs
            sgIoHdr.dxferp = dxferPtr;
            sgIoHdr.cmdp = cdbPtr;
            sgIoHdr.sbp = sensePtr;
            
            // Copy the data from the managed object to the buffer
            Marshal.StructureToPtr(sgIoHdr, sgIoHdrPtr, true);

            int result = StorageLinuxImports.ioctl(handle, StorageLinuxConstants.SG_IOCTL, sgIoHdrPtr);

            if (result < 0) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                sgIoHdr = Marshal.PtrToStructure<StorageLinuxStructs.SgIoHdr>(sgIoHdrPtr);
                pageData = StorageCommonHelpers.ConvertIntPtrToByteArray(dxferPtr, (int)StorageAtaConstants.ATA_LOG_SIZE_BYTES);
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(dxferPtr);
            Marshal.FreeHGlobal(sensePtr);
            Marshal.FreeHGlobal(cdbPtr);
            Marshal.FreeHGlobal(sgIoHdrPtr);
        }

        return endResult;
    }
}

