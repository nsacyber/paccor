using PcieLib;

namespace StorageNvme;
internal class StorageNvmeData(StorageNvmeStructs.NvmeIdentifyControllerData nvmeCtrl, ClassCode classCode) {
    public StorageNvmeStructs.NvmeIdentifyControllerData NvmeCtrl {
        get;
    } = nvmeCtrl;

    public ClassCode ClassCode {
        get;
    } = classCode;
}