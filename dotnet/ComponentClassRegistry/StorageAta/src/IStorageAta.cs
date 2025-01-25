namespace StorageAta;
public interface IStorageAta {
    bool CollectAtaData(out List<StorageAtaData> list);
}
