using StorageLib;
using System.Runtime.Versioning;

namespace StorageAta.Linux;

[SupportedOSPlatform("linux")]
public class StorageAtaLinuxConstants {
    public enum AtaPassThroughOpCodes : byte {
        AtaPassThrough16 = 0x85
    }
    
    public enum AtaPassThroughProtocol : byte {
        AtaPio = 0x08,
        AtaPioExtend = 0x09,
        AtaDma = 0x0C,
        AtaDmaExtend = 0x0D,
    }
    
    public enum AtaPassThroughFlags : byte {
        AtaReadOneBlock = 0x0D,
        AtaReadTwoBlocks = 0x0E,
        AtaReadOneBlockWithCk = 0x2D,
        AtaReadTwoBlocksWithCk = 0x2E
    }
}