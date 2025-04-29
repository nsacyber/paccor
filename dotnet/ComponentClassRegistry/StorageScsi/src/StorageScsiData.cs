using StorageLib;

namespace StorageScsi;

public class StorageScsiData(StorageScsiStructs.ScsiInquiryDataNoVendorSpecific inquiry, byte[] vpd80, byte[] vpd83) {
    public StorageScsiStructs.ScsiInquiryDataNoVendorSpecific Inquiry {
        get;
    } = inquiry;
    public byte[] Vpd80 {
        get;
    } = vpd80;
    public byte[] Vpd83 {
        get;
    } = vpd83;

    public static bool Build(out StorageScsiData obj, byte[] inquiry, byte[] vpd80, byte[] vpd83) {
        bool invalidData = false;

        if (inquiry.Length < StorageScsiConstants.SCSI_INQUIRY_DATA_BUFFER_SIZE) {
            inquiry = new byte[StorageScsiConstants.SCSI_INQUIRY_DATA_BUFFER_SIZE];
            invalidData = true;
        }
        
        StorageScsiStructs.ScsiInquiryDataNoVendorSpecific inquiryData = StorageCommonHelpers.CreateStruct<StorageScsiStructs.ScsiInquiryDataNoVendorSpecific>(inquiry);

        obj = new(inquiryData, vpd80, vpd83);
        return !invalidData;
    }
}