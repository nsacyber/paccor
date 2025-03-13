namespace StorageLib.Windows;
public class StorageWinDiskDescriptor(int diskNumber, StorageWinConstants.StorageBusType adapterBusType, StorageWinConstants.StorageBusType deviceBusType) {
    public int DiskNumber {
        get;
    } = diskNumber;
    public StorageWinConstants.StorageBusType AdapterBusType {
        get;
    } = adapterBusType;
    public StorageWinConstants.StorageBusType DeviceBusType {
        get;
    } = deviceBusType;
}
