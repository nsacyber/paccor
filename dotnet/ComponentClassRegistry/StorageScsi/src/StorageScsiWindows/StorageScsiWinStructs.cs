using StorageLib.Windows;
using System.Runtime.InteropServices.JavaScript;
using System;
using System.Runtime.Versioning;
using System.Runtime.InteropServices;
using static StorageScsi.Windows.StorageScsiWinConstants;

namespace StorageScsi.Windows;

[SupportedOSPlatform("windows")]
public class StorageScsiWinStructs {
    // ntddscsi.h
    [StructLayout(LayoutKind.Sequential)]
    public struct ScsiPassThroughDirect {
        /**
         * Contains the value of sizeof(SCSI_PASS_THROUGH_DIRECT)
         */
        [MarshalAs(UnmanagedType.U2)]
        public ushort Length;
        public byte ScsiStatus;
        public byte PathId;
        public byte TargetId;
        public byte Lun;
        public byte CdbLength;
        public byte SenseInfoLength;
        public byte DataIn;

        [MarshalAs(UnmanagedType.U4)]
        public uint DataTransferLength;

        [MarshalAs(UnmanagedType.U4)]
        public uint TimeOutValue;

        public IntPtr DataBuffer;

        [MarshalAs(UnmanagedType.U4)]
        public uint SenseInfoOffset;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] Cdb;
    }
}