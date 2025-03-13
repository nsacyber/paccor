using StorageLib.Linux;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StorageLib;
public class StorageDiskDescriptor(string diskId) {
    public string DiskId {
        get;
    } = diskId;
}
