using System.Runtime.InteropServices;

namespace StorageAta;
public class StorageAtaStructs {
    [StructLayout(LayoutKind.Sequential)]
    public struct AtaPageDataWords {
        [MarshalAs(UnmanagedType.U2)] public ushort Word0;
        [MarshalAs(UnmanagedType.U2)] public ushort Word1;
        [MarshalAs(UnmanagedType.U2)] public ushort Word2;
        [MarshalAs(UnmanagedType.U2)] public ushort Word3;
        [MarshalAs(UnmanagedType.U2)] public ushort Word4;
        [MarshalAs(UnmanagedType.U2)] public ushort Word5;
        [MarshalAs(UnmanagedType.U2)] public ushort Word6;
        [MarshalAs(UnmanagedType.U2)] public ushort Word7;
        [MarshalAs(UnmanagedType.U2)] public ushort Word8;
        [MarshalAs(UnmanagedType.U2)] public ushort Word9;
        [MarshalAs(UnmanagedType.U2)] public ushort Word10;
        [MarshalAs(UnmanagedType.U2)] public ushort Word11;
        [MarshalAs(UnmanagedType.U2)] public ushort Word12;
        [MarshalAs(UnmanagedType.U2)] public ushort Word13;
        [MarshalAs(UnmanagedType.U2)] public ushort Word14;
        [MarshalAs(UnmanagedType.U2)] public ushort Word15;
        [MarshalAs(UnmanagedType.U2)] public ushort Word16;
        [MarshalAs(UnmanagedType.U2)] public ushort Word17;
        [MarshalAs(UnmanagedType.U2)] public ushort Word18;
        [MarshalAs(UnmanagedType.U2)] public ushort Word19;
        [MarshalAs(UnmanagedType.U2)] public ushort Word20;
        [MarshalAs(UnmanagedType.U2)] public ushort Word21;
        [MarshalAs(UnmanagedType.U2)] public ushort Word22;
        [MarshalAs(UnmanagedType.U2)] public ushort Word23;
        [MarshalAs(UnmanagedType.U2)] public ushort Word24;
        [MarshalAs(UnmanagedType.U2)] public ushort Word25;
        [MarshalAs(UnmanagedType.U2)] public ushort Word26;
        [MarshalAs(UnmanagedType.U2)] public ushort Word27;
        [MarshalAs(UnmanagedType.U2)] public ushort Word28;
        [MarshalAs(UnmanagedType.U2)] public ushort Word29;
        [MarshalAs(UnmanagedType.U2)] public ushort Word30;
        [MarshalAs(UnmanagedType.U2)] public ushort Word31;
        [MarshalAs(UnmanagedType.U2)] public ushort Word32;
        [MarshalAs(UnmanagedType.U2)] public ushort Word33;
        [MarshalAs(UnmanagedType.U2)] public ushort Word34;
        [MarshalAs(UnmanagedType.U2)] public ushort Word35;
        [MarshalAs(UnmanagedType.U2)] public ushort Word36;
        [MarshalAs(UnmanagedType.U2)] public ushort Word37;
        [MarshalAs(UnmanagedType.U2)] public ushort Word38;
        [MarshalAs(UnmanagedType.U2)] public ushort Word39;
        [MarshalAs(UnmanagedType.U2)] public ushort Word40;
        [MarshalAs(UnmanagedType.U2)] public ushort Word41;
        [MarshalAs(UnmanagedType.U2)] public ushort Word42;
        [MarshalAs(UnmanagedType.U2)] public ushort Word43;
        [MarshalAs(UnmanagedType.U2)] public ushort Word44;
        [MarshalAs(UnmanagedType.U2)] public ushort Word45;
        [MarshalAs(UnmanagedType.U2)] public ushort Word46;
        [MarshalAs(UnmanagedType.U2)] public ushort Word47;
        [MarshalAs(UnmanagedType.U2)] public ushort Word48;
        [MarshalAs(UnmanagedType.U2)] public ushort Word49;
        [MarshalAs(UnmanagedType.U2)] public ushort Word50;
        [MarshalAs(UnmanagedType.U2)] public ushort Word51;
        [MarshalAs(UnmanagedType.U2)] public ushort Word52;
        [MarshalAs(UnmanagedType.U2)] public ushort Word53;
        [MarshalAs(UnmanagedType.U2)] public ushort Word54;
        [MarshalAs(UnmanagedType.U2)] public ushort Word55;
        [MarshalAs(UnmanagedType.U2)] public ushort Word56;
        [MarshalAs(UnmanagedType.U2)] public ushort Word57;
        [MarshalAs(UnmanagedType.U2)] public ushort Word58;
        [MarshalAs(UnmanagedType.U2)] public ushort Word59;
        [MarshalAs(UnmanagedType.U2)] public ushort Word60;
        [MarshalAs(UnmanagedType.U2)] public ushort Word61;
        [MarshalAs(UnmanagedType.U2)] public ushort Word62;
        [MarshalAs(UnmanagedType.U2)] public ushort Word63;
        [MarshalAs(UnmanagedType.U2)] public ushort Word64;
        [MarshalAs(UnmanagedType.U2)] public ushort Word65;
        [MarshalAs(UnmanagedType.U2)] public ushort Word66;
        [MarshalAs(UnmanagedType.U2)] public ushort Word67;
        [MarshalAs(UnmanagedType.U2)] public ushort Word68;
        [MarshalAs(UnmanagedType.U2)] public ushort Word69;
        [MarshalAs(UnmanagedType.U2)] public ushort Word70;
        [MarshalAs(UnmanagedType.U2)] public ushort Word71;
        [MarshalAs(UnmanagedType.U2)] public ushort Word72;
        [MarshalAs(UnmanagedType.U2)] public ushort Word73;
        [MarshalAs(UnmanagedType.U2)] public ushort Word74;
        [MarshalAs(UnmanagedType.U2)] public ushort Word75;
        [MarshalAs(UnmanagedType.U2)] public ushort Word76;
        [MarshalAs(UnmanagedType.U2)] public ushort Word77;
        [MarshalAs(UnmanagedType.U2)] public ushort Word78;
        [MarshalAs(UnmanagedType.U2)] public ushort Word79;
        [MarshalAs(UnmanagedType.U2)] public ushort Word80;
        [MarshalAs(UnmanagedType.U2)] public ushort Word81;
        [MarshalAs(UnmanagedType.U2)] public ushort Word82;
        [MarshalAs(UnmanagedType.U2)] public ushort Word83;
        [MarshalAs(UnmanagedType.U2)] public ushort Word84;
        [MarshalAs(UnmanagedType.U2)] public ushort Word85;
        [MarshalAs(UnmanagedType.U2)] public ushort Word86;
        [MarshalAs(UnmanagedType.U2)] public ushort Word87;
        [MarshalAs(UnmanagedType.U2)] public ushort Word88;
        [MarshalAs(UnmanagedType.U2)] public ushort Word89;
        [MarshalAs(UnmanagedType.U2)] public ushort Word90;
        [MarshalAs(UnmanagedType.U2)] public ushort Word91;
        [MarshalAs(UnmanagedType.U2)] public ushort Word92;
        [MarshalAs(UnmanagedType.U2)] public ushort Word93;
        [MarshalAs(UnmanagedType.U2)] public ushort Word94;
        [MarshalAs(UnmanagedType.U2)] public ushort Word95;
        [MarshalAs(UnmanagedType.U2)] public ushort Word96;
        [MarshalAs(UnmanagedType.U2)] public ushort Word97;
        [MarshalAs(UnmanagedType.U2)] public ushort Word98;
        [MarshalAs(UnmanagedType.U2)] public ushort Word99;
        [MarshalAs(UnmanagedType.U2)] public ushort Word100;
        [MarshalAs(UnmanagedType.U2)] public ushort Word101;
        [MarshalAs(UnmanagedType.U2)] public ushort Word102;
        [MarshalAs(UnmanagedType.U2)] public ushort Word103;
        [MarshalAs(UnmanagedType.U2)] public ushort Word104;
        [MarshalAs(UnmanagedType.U2)] public ushort Word105;
        [MarshalAs(UnmanagedType.U2)] public ushort Word106;
        [MarshalAs(UnmanagedType.U2)] public ushort Word107;
        [MarshalAs(UnmanagedType.U2)] public ushort Word108;
        [MarshalAs(UnmanagedType.U2)] public ushort Word109;
        [MarshalAs(UnmanagedType.U2)] public ushort Word110;
        [MarshalAs(UnmanagedType.U2)] public ushort Word111;
        [MarshalAs(UnmanagedType.U2)] public ushort Word112;
        [MarshalAs(UnmanagedType.U2)] public ushort Word113;
        [MarshalAs(UnmanagedType.U2)] public ushort Word114;
        [MarshalAs(UnmanagedType.U2)] public ushort Word115;
        [MarshalAs(UnmanagedType.U2)] public ushort Word116;
        [MarshalAs(UnmanagedType.U2)] public ushort Word117;
        [MarshalAs(UnmanagedType.U2)] public ushort Word118;
        [MarshalAs(UnmanagedType.U2)] public ushort Word119;
        [MarshalAs(UnmanagedType.U2)] public ushort Word120;
        [MarshalAs(UnmanagedType.U2)] public ushort Word121;
        [MarshalAs(UnmanagedType.U2)] public ushort Word122;
        [MarshalAs(UnmanagedType.U2)] public ushort Word123;
        [MarshalAs(UnmanagedType.U2)] public ushort Word124;
        [MarshalAs(UnmanagedType.U2)] public ushort Word125;
        [MarshalAs(UnmanagedType.U2)] public ushort Word126;
        [MarshalAs(UnmanagedType.U2)] public ushort Word127;
        [MarshalAs(UnmanagedType.U2)] public ushort Word128;
        [MarshalAs(UnmanagedType.U2)] public ushort Word129;
        [MarshalAs(UnmanagedType.U2)] public ushort Word130;
        [MarshalAs(UnmanagedType.U2)] public ushort Word131;
        [MarshalAs(UnmanagedType.U2)] public ushort Word132;
        [MarshalAs(UnmanagedType.U2)] public ushort Word133;
        [MarshalAs(UnmanagedType.U2)] public ushort Word134;
        [MarshalAs(UnmanagedType.U2)] public ushort Word135;
        [MarshalAs(UnmanagedType.U2)] public ushort Word136;
        [MarshalAs(UnmanagedType.U2)] public ushort Word137;
        [MarshalAs(UnmanagedType.U2)] public ushort Word138;
        [MarshalAs(UnmanagedType.U2)] public ushort Word139;
        [MarshalAs(UnmanagedType.U2)] public ushort Word140;
        [MarshalAs(UnmanagedType.U2)] public ushort Word141;
        [MarshalAs(UnmanagedType.U2)] public ushort Word142;
        [MarshalAs(UnmanagedType.U2)] public ushort Word143;
        [MarshalAs(UnmanagedType.U2)] public ushort Word144;
        [MarshalAs(UnmanagedType.U2)] public ushort Word145;
        [MarshalAs(UnmanagedType.U2)] public ushort Word146;
        [MarshalAs(UnmanagedType.U2)] public ushort Word147;
        [MarshalAs(UnmanagedType.U2)] public ushort Word148;
        [MarshalAs(UnmanagedType.U2)] public ushort Word149;
        [MarshalAs(UnmanagedType.U2)] public ushort Word150;
        [MarshalAs(UnmanagedType.U2)] public ushort Word151;
        [MarshalAs(UnmanagedType.U2)] public ushort Word152;
        [MarshalAs(UnmanagedType.U2)] public ushort Word153;
        [MarshalAs(UnmanagedType.U2)] public ushort Word154;
        [MarshalAs(UnmanagedType.U2)] public ushort Word155;
        [MarshalAs(UnmanagedType.U2)] public ushort Word156;
        [MarshalAs(UnmanagedType.U2)] public ushort Word157;
        [MarshalAs(UnmanagedType.U2)] public ushort Word158;
        [MarshalAs(UnmanagedType.U2)] public ushort Word159;
        [MarshalAs(UnmanagedType.U2)] public ushort Word160;
        [MarshalAs(UnmanagedType.U2)] public ushort Word161;
        [MarshalAs(UnmanagedType.U2)] public ushort Word162;
        [MarshalAs(UnmanagedType.U2)] public ushort Word163;
        [MarshalAs(UnmanagedType.U2)] public ushort Word164;
        [MarshalAs(UnmanagedType.U2)] public ushort Word165;
        [MarshalAs(UnmanagedType.U2)] public ushort Word166;
        [MarshalAs(UnmanagedType.U2)] public ushort Word167;
        [MarshalAs(UnmanagedType.U2)] public ushort Word168;
        [MarshalAs(UnmanagedType.U2)] public ushort Word169;
        [MarshalAs(UnmanagedType.U2)] public ushort Word170;
        [MarshalAs(UnmanagedType.U2)] public ushort Word171;
        [MarshalAs(UnmanagedType.U2)] public ushort Word172;
        [MarshalAs(UnmanagedType.U2)] public ushort Word173;
        [MarshalAs(UnmanagedType.U2)] public ushort Word174;
        [MarshalAs(UnmanagedType.U2)] public ushort Word175;
        [MarshalAs(UnmanagedType.U2)] public ushort Word176;
        [MarshalAs(UnmanagedType.U2)] public ushort Word177;
        [MarshalAs(UnmanagedType.U2)] public ushort Word178;
        [MarshalAs(UnmanagedType.U2)] public ushort Word179;
        [MarshalAs(UnmanagedType.U2)] public ushort Word180;
        [MarshalAs(UnmanagedType.U2)] public ushort Word181;
        [MarshalAs(UnmanagedType.U2)] public ushort Word182;
        [MarshalAs(UnmanagedType.U2)] public ushort Word183;
        [MarshalAs(UnmanagedType.U2)] public ushort Word184;
        [MarshalAs(UnmanagedType.U2)] public ushort Word185;
        [MarshalAs(UnmanagedType.U2)] public ushort Word186;
        [MarshalAs(UnmanagedType.U2)] public ushort Word187;
        [MarshalAs(UnmanagedType.U2)] public ushort Word188;
        [MarshalAs(UnmanagedType.U2)] public ushort Word189;
        [MarshalAs(UnmanagedType.U2)] public ushort Word190;
        [MarshalAs(UnmanagedType.U2)] public ushort Word191;
        [MarshalAs(UnmanagedType.U2)] public ushort Word192;
        [MarshalAs(UnmanagedType.U2)] public ushort Word193;
        [MarshalAs(UnmanagedType.U2)] public ushort Word194;
        [MarshalAs(UnmanagedType.U2)] public ushort Word195;
        [MarshalAs(UnmanagedType.U2)] public ushort Word196;
        [MarshalAs(UnmanagedType.U2)] public ushort Word197;
        [MarshalAs(UnmanagedType.U2)] public ushort Word198;
        [MarshalAs(UnmanagedType.U2)] public ushort Word199;
        [MarshalAs(UnmanagedType.U2)] public ushort Word200;
        [MarshalAs(UnmanagedType.U2)] public ushort Word201;
        [MarshalAs(UnmanagedType.U2)] public ushort Word202;
        [MarshalAs(UnmanagedType.U2)] public ushort Word203;
        [MarshalAs(UnmanagedType.U2)] public ushort Word204;
        [MarshalAs(UnmanagedType.U2)] public ushort Word205;
        [MarshalAs(UnmanagedType.U2)] public ushort Word206;
        [MarshalAs(UnmanagedType.U2)] public ushort Word207;
        [MarshalAs(UnmanagedType.U2)] public ushort Word208;
        [MarshalAs(UnmanagedType.U2)] public ushort Word209;
        [MarshalAs(UnmanagedType.U2)] public ushort Word210;
        [MarshalAs(UnmanagedType.U2)] public ushort Word211;
        [MarshalAs(UnmanagedType.U2)] public ushort Word212;
        [MarshalAs(UnmanagedType.U2)] public ushort Word213;
        [MarshalAs(UnmanagedType.U2)] public ushort Word214;
        [MarshalAs(UnmanagedType.U2)] public ushort Word215;
        [MarshalAs(UnmanagedType.U2)] public ushort Word216;
        [MarshalAs(UnmanagedType.U2)] public ushort Word217;
        [MarshalAs(UnmanagedType.U2)] public ushort Word218;
        [MarshalAs(UnmanagedType.U2)] public ushort Word219;
        [MarshalAs(UnmanagedType.U2)] public ushort Word220;
        [MarshalAs(UnmanagedType.U2)] public ushort Word221;
        [MarshalAs(UnmanagedType.U2)] public ushort Word222;
        [MarshalAs(UnmanagedType.U2)] public ushort Word223;
        [MarshalAs(UnmanagedType.U2)] public ushort Word224;
        [MarshalAs(UnmanagedType.U2)] public ushort Word225;
        [MarshalAs(UnmanagedType.U2)] public ushort Word226;
        [MarshalAs(UnmanagedType.U2)] public ushort Word227;
        [MarshalAs(UnmanagedType.U2)] public ushort Word228;
        [MarshalAs(UnmanagedType.U2)] public ushort Word229;
        [MarshalAs(UnmanagedType.U2)] public ushort Word230;
        [MarshalAs(UnmanagedType.U2)] public ushort Word231;
        [MarshalAs(UnmanagedType.U2)] public ushort Word232;
        [MarshalAs(UnmanagedType.U2)] public ushort Word233;
        [MarshalAs(UnmanagedType.U2)] public ushort Word234;
        [MarshalAs(UnmanagedType.U2)] public ushort Word235;
        [MarshalAs(UnmanagedType.U2)] public ushort Word236;
        [MarshalAs(UnmanagedType.U2)] public ushort Word237;
        [MarshalAs(UnmanagedType.U2)] public ushort Word238;
        [MarshalAs(UnmanagedType.U2)] public ushort Word239;
        [MarshalAs(UnmanagedType.U2)] public ushort Word240;
        [MarshalAs(UnmanagedType.U2)] public ushort Word241;
        [MarshalAs(UnmanagedType.U2)] public ushort Word242;
        [MarshalAs(UnmanagedType.U2)] public ushort Word243;
        [MarshalAs(UnmanagedType.U2)] public ushort Word244;
        [MarshalAs(UnmanagedType.U2)] public ushort Word245;
        [MarshalAs(UnmanagedType.U2)] public ushort Word246;
        [MarshalAs(UnmanagedType.U2)] public ushort Word247;
        [MarshalAs(UnmanagedType.U2)] public ushort Word248;
        [MarshalAs(UnmanagedType.U2)] public ushort Word249;
        [MarshalAs(UnmanagedType.U2)] public ushort Word250;
        [MarshalAs(UnmanagedType.U2)] public ushort Word251;
        [MarshalAs(UnmanagedType.U2)] public ushort Word252;
        [MarshalAs(UnmanagedType.U2)] public ushort Word253;
        [MarshalAs(UnmanagedType.U2)] public ushort Word254;
        [MarshalAs(UnmanagedType.U2)] public ushort Word255;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct AtaIdentifyData {
        // Overall size must be 512 bytes (256 x 2 bytes = 512 bytes)
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 216)]
        internal byte[] DontCare1; // 0:215 Just need this page for WWN. May fill the rest in another time.

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] WWN; // byte 216:223 World Wide Name

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 288)]
        internal byte[] DontCare2; // 224:511 Just need this page for WWN. May fill the rest in another time.
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct AtaCapabilitiesData {
        // Overall size must be 512 bytes (256 x 2 bytes = 512 bytes)
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] Header; // byte 0:7 Supported Capabilities page information header

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] Caps; // byte 7:15 Supported Capabilities

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] DMC; // byte 16:23 Download Microcode Capabilities

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] NMRR; // byte 24:31 Nominal Media Rotation Rate

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] FormFactor; // byte 32:39 Form Factor

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] WRVSCM3; // byte 40:47 Write-Read-Verify Sector Count Mode 3

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] WRVSCM2; // byte 48:55 Write-Read-Verify Sector Count Mode 2

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] WWN; // byte 56:71 World Wide Name

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] DSM; // byte 72:79 Data Set Management

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        internal byte[] UPUT; // byte 80:95 Utilization Per Unit Time

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] UURS; // byte 96:103 Utilization Usage Rate Support

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] ZC; // byte 104:111 Zoned Capabilities

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] SZC; // byte 112:119 Supported ZAC Capabilities

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] ABOC; // byte 120:127 Advanced Background Operations Capabilities

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] ABOR; // byte 128:135 Advanced Background Operations Recommendations

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] QD; // byte 136:143 Queue Depth

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] SSC; // byte 144:151 Supported SCT Capabilities

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] DC; // byte 152:159 Depopulation Capabilities

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] DET; // byte 160:167 Depopulation Execution Time

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] CDLS; // byte 168:175 Command Duration Limit Supported

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] CDLMinTL; // byte 176:183 Command Duration Limit Minimum Time Limit

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] CDLMaxTL; // byte 184:191 Command Duration Limit Maximum Time Limit

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 312)]
        internal byte[] Reserved; // byte 192:503 Reserved

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] VSSC; // byte 504:511 Vendor Specific Supported Capabilities
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct AtaStringsData {
        // Overall size must be 512 bytes (256 x 2 bytes = 512 bytes)
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] Header;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 20)]
        public byte[] SN; // byte 8:27 Serial number, ATA string

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] Reserved1;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] FR; // byte 32:39 Firmware revision, ATA string

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] Reserved2;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 40)]
        public byte[] MN; // byte 48:87 Model number, ATA string

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        internal byte[] Reserved3;

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] AdditionalProductIdentifier; // byte 96:103 ATA string

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 408)]
        internal byte[] Reserved4;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct AtaLogTaskIn {
        [MarshalAs(UnmanagedType.U2)] public ushort Feature;
        [MarshalAs(UnmanagedType.U2)] public ushort Count;
        public byte LogAddress;
        public byte PageNumberMsb;
        [MarshalAs(UnmanagedType.U2)] internal ushort Reserved1;
        public byte PageNumberLsb;
        internal byte Reserved2;
        public byte Device;
        public byte Command;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct AtaLogTaskOut {
        public byte Error;
        [MarshalAs(UnmanagedType.U2)] internal ushort Count;  // Reserved
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 6)]
        internal byte Lba; // Reserved
        public byte Device;
        public byte Status;
    }
}

