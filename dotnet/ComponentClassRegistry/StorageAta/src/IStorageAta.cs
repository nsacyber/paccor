using StorageLib;
using System.Collections.Immutable;

namespace StorageAta;
public interface IStorageAta {
    bool CollectAtaData(out List<StorageAtaData> list, ImmutableList<StorageDiskDescriptor> disks);
}
