using System.Management;
using System.Runtime.InteropServices;
using System.Text;

namespace Smbios {
    public class Smbios {
        public static readonly string WindowsQueryScope = @"root\\WMI";
        public static readonly string WindowsQueryString = "SELECT * FROM MSSMBios_RawSMBiosTables";
        public static readonly string LinuxPathEntryTable = "/sys/firmware/dmi/tables/smbios_entry_point";
        public static readonly string LinuxPathStructures = "/sys/firmware/dmi/tables/DMI";

        /// <summary>
        /// The SMBIOS Major Version from the Entry Point.
        /// </summary>
        public int MajorVersion {
            get;
            private set;
        }

        /// <summary>
        /// The SMBIOS Minor Version from the Entry Point.
        /// </summary>
        public int MinorVersion {
            get;
            private set;
        }

        /// <summary>
        /// Smbios data organized by structure type.
        /// </summary>
        public IDictionary<int, IList<SmbiosTable>> Structures {
            get;
            private init;
        } = new Dictionary<int, IList<SmbiosTable>> ();

        /// <summary>
        /// True if all structures were built with expected data lengths. False if any structure was not valid.
        /// </summary>
        public bool Valid {
            get;
            private set;
        }

        /// <summary>
        /// Calls internal methods to gather raw SMBIOS data from the OS. Then parses that data into a dictionary of SmbiosTable objects.
        /// </summary>
        /// <returns>Smbios data organized by structure type.</returns>
        public static Smbios GetSmbios() {
            int majorVersion = 0;
            int minorVersion = 0;
            byte[] data = Array.Empty<byte>();

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
                GetSmbiosWindows(out majorVersion, out minorVersion, out data);
            } else if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
                GetSmbiosLinux(out majorVersion, out minorVersion, out data);
            }

            // Parse full smbios table into objects
            Smbios smbios = new() {
                MajorVersion = majorVersion,
                MinorVersion = minorVersion,
                Structures = ParseSmbiosData(data)
            };

            // Verify all tables have expected ranges
            smbios.Valid = VerifyStructures(smbios.Structures);

            return smbios;
        }

        /// <summary>
        /// Each structure is built with a simple check to make sure the data length looks correct.
        /// </summary>
        /// <param name="structures">Parsed smbios data.</param>
        /// <returns>This function returns true if all structures in the dictionary are valid. It will return false if any one of them is not valid.</returns>
        private static bool VerifyStructures(IDictionary<int, IList<SmbiosTable>> structures) {
            bool check = true;
            foreach (IList<SmbiosTable> list in structures.Values) {
                foreach (SmbiosTable table in list) {
                    check = check && table.Valid;
                    if (!check) {
                        break;
                    }
                }
                if (!check) {
                    break;
                }
            }

            return check;
        }

        /// <summary>
        /// Turns raw SMBIOS data into SmbiosTable objects. Organizes them by structure type.
        /// </summary>
        /// <param name="smbiosData">Byte array of SMBIOS table data.</param>
        /// <returns>SmbiosTable objects organized by structure type.</returns>
        public static Dictionary<int, IList<SmbiosTable>> ParseSmbiosData(byte[] smbiosData) {
            Dictionary<int, IList<SmbiosTable>> structs = new();

            if (smbiosData.Length == 0) {
                return structs;
            }

            int pos = 0;
            List<string> strings = new();

            // Change pos if entry point information is included.
            if (smbiosData.Length > 4 && Encoding.ASCII.GetString(smbiosData[0..4]).Equals("_SM_")) {
                pos = 0x1F;
            }
            if (smbiosData.Length > 5 && Encoding.ASCII.GetString(smbiosData[0..5]).Equals("_SM3_")) {
                pos = 0x18;
            }
            
            while (pos < smbiosData.Length) {
                int structureStart = pos;
                int structureLength = smbiosData[structureStart + 1];
                int structureEnd = structureStart + structureLength;
                pos = structureEnd;

                // Parse through strings section
                while (smbiosData[pos] != 0) {
                    string newString = "";

                    while (smbiosData[pos] != 0) {
                        newString += (char)smbiosData[pos++];
                    }

                    strings.Add(newString);
                    pos++;
                }

                // Save table to dictionary
                SmbiosTable table = new(smbiosData[structureStart..structureEnd], strings.ToArray());
                if (!structs.ContainsKey(table.Type)) {
                    structs.Add(table.Type, new List<SmbiosTable>());
                }
                structs[table.Type].Add(table);

                // new structure
                strings = new List<string>();
                pos++;

                if (smbiosData[pos] == 0) {
                    pos++;
                }
            }

            return structs;
        }

        /// <summary>
        /// Performs the Windows-specific steps of gather the SMBIOS table data from the OS. Updates the parameter values from entry point data.
        /// </summary>
        /// <param name="majorVersion">Collect the SMBIOS Major Version from the Entry Point.</param>
        /// <param name="minorVersion">Collect the SMBIOS Minor Version from the Entry Point.</param>
        /// <param name="data">Collect the SMBIOS table data.</param>
        private static void GetSmbiosWindows(out int majorVersion, out int minorVersion, out byte[] data) {
            majorVersion = 0;
            minorVersion = 0;
            data = Array.Empty<byte>();

            if (!RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
                return;
            }

            ManagementObjectCollection collection;
            using (ManagementObjectSearcher searcher = new ManagementObjectSearcher("root\\WMI", "SELECT * FROM MSSMBios_RawSMBiosTables")) {
                //using (ManagementObjectSearcher searcher = new(WindowsQueryScope, WindowsQueryString)) {
                collection = searcher.Get();
            }

            foreach (ManagementBaseObject? o in collection) {
                if (o == null) {
                    continue;
                }

                ManagementObject mo = (ManagementObject)o;
                object dataObj = mo["SMBiosData"];
                object majorVersionObj = mo["SmbiosMajorVersion"];
                object minorVersionObj = mo["SmbiosMinorVersion"];
                if (dataObj == null || majorVersionObj == null || minorVersionObj == null) {
                    continue;
                }

                majorVersion = (byte)majorVersionObj;
                minorVersion = (byte)minorVersionObj;
                data = (byte[])dataObj;
                break;
            }
        }

        /// <summary>
        /// Performs the Linux-specific steps of gather the SMBIOS table data from the OS. Updates the parameter values from entry point data.
        /// </summary>
        /// <param name="majorVersion">Collect the SMBIOS Major Version from the Entry Point.</param>
        /// <param name="minorVersion">Collect the SMBIOS Minor Version from the Entry Point.</param>
        /// <param name="data">Collect the SMBIOS table data.</param>
        private static void GetSmbiosLinux(out int majorVersion, out int minorVersion, out byte[] data) {
            majorVersion = 0;
            minorVersion = 0;
            data = Array.Empty<byte>();

            if (!RuntimeInformation.IsOSPlatform(OSPlatform.Linux)) {
                return;
            }

            // Is this program running with elevated privileges?
            if (LinuxImports.geteuid() != 0) {
                return;
            }

            byte[] entryTable = File.ReadAllBytes(LinuxPathEntryTable);
            majorVersion = entryTable[0x07];
            minorVersion = entryTable[0x08];
            data = File.ReadAllBytes(LinuxPathStructures);
        }
    }
}
