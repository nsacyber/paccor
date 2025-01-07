using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageNvme.Linux;

[SupportedOSPlatform("linux")]
public class StorageNvmeLinuxStructs {

    [StructLayout(LayoutKind.Sequential)]
    public struct NvmePassthruCmd {
        public StorageNvmeStructs.NvmeAdminCommand adminCmd;
        [MarshalAs(UnmanagedType.U4)] public uint timeout;
        [MarshalAs(UnmanagedType.U4)] public uint result;
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4096)]
        public byte[] data;
    }
}
