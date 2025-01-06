using Microsoft.Win32.SafeHandles;
using StorageLib;
using System.Runtime.InteropServices;
using System.Text;

namespace StorageWin;

public class StorageWin {
    public static bool QueryNvmeCnsThruIntelRstDriver(int deviceNumber = 0) {
        bool result = QueryNvmeCnsThruIntelRstDriver(out StorageWinStructs.IntelNvmeIoctlPassthrough passThrough, deviceNumber, StorageWinConstants.NvmeCnsValue.IDENTIFY_CONTROLLER, 0);

        if (result) {

        }

        return result;
    }

    public static bool ConvertIntelNvmeIoctlPassthroughDataToNvmeControllerData(out StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl, StorageWinStructs.IntelNvmeIoctlPassthrough passThrough) {
        bool result = StorageCommonHelpers.ConvertBufferToNvmeControllerData(out nvmeCtrl, passThrough.data);

        return result;
    }


    // If Identify Namespace Data is needed: QueryNvmeCnsThruIntelRstDriver(out StorageWinStructs.IntelNvmeIoctlPassthrough passThrough2, deviceNumber, StorageWinConstants.NvmeCnsValue.IDENTIFY_NAMESPACE_FOR_NSID, 1);
    public static bool QueryNvmeCnsThruIntelRstDriver(out StorageWinStructs.IntelNvmeIoctlPassthrough passThrough, int deviceNumber = 0, StorageWinConstants.NvmeCnsValue cnsValue = StorageWinConstants.NvmeCnsValue.IDENTIFY_CONTROLLER, uint nsid = 0) {
        string deviceString = string.Format(StorageWinConstants.DISK_HANDLE_SCSI, deviceNumber);

        passThrough = StorageCommonHelpers.CreateStruct<StorageWinStructs.IntelNvmeIoctlPassthrough>();

        if (cnsValue == StorageWinConstants.NvmeCnsValue.IDENTIFY_NAMESPACE_FOR_NSID && nsid == 0) {
            return false;
        }

        SafeFileHandle handle = StorageWinHelpers.OpenDevice(deviceString);

        if (handle.IsInvalid) {
            return false;
        }

        bool endResult = false;
        IntPtr ptr = IntPtr.Zero;

        int allocationSize = Marshal.SizeOf<StorageWinStructs.IntelNvmeIoctlPassthrough>();
        int srbHeaderSize = Marshal.SizeOf<StorageWinStructs.SrbIoControl>();
        int dataBufferOffset = Marshal.OffsetOf<StorageWinStructs.IntelNvmeIoctlPassthrough>("data").ToInt32();

        try {
            passThrough.Header.HeaderLength = (uint)srbHeaderSize;
            passThrough.Header.Signature = Encoding.ASCII.GetBytes(StorageWinConstants.INTELNVM_SIGNATURE);
            passThrough.Header.Timeout = 15;
            passThrough.Header.ControlCode = StorageWinConstants.IOCTL_INTEL_NVME_PASSTHROUGH;
            passThrough.Header.Length = (uint)allocationSize - (uint)srbHeaderSize;
            passThrough.Version = StorageWinConstants.INTEL_NVME_PASS_THROUGH_VERSION;
            passThrough.TargetId = 0;
            passThrough.Lun = 0;

            // MAY NEED TO SET SCSI Handle and PathId
            // Handle = \\.\Scsi{deviceNumber}:
            // PathId = 2
            passThrough.PathId = 2; // TODO Pass in path id, possibly pass in object

            passThrough.Parameters.Command.Admin.opcode = (byte)StorageWinConstants.NvmeAdminCommandOpcode.IDENTIFY;
            passThrough.Parameters.IsIOCommandSet = 0; // 0 = Admin command set
            passThrough.Parameters.Command.Admin.nsid = nsid;
            passThrough.Parameters.Command.Admin.cdw10 = (uint)cnsValue;

            passThrough.Parameters.DataBufferLength = 4096;
            passThrough.Parameters.DataBufferOffset = (uint)dataBufferOffset;

            // Allocate memory for the buffer
            ptr = Marshal.AllocHGlobal(allocationSize);

            // Needed the starting address of the buffer for the next command to calculate the offset 
            passThrough.Parameters.Command.Admin.addr = (ulong)IntPtr.Add(ptr, (int)passThrough.Parameters.DataBufferOffset).ToInt64();

            // Copy the data from the managed object to the buffer
            Marshal.StructureToPtr(passThrough, ptr, true);

            // Prepare to talk to the device
            NativeOverlapped overlapped = new();
            int returnedLength = 0;
            Marshal.SetLastSystemError(0);

            bool validTransfer = StorageWinImports.DeviceIoControl(handle, StorageWinConstants.IOCTL_SCSI_MINIPORT, ptr, allocationSize, ptr, allocationSize, ref returnedLength, ref overlapped);

            if (!validTransfer) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                passThrough = Marshal.PtrToStructure<StorageWinStructs.IntelNvmeIoctlPassthrough>(ptr);
            }

            Console.WriteLine();
        } finally {
            Marshal.FreeHGlobal(ptr);

            if (!handle.IsClosed) {
                handle.Close();
            }
        }

        return endResult;
    }


    // device handle assumed to be open and valid
    public static bool GetScsiAddress(out StorageWinStructs.ScsiAddress scsiAddress, SafeFileHandle handle) {
        bool endResult = false;
        IntPtr ptr = IntPtr.Zero;

        try {
            scsiAddress = StorageCommonHelpers.CreateStruct<StorageWinStructs.ScsiAddress>();
            int nOutBufferSize = Marshal.SizeOf<StorageWinStructs.ScsiAddress>();
            ptr = Marshal.AllocHGlobal(nOutBufferSize);

            NativeOverlapped overlapped = new();
            int returnedLength = 0;
            Marshal.SetLastSystemError(0);

            bool validTransfer = StorageWinImports.DeviceIoControl(handle, StorageWinConstants.IOCTL_SCSI_GET_ADDRESS, IntPtr.Zero, 0, ptr, nOutBufferSize, ref returnedLength, ref overlapped);

            if (!validTransfer) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                scsiAddress = Marshal.PtrToStructure<StorageWinStructs.ScsiAddress>(ptr);
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(ptr);
        }

        return endResult;
    }

    // device handle assumed to be open already
    public static bool QueryStorageProperty(out IntPtr ptr, SafeFileHandle handle, StorageWinConstants.StoragePropertyId propertyId) {
        bool endResult = false;
        IntPtr inPtr = IntPtr.Zero;
        IntPtr outPtr = IntPtr.Zero;
        ptr = IntPtr.Zero;

        try {
            int querySize = Marshal.SizeOf<StorageWinStructs.StoragePropertyQuery>();
            int headerSize = Marshal.SizeOf<StorageWinStructs.StorageDescriptorHeader>();
            StorageWinStructs.StoragePropertyQuery query = StorageCommonHelpers.CreateStruct<StorageWinStructs.StoragePropertyQuery>();
            query.PropertyId = propertyId;
            query.QueryType = StorageWinConstants.StorageQueryType.PropertyStandardQuery;

            // Allocate memory for the buffer
            inPtr = Marshal.AllocHGlobal(querySize);
            outPtr = Marshal.AllocHGlobal(headerSize);

            // Copy the data from the managed object to the buffer
            Marshal.StructureToPtr(query, inPtr, true);

            NativeOverlapped overlapped = new();
            int returnedLength = 0;
            Marshal.SetLastSystemError(0);

            uint blah = StorageWinConstants.IOCTL_STORAGE_QUERY_PROPERTY;
            bool validTransfer = StorageWinImports.DeviceIoControl(handle, StorageWinConstants.IOCTL_STORAGE_QUERY_PROPERTY, inPtr, querySize, outPtr, headerSize, ref returnedLength, ref overlapped);

            if (!validTransfer) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                StorageWinStructs.StorageDescriptorHeader header = Marshal.PtrToStructure<StorageWinStructs.StorageDescriptorHeader>(outPtr);
                if (header.Size > 0) {
                    headerSize = (int)header.Size;
                }

                Marshal.FreeHGlobal(outPtr);
                outPtr = Marshal.AllocHGlobal(headerSize);

                validTransfer = StorageWinImports.DeviceIoControl(handle, StorageWinConstants.IOCTL_STORAGE_QUERY_PROPERTY, inPtr, querySize, outPtr, headerSize, ref returnedLength, ref overlapped);

                if (!validTransfer) {
                    int systemerror = Marshal.GetLastSystemError();
                    endResult = false;
                } else {
                    ptr = outPtr;
                    endResult = true;
                }
            }
        } finally {
            Marshal.FreeHGlobal(inPtr);
            if (!endResult) {
                Marshal.FreeHGlobal(outPtr);  // Don't remove out buffer if result was valid. It still need to be converted into a structure.
            }

        }

        return endResult;
    }

    // device handle assumed to be open and valid
    public static bool QueryStorageDeviceProperty(out StorageWinStructs.StorageDeviceDescriptor descriptor, SafeFileHandle handle) {
        descriptor = StorageCommonHelpers.CreateStruct<StorageWinStructs.StorageDeviceDescriptor>();
        bool endResult = QueryStorageProperty(out IntPtr ptr, handle, StorageWinConstants.StoragePropertyId.StorageDeviceProperty);

        if (endResult) {
            descriptor = Marshal.PtrToStructure<StorageWinStructs.StorageDeviceDescriptor>(ptr);
        }

        Marshal.FreeHGlobal(ptr);

        return endResult;
    }


    // device handle assumed to be open and valid
    public static bool QueryStorageAdapterProperty(out StorageWinStructs.StorageAdapterDescriptor descriptor, SafeFileHandle handle) {
        descriptor = StorageCommonHelpers.CreateStruct<StorageWinStructs.StorageAdapterDescriptor>();
        bool endResult = QueryStorageProperty(out IntPtr ptr, handle, StorageWinConstants.StoragePropertyId.StorageAdapterProperty);

        if (endResult) {
            descriptor = Marshal.PtrToStructure<StorageWinStructs.StorageAdapterDescriptor>(ptr);
        }

        Marshal.FreeHGlobal(ptr);

        return endResult;
    }
}
