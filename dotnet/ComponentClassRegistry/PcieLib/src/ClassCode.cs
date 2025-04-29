namespace PcieLib;

public class ClassCode {
    public byte[] Data {
        get;
        private set;
    } = Array.Empty<byte>();
    public byte Class {
        get;
        private set;
    }
    public byte SubClass {
        get;
        private set;
    }
    public byte ProgrammingInterface {
        get;
        private set;
    }

    public string Hex {
        get;
        private set;
    } = "";

    public ClassCode() {
    }

    public ClassCode(byte[] inData, bool littleEndian = true) {
        if (inData.Length <= 0 || inData.Length > 3) {
            return;
        }

        Data = inData;
        if (littleEndian) {
            Array.Reverse<byte>(Data);
        }
        Class = Data[0];
        SubClass = Data[1];
        ProgrammingInterface = Data[2];
        Hex = Convert.ToHexString(Data).PadLeft(6, '0');
    }
}
