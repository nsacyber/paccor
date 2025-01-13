using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.XPath;

namespace PcieWinCfgMgr;

public class MainCanDelete {
    public static void Main(string[] args) {
        bool result = PciWinCfgMgr.GetDiskDevInterfaces(out List<string> diskDeviceInterfaceIdsW);

        bool result2 = PciWinCfgMgr.GetDeviceInterfaceInstanceIds(out List<string> deviceInterfaceInstanceIds, diskDeviceInterfaceIdsW);

        bool result3 = PciWinCfgMgr.GetDeviceNodeOfParent(out IntPtr parentNodePtr, deviceInterfaceInstanceIds[0]);

        bool result4 = PciWinCfgMgr.GetPciClassCodeFromDevNode(out byte Class, out byte SubClass, out byte ProgrammingInterface, parentNodePtr);

        string cc = CfgHelpers.ClassCodesToHexString(Class, SubClass, ProgrammingInterface);

        bool result5 = PciWinCfgMgr.ParseHardwareIdsFromDevNode(out ushort VendorId, out ushort DeviceId, out ushort SubsystemVendorId, out ushort SubsystemId, out byte Revision, out Class, out SubClass, out ProgrammingInterface, parentNodePtr);

        bool result6 = PciWinCfgMgr.GetAllPciDeviceInstanceIds(out List<string> pciDeviceInstanceIds);

        foreach(string pciDeviceInstanceId in pciDeviceInstanceIds) {
            bool result7 = PciWinCfgMgr.CreateMockConfigBufferFromPciDeviceInstanceId(out byte[] config, out bool isLittleEndian, pciDeviceInstanceId);

            if (!result7) {
                Console.WriteLine("PCI Device Instance ID: " + pciDeviceInstanceId);
            }
        }

        Console.WriteLine();

    }
}
