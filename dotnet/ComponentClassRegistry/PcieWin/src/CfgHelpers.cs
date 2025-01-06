using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PcieWinCfgMgr;
public class CfgHelpers {
    public static string ClassCodesToHexString(uint baseClassInt = 0, uint subClassInt = 0, uint progIfInt = 0) {
        string cc = baseClassInt.ToString("X2") + subClassInt.ToString("X2") + progIfInt.ToString("X2");

        return cc;
    }
}
