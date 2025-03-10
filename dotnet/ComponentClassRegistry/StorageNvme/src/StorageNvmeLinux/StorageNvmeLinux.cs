using Microsoft.Win32.SafeHandles;
using PcieLib;
using StorageLib;
using StorageLib.Linux;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using System.Text.RegularExpressions;

namespace StorageNvme.Linux;

[SupportedOSPlatform("linux")]
public class StorageNvmeLinux : IStorageNvme {
    public bool CollectNvmeData(out List<StorageNvmeData> list) {
        list = [];
        bool noProblems = true;
        string[] matches = StorageLinux.GetPhysicalDevicePaths(StorageLinuxConstants.BlockType.NVME);

        foreach (string devName in matches) {
            bool nvmeCtrlRead = QueryNvmeCns(out StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl, devName);
            bool pciRead = FindPciHeaderForNvmeDevName(out byte[] config, out byte[] vpd, devName);
            if (nvmeCtrlRead && pciRead) {
                PcieDevice pciDev = new(config, vpd);
                StorageNvmeData nvmeData = new(nvmeCtrl, pciDev.ClassCode);
                list.Add(nvmeData);
            }
            noProblems = noProblems && nvmeCtrlRead && pciRead;
        }
        return noProblems;
    }

    public static bool FindPciHeaderForNvmeDevName(out byte[] config, out byte[] vpd, string devName) {
        bool result = false;
        config = Array.Empty<byte>();
        vpd = Array.Empty<byte>();

        // devName ex /dev/nvme0n1
        Regex findRegex = new Regex(@"^/dev/(nvme)([0-9A-Fa-f]+)n([0-9A-Fa-f]+)");
        Match match = findRegex.Match(devName);
        string findPath = "/" + match.Groups[1].Value + "/" + match.Groups[1].Value + match.Groups[2].Value;
        string findName = match.Groups[1].Value + match.Groups[2].Value + "n" + match.Groups[3].Value;
        //Directory.
        // find /sys/bus/pci/devices/*/nvme/nvme0 -type d -name nvme0n1
        // /sys/bus/pci/devices/0000:3b:00.0/nvme/nvme0/nvme0n1
        Regex sysRegex = new Regex(@"^/sys/bus/pci/devices/[0-9A-Fa-f:.]+$");
        string[] pciDevices = Directory.GetDirectories("/sys/bus/pci/devices");
        foreach (string pciDevice in pciDevices) {
            if (Directory.Exists(pciDevice + findPath + "/" + findName)) {
                string pciDeviceConfigFile = pciDevice + "/config";
                string pciDeviceVpdFile = pciDevice + "/vpd";
                if (File.Exists(pciDeviceVpdFile)) {
                    vpd = File.ReadAllBytes(pciDeviceVpdFile);
                }
                if (File.Exists(pciDeviceConfigFile)) {
                    config = File.ReadAllBytes(pciDeviceConfigFile);
                    result = true;
                }
                break;
            }
        }

        return result;
    }

    

    public static bool QueryNvmeCns(out StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl, string devName) {
        StorageNvmeLinuxStructs.NvmePassthruCmd passthru = StorageCommonHelpers.CreateStruct<StorageNvmeLinuxStructs.NvmePassthruCmd>();
        nvmeCtrl = new();
        // open handle on nvme device
        using SafeFileHandle handle = StorageCommonHelpers.OpenDevice(devName);

        IntPtr ptr = IntPtr.Zero;
        bool endResult = false;
        int allocationSize = Marshal.SizeOf<StorageNvmeLinuxStructs.NvmePassthruCmd>();

        passthru.adminCmd.opcode = (byte)StorageNvmeConstants.NvmeAdminCommandOpcode.IDENTIFY;
        passthru.adminCmd.dataLength = 4096;
        passthru.adminCmd.cdw10 = (uint)StorageNvmeConstants.NvmeCnsValue.IDENTIFY_CONTROLLER;
        passthru.timeout = 15000;

        try {
            // Allocate memory for the buffer
            ptr = Marshal.AllocHGlobal(allocationSize);

            // Needed the starting address of the buffer for the next command to calculate the offset
            passthru.adminCmd.addr = (ulong)IntPtr.Add(ptr, Marshal.OffsetOf<StorageNvmeLinuxStructs.NvmePassthruCmd>("data").ToInt32());

            // Copy the data from the managed object to the buffer
            Marshal.StructureToPtr(passthru, ptr, true);

            int result = StorageLinuxImports.ioctl(handle, StorageNvmeLinuxConstants.NVME_IOCTL_ADMIN_CMD, ptr);

            if (result < 0) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                nvmeCtrl = Marshal.PtrToStructure<StorageNvmeStructs.NvmeIdentifyControllerData>(IntPtr.Add(ptr, Marshal.OffsetOf<StorageNvmeLinuxStructs.NvmePassthruCmd>("data").ToInt32()));
                endResult = true;
            }
        } finally {
            Marshal.FreeHGlobal(ptr);
        }

        return endResult;
    }
}
