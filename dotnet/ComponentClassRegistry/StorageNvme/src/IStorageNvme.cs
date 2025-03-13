using StorageLib;
using System.Collections.Immutable;

namespace StorageNvme;
public interface IStorageNvme {
    bool CollectNvmeData(out List<StorageNvmeData> list, ImmutableList<StorageDiskDescriptor> disks);
}
