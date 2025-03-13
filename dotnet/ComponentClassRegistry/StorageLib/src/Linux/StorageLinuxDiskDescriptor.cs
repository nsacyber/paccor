namespace StorageLib.Linux;
public class StorageLinuxDiskDescriptor(string diskPath, StorageLinuxConstants.BlockType type) {
    public string DiskPath {
        get;
    } = diskPath;

    public StorageLinuxConstants.BlockType BlockType {
        get;
    } = type;
}
