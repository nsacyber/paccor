using System.Runtime.InteropServices;

namespace PcieWinCfgMgr;

// Of course Windows has to make reading static PCIe device information more complicated than necessary.
public class CfgStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct SpDevinfoData { // SP_DEVINFO_DATA: setupapi.h
        [MarshalAs(UnmanagedType.U4)] public uint cbSize;
        public Guid ClassGuid;
        [MarshalAs(UnmanagedType.U4)] public uint DevInst;
        [MarshalAs(UnmanagedType.U8)] public ulong Reserved;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct SpDeviceInterfaceData { // SP_DEVICE_INTERFACE_DATA: setupapi.h
        [MarshalAs(UnmanagedType.U4)] public uint cbSize;
        public Guid InterfaceClassGuid;
        [MarshalAs(UnmanagedType.U4)] public uint Flags;
        [MarshalAs(UnmanagedType.U8)] public ulong Reserved;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct DevPropKey { // DEVPROPKEY: devpropdef.h
        public Guid DEVPROPGUID;
        [MarshalAs(UnmanagedType.U4)] public uint DEVPROPID;
    }
}
