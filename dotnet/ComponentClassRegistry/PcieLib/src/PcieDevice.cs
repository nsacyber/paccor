using System;
using System.Buffers.Binary;
using System.Text;

namespace PcieLib;

public class PcieDevice {
    public byte[] Config {
        get;
        private set;
    }
    public byte[] Vpd {
        get;
        private set;
    }

    public int ConfigType {
        get;
        private set;
    }
    public ClassCode ClassCode {
        get;
        private set;
    }
    public PcieId VendorId {
        get;
        private set;
    }
    public PcieId DeviceId {
        get;
        private set;
    }
    public PcieId SubsystemVendorId {
        get;
        private set;
    }
    public PcieId SubsystemId {
        get;
        private set;
    }
    public byte RevisionId {
        get;
        private set;
    }
    public byte[] DeviceSerialNumber {
        get;
        private set;
    }

    public string VpdMn {
        get;
        private set;
    } = "";
    public string VpdPn {
        get;
        private set;
    } = "";
    public string VpdSn {
        get;
        private set;
    } = "";

    public byte[] NetworkMac { // Need a driver to fetch the address out of BAR. 
        get;
        set; // Can be set after object is created.
    } = Array.Empty<byte>();

    public PcieDevice(byte[] inConfig, byte[] inVpd) : this(inConfig, inVpd, true) {
    }

    public PcieDevice(byte[] inConfig, byte[] inVpd, bool littleEndian) {
        Config = inConfig.Length > 0 ? inConfig :[];

        ConfigType = (Config.Length > 0xD) ? Config[0xE] & 0x7 : 0;

        VendorId = (Config.Length > 1) ? new PcieId(inConfig[..0x2], littleEndian) : new PcieId();
        DeviceId = (Config.Length > 3) ? new PcieId(inConfig[0x2..0x4], littleEndian) : new PcieId();
        RevisionId = (Config.Length > 7) ? inConfig[0x8] : (byte)0;
        ClassCode = (Config.Length > 10) ? new ClassCode(inConfig[0x9..0xC], littleEndian) : new ClassCode();
        SubsystemVendorId = (ConfigType == 0 && Config.Length > 0x2C) ? new PcieId(inConfig[0x2C..0x2E], littleEndian) : new PcieId();
        SubsystemId = (ConfigType == 0 && Config.Length > 0x2E) ? new PcieId(inConfig[0x2E..0x30], littleEndian) : new PcieId();
        DeviceSerialNumber = (Config.Length > 0x100) ? SeekDsn(inConfig[0x100..], littleEndian) : [];

        if (inVpd.Length <= 0) {
            Vpd = [];
            return;
        }

        Vpd = inVpd;
        ParseVpd(Vpd, out string pn, out string mn, out string sn, littleEndian);
        VpdPn = pn;
        VpdMn = mn;
        VpdSn = sn;
    }
    public static byte[] SeekDsn(byte[] inData, bool littleEndian = true) {
        byte[] dsn = [];
        int pos = 0;
        while((pos+12) < inData.Length) {
            byte[] capIdBytes = inData[pos..(pos + 2)];
            if (littleEndian) {
                Array.Reverse(capIdBytes);
            }

            if (capIdBytes[0] == 0 && capIdBytes[1] == 3) {
                dsn = inData[(pos + 4)..(pos + 12)];
                if (littleEndian) {
                    Array.Reverse(dsn);
                }

                break;
            } else {
                byte[] nextCapBytes = inData[(pos + 2)..(pos + 4)];
                if (littleEndian) {
                    Array.Reverse(nextCapBytes);
                }
                ushort nextCap = BinaryPrimitives.ReadUInt16BigEndian(nextCapBytes);
                nextCap >>= 4;
                if (nextCap == 0) {
                    break;
                }
                pos = nextCap - 0x100; // inData is not given the initial 256 bytes of the config space
            }
        }
        return dsn;
    }

    public static void ParseVpd(byte[] inData, out string pn, out string mn, out string sn, bool littleEndian = true) {
        pn = "";
        mn = "";
        sn = "";

        int pos = 0;
        byte tagId = 0;
        ushort tagDataLength = 0;

        // Search for VPD-R tag
        while (pos < inData.Length) {
            tagId = inData[pos];

            if (tagId == 0x0F) { // End Tag; Stop
                return;
            }

            tagDataLength = 0;
            bool largeTag = (tagId & 0x80) == 0x80;
            if (largeTag) { // Large Tag
                byte[] tagDataLengthBytes = inData[(pos+1)..(pos + 3)];
                if (littleEndian) {
                    Array.Reverse(tagDataLengthBytes);
                }
                tagDataLength = BinaryPrimitives.ReadUInt16BigEndian(tagDataLengthBytes);
            } else {
                tagDataLength = (ushort)(tagId & 0x3);
            }

            if (tagId == 0x90) { // VPD-R Tag
                break;
            }

            pos = pos + 1 + tagDataLength;
            if (largeTag) {
                pos += 2;
            }
        }

        if (tagId != 0x90) { // Stop if VPD-R not found
            return;
        }

        // At this point, pos should be pointing at the VPD-R tag id byte
        //  and tagDataLength should be the length of the VPD-R tag
        int tagIdPos = pos;
        pos += 3;
        int tagEnd = tagIdPos + 3 + tagDataLength;

        // Search for desired keywords
        while (pos < tagEnd) {
            string keyword = Encoding.ASCII.GetString(inData[pos..(pos + 2)]);
            int len = inData[pos+2];
            int start = pos+3;
            int end = start + len; // C# byte range end is not inclusive
            
            if (end > inData.Length) {
                break;
            }

            byte[] keywordDataBytes = inData[start..end];

            switch (keyword) {
                case "PN":
                case "pn":
                case "Pn":
                case "pN":
                    pn = Encoding.ASCII.GetString(keywordDataBytes);
                    break;
                case "MN":
                case "mn":
                case "Mn":
                case "mN":
                    mn = Encoding.ASCII.GetString(keywordDataBytes);
                    break;
                case "SN":
                case "sn":
                case "Sn":
                case "sN":
                    sn = Encoding.ASCII.GetString(keywordDataBytes);
                    break;
                case "RV":
                case "rv":
                case "Rv":
                case "rV":
                    byte checksum = inData[pos+3];
                    byte calc = 0;
                    for (int i = 0; i <= (pos + 3); i++) {
                        calc += inData[i];
                    }
                    if (calc != 0) {
                        pn = "";
                        mn = "";
                        sn = "";
                    }

                    break;
            }

            pos = end;
        }
    }
}