using System.Runtime.InteropServices;
using System.Runtime.Versioning;

namespace StorageAta.Linux;

[SupportedOSPlatform("linux")]
public class StorageAtaLinuxStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct AtaPassThroughCdb16 {
        public byte opcode;
        public byte protocol; // extend = 1, LBA = 48 bits
        public byte flags;
        internal byte features_reserved;
        public byte features;
        internal byte sector_count_reserved;
        public byte sector_count;
        internal byte lba_low_reserved;
        public byte lba_low;
        internal byte lba_mid_reserved;
        public byte lba_mid;
        internal byte lba_high_reserved;
        public byte lba_high;
        public byte device;
        public byte command;
        public byte control;
    }
}