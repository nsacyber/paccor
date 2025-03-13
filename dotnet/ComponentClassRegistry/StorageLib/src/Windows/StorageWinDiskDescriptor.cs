namespace StorageLib.Windows;
public class StorageWinDiskDescriptor(int diskNumber, StorageWinConstants.StorageBusType adapterBusType, StorageWinConstants.StorageBusType deviceBusType) : StorageDiskDescriptor("" + diskNumber) {
    public int DiskNumber {
        get; 
    }
    public StorageWinConstants.StorageBusType AdapterBusType {
        get;
    } = adapterBusType;
    public StorageWinConstants.StorageBusType DeviceBusType {
        get;
    } = deviceBusType;
}
