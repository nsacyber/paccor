using StorageLib;
using System.Collections.Immutable;

namespace StorageScsi;
internal interface IStorageScsi {
    bool CollectScsiData(out List<StorageScsiData> list, ImmutableList<StorageDiskDescriptor> disks);
}