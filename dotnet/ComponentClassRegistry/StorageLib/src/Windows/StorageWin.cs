using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;

namespace StorageLib.Windows;

public class StorageWin {
    public static uint CTL_CODE(uint deviceType, uint function, uint method, uint access) {
        return ((deviceType << 16) | (access << 14) | (function << 2) | method);
    }
    public static uint CTL_CODE(uint deviceType, uint function, StorageWinConstants.IoctlMethodCodes method, StorageWinConstants.IoctlFileAccess access) {
        return CTL_CODE(deviceType, function, (uint)method, (uint)access);
    }

    public static bool GetStorageDeviceNumber(out StorageWinStructs.StorageDeviceNumber sdn, int physicalDiskNumber) {
        string pdHandle = string.Format(StorageWinConstants.DISK_HANDLE_PD, physicalDiskNumber);
        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(pdHandle);
        return GetStorageDeviceNumber(out sdn, handle);
    }

    public static bool GetStorageDeviceNumber(out StorageWinStructs.StorageDeviceNumber sdn, SafeFileHandle handle) {
        bool endResult = false;
        sdn = StorageCommonHelpers.CreateStruct<StorageWinStructs.StorageDeviceNumber>();

        // device handle assumed to be open already
        if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
            return false;
        }

        IntPtr outPtr = IntPtr.Zero;

        // from cfgmgr32.h
        // DEVINST = uint
        // 
        int bufferSize = Marshal.SizeOf<StorageWinStructs.StorageDeviceNumber>();
        try {
            // Allocate memory for the buffer
            outPtr = Marshal.AllocHGlobal(bufferSize);

            // Prepare to talk to the device
            NativeOverlapped overlapped = new();
            int returnedLength = 0;
            Marshal.SetLastSystemError(0);

            bool validTransfer = StorageWinImports.DeviceIoControl(handle, StorageWinConstants.IOCTL_STORAGE_GET_DEVICE_NUMBER, IntPtr.Zero, 0, outPtr, bufferSize, ref returnedLength, ref overlapped);

            if (!validTransfer) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                sdn = Marshal.PtrToStructure<StorageWinStructs.StorageDeviceNumber>(outPtr);
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(outPtr);
        }

        return endResult;
    }

    public static bool GetScsiAddress(out StorageWinStructs.ScsiAddress scsiAddress, string deviceHandle) {
        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(deviceHandle);
        return GetScsiAddress(out scsiAddress, handle);
    }

    // device handle assumed to be open and valid
    public static bool GetScsiAddress(out StorageWinStructs.ScsiAddress scsiAddress, SafeFileHandle handle) {
        bool endResult = false;
        scsiAddress = StorageCommonHelpers.CreateStruct<StorageWinStructs.ScsiAddress>();
        IntPtr ptr = IntPtr.Zero;

        // device handle assumed to be open already
        if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
            return false;
        }

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

    public static bool QueryStorageProperty(out byte[] buffer, string deviceHandle, StorageWinConstants.StoragePropertyId propertyId) {
        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(deviceHandle);
        return QueryStorageProperty(out buffer, handle, propertyId);
    }

    public static bool QueryStorageProperty(out byte[] buffer, SafeFileHandle handle, StorageWinConstants.StoragePropertyId propertyId) {
        bool endResult = false;
        IntPtr inPtr = IntPtr.Zero;
        IntPtr outPtr = IntPtr.Zero;
        buffer = Array.Empty<byte>();

        // device handle assumed to be open already
        if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
            return false;
        }

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
                    buffer = StorageCommonHelpers.ConvertIntPtrToByteArray(outPtr, headerSize);
                    endResult = true;
                }
            }
        } finally {
            Marshal.FreeHGlobal(inPtr);
            Marshal.FreeHGlobal(outPtr);
        }

        return endResult;
    }

    public static bool QueryStorageDeviceProperty(out StorageWinStructs.StorageDeviceDescriptor descriptor, string deviceHandle) {
        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(deviceHandle);
        return QueryStorageDeviceProperty(out descriptor, handle);
    }

    public static bool QueryStorageDeviceProperty(out StorageWinStructs.StorageDeviceDescriptor descriptor, SafeFileHandle handle) {
        descriptor = StorageCommonHelpers.CreateStruct<StorageWinStructs.StorageDeviceDescriptor>();
        bool endResult = QueryStorageProperty(out byte[] buffer, handle, StorageWinConstants.StoragePropertyId.StorageDeviceProperty);

        if (endResult) {
            descriptor = StorageCommonHelpers.CreateStruct<StorageWinStructs.StorageDeviceDescriptor>(buffer);
        }

        return endResult;
    }

    public static bool QueryStorageAdapterProperty(out StorageWinStructs.StorageAdapterDescriptor descriptor, string deviceHandle) {
        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(deviceHandle);
        return QueryStorageAdapterProperty(out descriptor, handle);
    }

    public static bool QueryStorageAdapterProperty(out StorageWinStructs.StorageAdapterDescriptor descriptor, SafeFileHandle handle) {
        descriptor = StorageCommonHelpers.CreateStruct<StorageWinStructs.StorageAdapterDescriptor>();
        bool endResult = QueryStorageProperty(out byte[] buffer, handle, StorageWinConstants.StoragePropertyId.StorageAdapterProperty);

        if (endResult) {
            descriptor = StorageCommonHelpers.CreateStruct<StorageWinStructs.StorageAdapterDescriptor>(buffer);
        }

        return endResult;
    }
}
