using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using System.Text.RegularExpressions;

namespace StorageLib.Linux;

[SupportedOSPlatform("linux")]
public class StorageLinux {
    public static string[] GetPhysicalDevicePaths(string regex, List<Func<string, string>>? selectFilters = null) {
        if (string.IsNullOrWhiteSpace(regex)) {
            return [];
        }
        
        Regex regexObj = new Regex(regex);
        IEnumerable<string> entries = Directory.EnumerateFileSystemEntries(@"/dev/").Where(f => regexObj.IsMatch(f));

        if (selectFilters != null) {
            foreach (Func<string, string> filter in selectFilters) {
                entries = entries.Select(filter);
            }
        }
        
        string[] matches = entries.Distinct().ToArray();
        return matches;
    }
}