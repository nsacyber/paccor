namespace StorageLib.Linux;
public class StorageLinuxDiskDescriptor(string diskPath, StorageLinuxConstants.BlockType type) : StorageDiskDescriptor(diskPath) {
    public StorageLinuxConstants.BlockType BlockType {
        get;
    } = type;
}
