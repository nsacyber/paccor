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
        public SmbiosTable(byte[] inData, string[] inStrings) : this(inData, inStrings, true) {
        }

        public SmbiosTable(byte[] inData, string[] inStrings, bool stringsTerminated) {
            Data = inData.Length > 0 ? inData : [];
            Strings = inStrings.Length > 0 ? inStrings : [];

            if (inData.Length > 3) {
                Type = inData[0];
                Handle = BitConverter.ToInt16(inData, 2);
                Valid = stringsTerminated && inData[1] == inData.Length;
            }
        }
    }
}
