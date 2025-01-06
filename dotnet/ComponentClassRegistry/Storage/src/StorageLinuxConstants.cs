using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StorageWin;
public class StorageLinuxConstants {
    // fcntl.h
    internal static readonly uint O_ACCMODE = 0x3;
    internal static readonly uint O_RDONLY = 0x0;
    internal static readonly uint O_WRONLY = 0x1;
    internal static readonly uint O_RDWR = 0x2;
    internal static readonly uint O_APPEND = 0x2000;
    internal static readonly uint O_NONBLOCK = 0x0800;

    // nvme_ioctl.h
    internal static readonly uint NVME_IOCTL_ID = 0xC0484E40; //0x4E40;
    internal static readonly uint NVME_IOCTL_ADMIN_CMD = 0xC0484E41; //0x4E41;
}
