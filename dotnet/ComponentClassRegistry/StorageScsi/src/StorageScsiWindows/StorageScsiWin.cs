using Microsoft.Win32.SafeHandles;
using StorageLib;
using StorageLib.Windows;
using StorageScsi;
using System.Buffers.Binary;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using static StorageLib.Windows.StorageWinStructs;

namespace StorageScsi.Windows;

[SupportedOSPlatform("windows")]
public class StorageScsiWin : IStorageScsi {
    public bool CollectScsiData(out List<StorageScsiData> list) {
        list = new();
        bool noProblems = true;

        int numPhysicalDisks = StorageWin.GetNumPhysicalDisks();
        for (int i = 0; i < numPhysicalDisks; i++) {
            string pdHandle = string.Format(StorageWinConstants.DISK_HANDLE_PD, i);

            using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(pdHandle);

            if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
                continue;
            }

            bool adapterDescriptorSuccess = StorageWin.QueryStorageAdapterProperty(out StorageWinStructs.StorageAdapterDescriptor adapterDescriptor, handle);

            if (!adapterDescriptorSuccess) {
                continue;
            }

            bool deviceDescriptorSuccess = StorageWin.QueryStorageDeviceProperty(out StorageWinStructs.StorageDeviceDescriptor deviceDescriptor, handle);

            if (!deviceDescriptorSuccess) {
                continue;
            }

            bool acceptableDeviceBusType = false;
            bool acceptableAdapterBusType = false;

            switch (adapterDescriptor.BusType) {
                case StorageWinConstants.StorageBusType.BusTypeiScsi:
                case StorageWinConstants.StorageBusType.BusTypeSas:
                case StorageWinConstants.StorageBusType.BusTypeScsi:
                    acceptableAdapterBusType = true;
                    break;
                default:
                    break;
            }

            switch (deviceDescriptor.BusType) {
                case StorageWinConstants.StorageBusType.BusTypeiScsi:
                case StorageWinConstants.StorageBusType.BusTypeSas:
                case StorageWinConstants.StorageBusType.BusTypeScsi:
                    acceptableDeviceBusType = true;
                    break;
                default:
                    break;
            }


            if (!acceptableAdapterBusType || !acceptableDeviceBusType) {
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
        StorageScsiWinStructs.ScsiPassThroughDirect passThrough = StorageCommonHelpers.CreateStruct<StorageScsiWinStructs.ScsiPassThroughDirect>();

        cdb.opcode = (byte)StorageScsiConstants.ScsiOpCode.INQUIRY;
        cdb.Byte1 = (byte)(vpd ? StorageScsiConstants.ScsiCdbByte1Flags.EVPD : StorageScsiConstants.ScsiCdbByte1Flags.NONE);
        cdb.PageCode = vpd ? vpdPage : (byte)0;
        cdb.AllocationLength = (ushort)data.Length;

        passThrough.Length = (ushort)Marshal.SizeOf(passThrough);
        passThrough.CdbLength = (byte)Marshal.SizeOf(cdb);
        passThrough.SenseInfoLength = 0;
        passThrough.DataIn = (byte)StorageScsiWinConstants.ScsiDataIn.SCSI_IOCTL_DATA_IN;
        passThrough.DataTransferLength = (uint)data.Length;
        passThrough.TimeOutValue = 15000;
        passThrough.SenseInfoOffset = passThrough.Length;

        Array.Copy(StorageCommonHelpers.CreateByteArray(cdb), passThrough.Cdb, passThrough.CdbLength);

        bool endResult = false;
        IntPtr dataPtr = IntPtr.Zero;
        IntPtr passThroughPtr = IntPtr.Zero;
        try {
            // Allocate memory for the data buffers
            dataPtr = Marshal.AllocHGlobal((int)passThrough.DataTransferLength);
            passThroughPtr = Marshal.AllocHGlobal(Marshal.SizeOf(passThrough));

            // Further set up
            StorageCommonHelpers.ZeroMemory(dataPtr, (int)passThrough.DataTransferLength);

            // Connect ptrs
            passThrough.DataBuffer = dataPtr;

            // Copy the data from the managed object to the buffer
            Marshal.StructureToPtr(passThrough, passThroughPtr, true);

            NativeOverlapped overlapped = new();
            int returnedLength = 0;
            Marshal.SetLastSystemError(0);

            // Prepare to talk to the device
            bool validTransfer = StorageWinImports.DeviceIoControl(handle, StorageScsiWinConstants.IOCTL_SCSI_PASS_THROUGH_DIRECT, passThroughPtr, passThrough.Length, passThroughPtr, passThrough.Length, ref returnedLength, ref overlapped);

            if (!validTransfer) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                passThrough = Marshal.PtrToStructure<StorageScsiWinStructs.ScsiPassThroughDirect>(passThroughPtr);
                data = StorageCommonHelpers.ConvertIntPtrToByteArray(dataPtr, (int)passThrough.DataTransferLength);
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(dataPtr);
            Marshal.FreeHGlobal(passThroughPtr);
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