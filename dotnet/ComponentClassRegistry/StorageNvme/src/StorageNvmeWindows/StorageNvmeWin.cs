using Microsoft.Win32.SafeHandles;
using PcieLib;
using PcieWinCfgMgr;
using StorageLib;
using StorageLib.Windows;
using StorageNvme;
using System;
using System.Runtime.InteropServices;
using System.Text;

namespace StorageNvme.Windows;
public class StorageNvmeWin : IStorageNvme {
    public bool CollectNvmeData(out List<StorageNvmeData> list) {
        list = new();
        bool noProblems = true;

        int numPhysicalDisks = GetNumPhysicalDisks();
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

            StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl = new();
            bool nvmeCtrlRead = false;

            if (deviceDescriptor.BusType == StorageWinConstants.StorageBusType.BusTypeNvme) {

                if (adapterDescriptor.BusType == StorageWinConstants.StorageBusType.BusTypeRAID) {
                    bool scsiAddressSuccess = StorageWin.GetScsiAddress(out StorageWinStructs.ScsiAddress scsiAddress, handle);

                    if (!scsiAddressSuccess) {
                        continue;
                    }

                    // Needed to collect identify namespace data
                    int nsid = scsiAddress.Lun + 1;

                    // Try to use Intel Passthrough
                    bool nvmePassthroughSuccess = QueryNvmeCnsThruIntelRstDriver(out StorageNvmeWinStructs.IntelNvmeIoctlPassthrough passThrough, i, scsiAddress, StorageNvmeConstants.NvmeCnsValue.IDENTIFY_CONTROLLER, 0);

                    if (nvmePassthroughSuccess) {
                        nvmeCtrl = StorageCommonHelpers.CreateStruct<StorageNvmeStructs.NvmeIdentifyControllerData>(passThrough.data);
                        nvmeCtrlRead = true;
                    }
                } else {
                    // Standard Query
                }
            }

            if (!nvmeCtrlRead) {
                continue;
            }

            bool pciRead = GetPciConfigFromCfgMgr(out byte[] config, i);

            if (nvmeCtrlRead && pciRead) {
                PcieDevice pciDev = new(config, Array.Empty<byte>());
                StorageNvmeData nvmeData = new(nvmeCtrl, pciDev.ClassCode);
                list.Add(nvmeData);
            }
            noProblems = noProblems && nvmeCtrlRead && pciRead;
        }

        return noProblems;
    }

    public static int GetNumPhysicalDisks() {
        int num = 2048; 
        Task<Tuple<int, string, string>> task = Task.Run(() => PowershellNumPhysicalDisks());
        Tuple<int, string, string> results = task.Result;
        if (task.Exception == null) {
            num = int.Parse(results.Item3);
        }

        return num;
    }

    private static async Task<Tuple<int, string, string>> PowershellNumPhysicalDisks() {
        return await StorageWinImports.Powershell("((Get-PhysicalDisk).DeviceId).Count");
    }

    public static bool GetPciConfigFromCfgMgr(out byte[] config, int diskNumber) {
        config = Array.Empty<byte>();

        bool sdnFound = StorageWin.GetStorageDeviceNumber(out StorageWinStructs.StorageDeviceNumber sdn, diskNumber);

        if (!sdnFound) {
            return false;
        }

        bool pciListFound = PciWinCfgMgr.GetDiskDevInterfaces(out List<string> diskDeviceInterfaceIdsW);

        if (!pciListFound) {
            return false;
        }

        bool foundDeviceInterfaceId = false;
        string instanceId = string.Empty;
        // Find the device instance id that matches the storage device number
        //    Open each disk device interface
        //    Query its storage device number
        //    If a match is found, save the device interface id that matches the disk number
        foreach (string deviceInterfaceIdW in diskDeviceInterfaceIdsW) {
            using SafeFileHandle diskHandle = StorageCommonHelpers.OpenDevice(deviceInterfaceIdW);
            bool functionWorked = StorageWin.GetStorageDeviceNumber(out StorageWinStructs.StorageDeviceNumber candidateSdn, diskHandle);

            if (sdnFound && functionWorked && sdn == candidateSdn) {
                foundDeviceInterfaceId = true;
                instanceId = deviceInterfaceIdW;
                break;
            }
        }

        if (!foundDeviceInterfaceId) {
            return false;
        }

        bool gotInstanceId = PciWinCfgMgr.GetDeviceInterfaceInstanceId(out string deviceInstanceId, instanceId);

        if (!gotInstanceId) {
            return false;
        }

        // The parent of the nvme disk device is its PCI interface
        bool gotParent = PciWinCfgMgr.GetDeviceNodeOfParent(out IntPtr parentNodePtr, deviceInstanceId);

        if (!gotParent) {
            return false;
        }

        bool gotConfig = PciWinCfgMgr.CreateMockConfigBufferFromPciDevNode(out config, out bool isLittleEndian, parentNodePtr);

        return gotConfig;
    }

    // To get Identify Controller Data use:QueryNvmeCnsThruIntelRstDriver(out StorageWinStructs.IntelNvmeIoctlPassthrough passThrough, deviceNumber, scsiAddr, StorageWinConstants.NvmeCnsValue.IDENTIFY_CONTROLLER, 0);
    // To get Identify Namespace Data use: QueryNvmeCnsThruIntelRstDriver(out StorageWinStructs.IntelNvmeIoctlPassthrough passThrough, deviceNumber, scsiAddr, StorageWinConstants.NvmeCnsValue.IDENTIFY_NAMESPACE_FOR_NSID, 1);
    public static bool QueryNvmeCnsThruIntelRstDriver(out StorageNvmeWinStructs.IntelNvmeIoctlPassthrough passThrough, int deviceNumber, StorageWinStructs.ScsiAddress scsiAddr, StorageNvmeConstants.NvmeCnsValue cnsValue, uint nsid) {
        string deviceString = string.Format(StorageWinConstants.DISK_HANDLE_SCSI, deviceNumber);

        passThrough = StorageCommonHelpers.CreateStruct<StorageNvmeWinStructs.IntelNvmeIoctlPassthrough>();

        if (cnsValue == StorageNvmeConstants.NvmeCnsValue.IDENTIFY_NAMESPACE_FOR_NSID && nsid == 0) {
            return false;
        }

        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(deviceString);

        if (!StorageCommonHelpers.IsDeviceHandleReady(handle)) {
            return false;
        }

        bool endResult = false;
        IntPtr ptr = IntPtr.Zero;

        int allocationSize = Marshal.SizeOf<StorageNvmeWinStructs.IntelNvmeIoctlPassthrough>();
        int srbHeaderSize = Marshal.SizeOf<StorageWinStructs.SrbIoControl>();
        int dataBufferOffset = Marshal.OffsetOf<StorageNvmeWinStructs.IntelNvmeIoctlPassthrough>("data").ToInt32();

        passThrough.Header.HeaderLength = (uint)srbHeaderSize;
        passThrough.Header.Signature = Encoding.ASCII.GetBytes(StorageNvmeWinConstants.INTELNVM_SIGNATURE);
        passThrough.Header.Timeout = 15;
        passThrough.Header.ControlCode = StorageNvmeWinConstants.IOCTL_INTEL_NVME_PASSTHROUGH;
        passThrough.Header.Length = (uint)allocationSize - (uint)srbHeaderSize;
        passThrough.Version = StorageNvmeWinConstants.INTEL_NVME_PASS_THROUGH_VERSION;
        passThrough.TargetId = scsiAddr.TargetId;
        passThrough.Lun = scsiAddr.Lun;
        passThrough.PathId = scsiAddr.PathId;

        passThrough.Parameters.Command.Admin.opcode = (byte)StorageNvmeConstants.NvmeAdminCommandOpcode.IDENTIFY;
        passThrough.Parameters.IsIOCommandSet = 0; // 0 = Admin command set
        passThrough.Parameters.Command.Admin.nsid = nsid;
        passThrough.Parameters.Command.Admin.cdw10 = (uint)cnsValue;

        passThrough.Parameters.DataBufferLength = 4096;
        passThrough.Parameters.DataBufferOffset = (uint)dataBufferOffset;

        try {
            // Allocate memory for the buffer
            ptr = Marshal.AllocHGlobal(allocationSize);

            // Need the starting address of the buffer for the next command to calculate the offset 
            passThrough.Parameters.Command.Admin.addr = (ulong)IntPtr.Add(ptr, (int)passThrough.Parameters.DataBufferOffset).ToInt64();

            // Copy the data from the managed object to the pointer
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
                passThrough = Marshal.PtrToStructure<StorageNvmeWinStructs.IntelNvmeIoctlPassthrough>(ptr);
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(ptr);
        }

        return endResult;
    }
}
