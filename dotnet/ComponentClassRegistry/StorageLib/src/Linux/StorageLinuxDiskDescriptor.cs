namespace StorageLib.Linux;
internal class StorageLinuxDiskDescriptor(string diskPath, StorageLinuxConstants.BlockType type) : StorageDiskDescriptor(diskPath) {
    public StorageLinuxConstants.BlockType BlockType {
        get;
    } = type;
}
