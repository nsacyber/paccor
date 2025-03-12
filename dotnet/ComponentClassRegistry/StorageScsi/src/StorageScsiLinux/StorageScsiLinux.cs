using Microsoft.Win32.SafeHandles;
using StorageLib;
using StorageLib.Linux;
using System.Buffers.Binary;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using static StorageLib.Windows.StorageWinStructs;

namespace StorageScsi.Linux;

[SupportedOSPlatform("linux")]
public class StorageScsiLinux : IStorageScsi {
    public bool CollectScsiData(out List<StorageScsiData> list) {
        list = new();
        bool noProblems = true;
        string[] matches = StorageLinux.GetPhysicalDevicePaths(StorageLinuxConstants.BlockType.SCSI);

        foreach (string devName in matches) {
            using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(devName);

            if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
                continue;
            }

            bool readInquiry = Inquiry(out byte[] inquiryData, handle);

            if (!readInquiry) {
                noProblems = false;
                continue;
            }

            bool readVpd80 = Inquiry(out byte[] vpd80, handle, true, (byte)StorageScsiConstants.ScsiPageCode.UNIT_SERIAL_NUMBER);

            if (!readVpd80) {
                noProblems = false;
                continue;
            }

            bool readVpd83 = Inquiry(out byte[] vpd83, handle, true, (byte)StorageScsiConstants.ScsiPageCode.DEVICE_IDENTIFICATION);

            if (!readVpd83) {
                noProblems = false;
                continue;
            }

            bool build = StorageScsiData.Build(out StorageScsiData scsiData, inquiryData, vpd80, vpd83);


            if (!build) {
                continue;
            }

            list.Add(scsiData);
        }

        return noProblems;
    }

    public static bool Inquiry(out byte[] data, SafeFileHandle handle, bool vpd = false, byte vpdPage = 0, uint dataLength = StorageScsiConstants.SCSI_INQUIRY_DATA_BUFFER_SIZE_CONST) {
        data = new byte[dataLength];

        if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
            return false;
        }

        StorageScsiStructs.ScsiCdb cdb = StorageCommonHelpers.CreateStruct<StorageScsiStructs.ScsiCdb>();
        StorageLinuxStructs.SgIoHdr sgIoHdr = StorageCommonHelpers.CreateStruct<StorageLinuxStructs.SgIoHdr>();

        cdb.opcode = (byte)StorageScsiConstants.ScsiOpCode.INQUIRY;
        cdb.Byte1 = (byte)(vpd ? StorageScsiConstants.ScsiCdbByte1Flags.EVPD : StorageScsiConstants.ScsiCdbByte1Flags.NONE);
        cdb.PageCode = vpd ? vpdPage : (byte)0;
        cdb.AllocationLength = (ushort)data.Length;

        sgIoHdr.interface_id    = StorageLinuxConstants.InterfaceId.SCSI_GENERIC;
        sgIoHdr.dxfer_direction = StorageLinuxConstants.SgDxfer.SG_DXFER_FROM_DEV;
        sgIoHdr.cmd_len         = (byte)Marshal.SizeOf(cdb);
        sgIoHdr.mx_sb_len       = StorageLinuxConstants.SCSI_SB_LEN_MAX;
        sgIoHdr.dxfer_len       = (uint)data.Length;
        sgIoHdr.timeout         = 15000;
        sgIoHdr.flags           = StorageLinuxConstants.SgFlag.SG_FLAG_DIRECT_IO;

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
                data = StorageCommonHelpers.ConvertIntPtrToByteArray(dxferPtr, data.Length);
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(dxferPtr);
            Marshal.FreeHGlobal(sensePtr);
            Marshal.FreeHGlobal(cdbPtr);
            Marshal.FreeHGlobal(sgIoHdrPtr);
        }

        uint newDataLength = (uint)(vpd ? (BinaryPrimitives.ReadInt16BigEndian(data.AsSpan()[2..4]) + 4) : (data[4] + 5));
        if (endResult && data.Length > 4 && newDataLength > dataLength) {
            // In VPD, Page length is 2 bytes and PAGE LENGTH = (n-3), where n is 0 based
            // In Inquiry data, using data[4]+5 because ADDITIONAL LENGTH = (n-4), where n is 0 based
            endResult = Inquiry(out data, handle, vpd, vpdPage, newDataLength);
        }

        return endResult;
    }
}