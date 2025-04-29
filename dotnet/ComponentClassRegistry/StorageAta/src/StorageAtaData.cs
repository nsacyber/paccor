using StorageLib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace StorageAta;
public class StorageAtaData(StorageAtaStructs.AtaIdentifyData identData, StorageAtaStructs.AtaCapabilitiesData capData, StorageAtaStructs.AtaStringsData stringsData) {
    public StorageAtaStructs.AtaIdentifyData Identify {
        get;
    } = identData;
    public StorageAtaStructs.AtaCapabilitiesData Capabilities {
        get;
    } = capData;
    public StorageAtaStructs.AtaStringsData Strings {
        get;
    } = stringsData;

    public static bool Build(out StorageAtaData obj, byte[] page1, byte[] page3, byte[] page5) {
        bool invalidData = false;
        if (page1.Length != StorageAtaConstants.ATA_LOG_SIZE_BYTES) {
            page1 = new byte[StorageAtaConstants.ATA_LOG_SIZE_BYTES];
            invalidData = true;
        }
        if (page3.Length != StorageAtaConstants.ATA_LOG_SIZE_BYTES) {
            page3 = new byte[StorageAtaConstants.ATA_LOG_SIZE_BYTES];
            invalidData = true;
        }
        if (page5.Length != StorageAtaConstants.ATA_LOG_SIZE_BYTES) {
            page5 = new byte[StorageAtaConstants.ATA_LOG_SIZE_BYTES];
            invalidData = true;
        }

        StorageAtaStructs.AtaIdentifyData identData = StorageCommonHelpers.CreateStruct<StorageAtaStructs.AtaIdentifyData>(page1);
        StorageAtaStructs.AtaCapabilitiesData capData = StorageCommonHelpers.CreateStruct<StorageAtaStructs.AtaCapabilitiesData>(page3);
        StorageAtaStructs.AtaStringsData stringsData = StorageCommonHelpers.CreateStruct<StorageAtaStructs.AtaStringsData>(page5);

        obj = new(identData, capData, stringsData);
        return !invalidData;
    }
}
