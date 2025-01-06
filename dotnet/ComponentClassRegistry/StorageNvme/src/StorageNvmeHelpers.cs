using System.Runtime.InteropServices;

namespace StorageNvme;
public class StorageNvmeHelpers {
    public static bool CollectNvmeData(out List<StorageNvmeData> list) {
        list = new();
        bool result = false;

        IStorageNvme? nvme = null;

        if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
            nvme = new Linux.StorageNvmeLinux();
        } else if(RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            nvme = new Windows.StorageNvmeWin();
        }

        if (nvme == null) {
            return false;
        }

        result = nvme.CollectNvmeData(out list);

        return result;
    }
}
