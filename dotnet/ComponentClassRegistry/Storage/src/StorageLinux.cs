using Microsoft.Win32.SafeHandles;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using StorageLib;

namespace StorageWin;
public class StorageLinux {
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

    public static bool Work() {
        bool endResult = true;
        Regex regex = new Regex(@"^(/dev/nvme[0-9A-Fa-f]+n[0-9A-Fa-f]+)p.*$");
        string[] matches = Directory.EnumerateFileSystemEntries(@"/dev/").Where(f => regex.IsMatch(f)).Select(s => s.Split('p')[0]).Distinct().ToArray();

        foreach (string devName in matches) {
            endResult = endResult && Go(out StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl, devName);
            endResult = endResult && FindPciHeaderForNvmeDevName(out byte[] config, out byte[] vpd, devName);
            Console.WriteLine();
        }
        return endResult;
    }

    public static bool Go(out StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl, string devName) {
        StorageLinuxStructs.NvmePassthruCmd passthru = StorageCommonHelpers.CreateStruct<StorageLinuxStructs.NvmePassthruCmd>();
        nvmeCtrl = new();
        // open handle on nvme device
        //SafeFileHandle handle = LinuxImports.open(devName, LinuxConstants.O_RDWR | LinuxConstants.O_NONBLOCK);
        SafeFileHandle handle = File.OpenHandle(devName, FileMode.Open, FileAccess.Read, FileShare.Read);

        if (handle.IsInvalid) {
            return false;
        }

        IntPtr ptr = IntPtr.Zero;
        bool endResult = false;
        int allocationSize = Marshal.SizeOf<StorageLinuxStructs.NvmePassthruCmd>();

        // ioctl??
        try {
            passthru.adminCmd.opcode = 0x06;
            passthru.adminCmd.dataLength = 4096;
            passthru.adminCmd.cdw10 = 1;
            passthru.timeout = 15000;
            //int initialIoctl = LinuxImports.ioctl(handle, LinuxConstants.NVME_IOCTL_ID);

            // Allocate memory for the buffer
            ptr = Marshal.AllocHGlobal(allocationSize);

            // Needed the starting address of the buffer for the next command to calculate the offset
            passthru.adminCmd.addr = (ulong)IntPtr.Add(ptr, Marshal.OffsetOf<StorageLinuxStructs.NvmePassthruCmd>("data").ToInt32());

            // Copy the data from the managed object to the buffer
            Marshal.StructureToPtr(passthru, ptr, true);

            int result = StorageLinuxImports.ioctl(handle, StorageLinuxConstants.NVME_IOCTL_ADMIN_CMD, ptr);

            if (result < 0) {
                int systemerror = Marshal.GetLastSystemError();
                endResult = false;
            } else {
                nvmeCtrl = Marshal.PtrToStructure<StorageNvmeStructs.NvmeIdentifyControllerData>(IntPtr.Add(ptr, Marshal.OffsetOf<StorageLinuxStructs.NvmePassthruCmd>("data").ToInt32()));
                endResult = true;
            }
        } finally {
            handle.Dispose();
            Marshal.FreeHGlobal(ptr);
        }

        return endResult;
    }
}
