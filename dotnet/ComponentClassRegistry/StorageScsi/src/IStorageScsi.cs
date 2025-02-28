namespace StorageScsi;
public interface IStorageScsi {
    bool CollectScsiData(out List<StorageScsiData> list);
}