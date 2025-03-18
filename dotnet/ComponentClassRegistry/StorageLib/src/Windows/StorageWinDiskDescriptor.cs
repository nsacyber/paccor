namespace StorageLib.Windows;
public class StorageWinDiskDescriptor(int diskNumber, StorageWinConstants.StorageBusType adapterBusType, StorageWinConstants.StorageBusType deviceBusType) : StorageDiskDescriptor("" + diskNumber) {
    public int DiskNumber {
        get;
    } = diskNumber;
    public StorageWinConstants.StorageBusType AdapterBusType {
        get;
    } = adapterBusType;
    public StorageWinConstants.StorageBusType DeviceBusType {
        get;
    } = deviceBusType;

    public StorageWinDiskDescriptor(int diskNumber, byte adapterBusType, StorageWinConstants.StorageBusType deviceBusType) : this(diskNumber, (StorageWinConstants.StorageBusType)adapterBusType, deviceBusType) {
    }

    public StorageWinDiskDescriptor(int diskNumber, StorageWinConstants.StorageBusType adapterBusType, byte deviceBusType) : this(diskNumber, adapterBusType, (StorageWinConstants.StorageBusType)deviceBusType) {
    }

    public StorageWinDiskDescriptor(int diskNumber, byte adapterBusType, byte deviceBusType) : this(diskNumber, (StorageWinConstants.StorageBusType)adapterBusType, (StorageWinConstants.StorageBusType)deviceBusType) {
    }

    public StorageWinDiskDescriptor(int diskNumber, uint adapterBusType, StorageWinConstants.StorageBusType deviceBusType) : this(diskNumber, (StorageWinConstants.StorageBusType)adapterBusType, deviceBusType) {
    }

    public StorageWinDiskDescriptor(int diskNumber, StorageWinConstants.StorageBusType adapterBusType, uint deviceBusType) : this(diskNumber, adapterBusType, (StorageWinConstants.StorageBusType)deviceBusType) {
    }

    public StorageWinDiskDescriptor(int diskNumber, uint adapterBusType, uint deviceBusType) : this(diskNumber, (StorageWinConstants.StorageBusType)adapterBusType, (StorageWinConstants.StorageBusType)deviceBusType) {
    }
}
