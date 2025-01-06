namespace StorageNvme;
public class StorageNvmeConstants {
    public static readonly uint NVME_IDENTIFY_DATA_BUFFER_SIZE = 4096;

    // NVMe Constants
    // NVM Express® Base Specification, Revision 2.1, 5.1.13.1 Figure 310: Identify – CNS Values
    public enum NvmeCnsValue : uint {
        IDENTIFY_NAMESPACE_FOR_NSID = 0x0, // Identify Namespace data structure for the specified NSID
        IDENTIFY_CONTROLLER = 0x01, // Identify Controller data structure for the controller processing the command
        IDENTIFY_ACTIVE_NAMESPACE_LIST = 0x02, // Active Namespace ID list
        IDENTIFY_NSID_DESCRIPTOR_FOR_NSID = 0x03 // Namespace Identification Descriptor list for the specified NSID.
    }

    // NVM Express® Base Specification, Revision 2.1, 3.1.3.4 Command Support Requirements
    public enum NvmeAdminCommandOpcode : byte {
        GET_LOG_PAGE = 0x02,
        IDENTIFY = 0x06
    }
}
