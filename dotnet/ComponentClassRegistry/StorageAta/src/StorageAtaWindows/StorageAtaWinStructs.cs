using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageAta.Windows;

[SupportedOSPlatform("windows")]
public class StorageAtaWinStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct AtaPassThroughDirect {
        [MarshalAs(UnmanagedType.U2)]
        public ushort Length;
        [MarshalAs(UnmanagedType.U2)]
        public ushort AtaFlags;
        public byte PathId;
        public byte TargetId;
        public byte Lun;
        public byte ReservedAsUchar;
        [MarshalAs(UnmanagedType.U4)]
        public uint DataTransferLength;
        [MarshalAs(UnmanagedType.U4)]
        public uint TimeOutValue;
        [MarshalAs(UnmanagedType.U4)]
        public uint ReservedAsUlong;
        public IntPtr DataBuffer;
        public AtaTaskFile PreviousTaskFile;
        public AtaTaskFile CurrentTaskFile;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct AtaTaskFile {
        public byte Features;
        public byte SectorCount;
        public byte SectorNumber;
        public byte CylinderLow;
        public byte CylinderHigh;
        public byte DeviceHead;
        public byte Command;
        public byte Reserved;
    }
}

