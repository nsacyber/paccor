using System.Runtime.InteropServices;

namespace StorageScsi;

public class StorageScsiStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct ScsiInquiryDataNoVendorSpecific {
        public byte PeripheralQualifierAndDeviceType;
        internal byte Byte1;
        internal byte Version;
        internal byte Byte3;
        internal byte AdditionalLength;
        internal byte Byte5;
        internal byte Byte6;
        internal byte Byte7;
           
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] T10VendorIdentification; // byte 8:15 T10 VENDOR IDENTIFICATION, left-aligned ASCII string
           
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] ProductIdentification; // byte 16:31 PRODUCT IDENTIFICATION, left-aligned ASCII string
           
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        public byte[] ProductRevisionLevel; // byte 32:35 PRODUCT REVISION LEVEL, left-aligned ASCII string
    }


    [StructLayout(LayoutKind.Sequential)]
    public struct ScsiCdb {
        public byte opcode; // StorageScsiConstants.ScsiOpCode
        public byte Byte1; // StorageScsiConstants.ScsiCdbByte1Flags (really only to set EVPD)
        public byte PageCode; // StorageScsiConstants.ScsiPageCode OR 0x01:0x7F for specific ASCII Information pages
        [MarshalAs(UnmanagedType.U2)] public ushort AllocationLength;
        public byte Control;
    }
}