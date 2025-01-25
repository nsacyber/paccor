using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StorageAta;
public class StorageAtaConstants {
    public static readonly uint ATA_LOG_SIZE_BYTES = 512;

    public enum AtaCommand : byte {
        ReadLogExt = 0x2F,
        ReadLogDmaExt = 0x47,
        IdentifyDevice = 0xEC
    }

    public enum AtaLogAddress : byte {
        IdentifyDeviceDataLog = 0x30
    }

    public enum AtaIdentifyDeviceLogPage : byte {
        ListSupportedPages = 0x00,
        IdentifyDeviceData = 0x01,
        Capacity = 0x02,
        SupportedCapabilities = 0x03,
        CurrentSettings = 0x04,
        AtaStrings = 0x05,
        Security = 0x06,
        ParallelAta = 0x07,
        SerialAta = 0x08
    }
}
