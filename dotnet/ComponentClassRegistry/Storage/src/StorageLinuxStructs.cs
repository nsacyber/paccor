using StorageLib;
using System.Runtime.InteropServices;

namespace StorageWin;
public class StorageLinuxStructs {
    [StructLayout(LayoutKind.Sequential)]
    internal struct NvmePassthruCmd {
        public StorageNvmeStructs.NvmeAdminCommand adminCmd;
        [MarshalAs(UnmanagedType.U4)] public uint timeout;
        [MarshalAs(UnmanagedType.U4)] public uint result;
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4096)]
        public byte[] data;
    }
}
