using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageLinuxStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct SgIoHdr {
        [MarshalAs(UnmanagedType.I4)] 
        public StorageLinuxConstants.InterfaceId interface_id;
        
        [MarshalAs(UnmanagedType.I4)] 
        public StorageLinuxConstants.SgDxfer dxfer_direction;

        public byte cmd_len;
        public byte mx_sb_len;
        
        [MarshalAs(UnmanagedType.U2)] 
        public ushort iovec_count;
        
        [MarshalAs(UnmanagedType.U4)] 
        public uint dxfer_len;

        public IntPtr dxferp;
        public IntPtr cmdp;
        public IntPtr sbp;
        
        [MarshalAs(UnmanagedType.U4)] 
        public uint timeout;
        
        [MarshalAs(UnmanagedType.U4)] 
        public StorageLinuxConstants.SgFlag flags;
        
        [MarshalAs(UnmanagedType.I4)] 
        public int pack_id;

        public IntPtr usr_ptr;

        public byte status;
        public byte masked_status;
        public byte msg_status;
        public byte sb_len_wr;
        
        [MarshalAs(UnmanagedType.U2)] 
        public ushort host_status;

        [MarshalAs(UnmanagedType.U2)]
        public ushort driver_status;
        
        [MarshalAs(UnmanagedType.I4)] 
        public int resid;

        [MarshalAs(UnmanagedType.U4)]
        public uint duration;

        [MarshalAs(UnmanagedType.U4)]
        public StorageLinuxConstants.SgInfo info;
    }
}
