namespace PcieLib;

public class PcieId {
    public byte[] Data {
        get;
        private set;
    } = Array.Empty<byte>();

    public ushort Id {
        get;
        private set;
    }

    public string Hex {
        get;
        private set;
    } = "";

    public PcieId() {
    }

    public PcieId(byte[] inData, bool littleEndian = true) {
        if (inData.Length <= 0 || inData.All(singleByte => singleByte == 0)) {
            return;
        }

        Data = inData;
        if (littleEndian) {
            Array.Reverse<byte>(Data);
        }
        Id = BitConverter.ToUInt16(Data);
        Hex = Convert.ToHexString(Data).PadLeft(4, '0');
    }
}