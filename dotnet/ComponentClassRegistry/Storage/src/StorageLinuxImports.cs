using Microsoft.Win32.SafeHandles;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace StorageWin;
public class StorageLinuxImports {
    [DllImport("libc", SetLastError = true)]
    internal static extern int ioctl(SafeFileHandle fd, uint op, IntPtr data);
}
