using PcieLib;

namespace StorageNvme;
public class StorageNvmeData(StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl, ClassCode classCode) {
    public StorageNvmeStructs.NvmeIdentifyControllerData NvmeCtrl {
        get;
    } = nvmeCtrl;

    public ClassCode ClassCode {
        get;
    } = classCode;
}