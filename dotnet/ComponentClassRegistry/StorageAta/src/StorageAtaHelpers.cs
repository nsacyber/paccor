using StorageLib;
using System.Collections.Immutable;
using System.Runtime.InteropServices;

namespace StorageAta;
public class StorageAtaHelpers {
    public static bool CollectAtaData(out List<StorageAtaData> list, ImmutableList<StorageDiskDescriptor> disks) {
        list = new();
        bool result = false;

        IStorageAta? ata = null;

        if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
            ata = new Linux.StorageAtaLinux();
        } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            ata = new Windows.StorageAtaWin();
        }

        if (ata == null) {
            return false;
        }

        result = ata.CollectAtaData(out list, disks);

        return result;
    }
}
