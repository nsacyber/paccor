using Microsoft.Win32.SafeHandles;
using StorageLib;
using StorageLib.Windows;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using System.Text;
using System.Threading.Tasks;

namespace StorageAta.Windows;

[SupportedOSPlatform("windows")]
public class StorageAtaWin : IStorageAta {
    public bool CollectAtaData(out List<StorageAtaData> list) {
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
                case StorageWinConstants.StorageBusType.BusTypeAta:
                case StorageWinConstants.StorageBusType.BusTypeSata:
                    acceptableAdapterBusType = true;
                    break;
                default:
                    break;
            }

            switch (deviceDescriptor.BusType) {
                case StorageWinConstants.StorageBusType.BusTypeAta:
                case StorageWinConstants.StorageBusType.BusTypeSata:
                    acceptableDeviceBusType = true;
                    break;
                default:
                    break;
            }


            if (!acceptableAdapterBusType || !acceptableDeviceBusType) {
                continue;
            }

            bool readPage03 = StorageAtaWin.QueryAtaLogPage(
                                out byte[] page03Data, 
                                handle,
                                StorageAtaConstants.AtaLogAddress.IdentifyDeviceDataLog,
                                StorageAtaConstants.AtaIdentifyDeviceLogPage.SupportedCapabilities);
            bool useDma = true;
            if (!readPage03) { // Maybe DMA didn't work
                readPage03 = StorageAtaWin.QueryAtaLogPage(
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

            bool readPage05 = StorageAtaWin.QueryAtaLogPage(
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

            bool build = StorageAtaData.Build(out StorageAtaData ataData, page03Data, page05Data);


            if (!build) {
                continue;
            }

            list.Add(ataData);
        }

        return noProblems;
    }

    public static bool QueryAtaLogPage(out byte[] pageData, int deviceNumber, StorageAtaConstants.AtaLogAddress logNumber, StorageAtaConstants.AtaIdentifyDeviceLogPage pageNumber, StorageAtaConstants.AtaCommand command = StorageAtaConstants.AtaCommand.ReadLogDmaExt) {
        pageData = [];
        string pdHandle = string.Format(StorageWinConstants.DISK_HANDLE_PD, deviceNumber);

        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(pdHandle);

        return QueryAtaLogPage(out pageData, handle, logNumber, pageNumber, command);
    }

    public static bool QueryAtaLogPage(out byte[] pageData, SafeFileHandle handle, StorageAtaConstants.AtaLogAddress logNumber, StorageAtaConstants.AtaIdentifyDeviceLogPage pageNumber, StorageAtaConstants.AtaCommand command = StorageAtaConstants.AtaCommand.ReadLogDmaExt) {
        pageData = [];

        if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
            return false;
        }

        bool endResult = false;
        IntPtr dataPtr = IntPtr.Zero;
        IntPtr passThroughStructPtr = IntPtr.Zero;

        StorageAtaWinStructs.AtaPassThroughDirect passThrough = StorageCommonHelpers.CreateStruct<StorageAtaWinStructs.AtaPassThroughDirect>();

        StorageAtaWinConstants.AtaPassThroughFlags ataFlags = StorageAtaWinConstants.AtaPassThroughFlags.ATA_FLAGS_DATA_IN | StorageAtaWinConstants.AtaPassThroughFlags.ATA_FLAGS_48BIT_COMMAND;

        if (command == StorageAtaConstants.AtaCommand.ReadLogDmaExt) {
            ataFlags |= StorageAtaWinConstants.AtaPassThroughFlags.ATA_FLAGS_USE_DMA;
        }

        passThrough.Length = (ushort)Marshal.SizeOf<StorageAtaWinStructs.AtaPassThroughDirect>();
        passThrough.AtaFlags = (ushort)(ataFlags);
        passThrough.DataTransferLength = StorageAtaConstants.ATA_LOG_SIZE_BYTES;
        passThrough.TimeOutValue = 15;
        passThrough.PreviousTaskFile = StorageCommonHelpers.CreateStruct<StorageAtaWinStructs.AtaTaskFile>();
        passThrough.CurrentTaskFile = StorageCommonHelpers.CreateStruct<StorageAtaWinStructs.AtaTaskFile>();
        passThrough.CurrentTaskFile.SectorCount = 1;
        passThrough.CurrentTaskFile.SectorNumber = (byte)logNumber;
        passThrough.CurrentTaskFile.CylinderLow = (byte)pageNumber;
        passThrough.CurrentTaskFile.Command = (byte)command;

        try {
            // Allocate memory for the data buffer
            dataPtr = Marshal.AllocHGlobal((int)StorageAtaConstants.ATA_LOG_SIZE_BYTES);

            // Save address in passthrough struct
            passThrough.DataBuffer = dataPtr;

            // Allocate memory for the pointer to the passthrough struct
            passThroughStructPtr = Marshal.AllocHGlobal((int)passThrough.Length);

            // Copy the data from the managed object to the pointer
            Marshal.StructureToPtr(passThrough, passThroughStructPtr, true);

            // Prepare to talk to the device
            NativeOverlapped overlapped = new();
            int returnedLength = 0;
            Marshal.SetLastSystemError(0);

            bool validTransfer = StorageWinImports.DeviceIoControl(handle, StorageAtaWinConstants.IOCTL_ATA_PASS_THROUGH_DIRECT, passThroughStructPtr, passThrough.Length, passThroughStructPtr, passThrough.Length, ref returnedLength, ref overlapped);

            if (!validTransfer) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                passThrough = Marshal.PtrToStructure<StorageAtaWinStructs.AtaPassThroughDirect>(passThroughStructPtr);
                pageData = StorageCommonHelpers.ConvertIntPtrToByteArray(dataPtr, (int)StorageAtaConstants.ATA_LOG_SIZE_BYTES);
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(dataPtr);
            Marshal.FreeHGlobal(passThroughStructPtr);
        }

        return endResult;
    }
}
