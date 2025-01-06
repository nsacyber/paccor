namespace StorageNvme;
public interface IStorageNvme {
    bool CollectNvmeData(out List<StorageNvmeData> list);
}
