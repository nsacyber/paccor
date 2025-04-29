namespace Smbios {
    public class SmbiosTable {
        public int Type {
            get;
            private set;
        }

        public int Handle {
            get;
            private set;
        }

        public byte[] Data {
            get;
            private set;
        }

        public string[] Strings {
            get;
            private set;
        }

        public bool Valid {
            get;
            private set;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="inData">The structure data. Not including strings.</param>
        /// <param name="inStrings">The strings of the structure.</param>
        public SmbiosTable(byte[] inData, string[] inStrings) {
            Data = inData.Length > 0 ? inData : Array.Empty<byte>();
            Strings = inStrings.Length > 0 ? inStrings : Array.Empty<string>();

            if (inData.Length > 3 && inData[1] == inData.Length) {
                Valid = true;
                Type = inData[0];
                Handle = BitConverter.ToInt16(inData, 2);
            }
        }
    }
}
