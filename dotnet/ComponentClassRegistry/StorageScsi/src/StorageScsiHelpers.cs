using System.Runtime.InteropServices;

namespace StorageScsi;
public class StorageScsiHelpers {
    public static bool CollectScsiData(out List<StorageScsiData> list) {
        list = new();
        bool result = false;

        IStorageScsi? scsi = null;

        if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
            scsi = new Linux.StorageScsiLinux();
        } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            scsi = new Windows.StorageScsiWin();
        }

        if (scsi == null) {
            return false;
        }

        result = scsi.CollectScsiData(out list);

        return result;
    }
}