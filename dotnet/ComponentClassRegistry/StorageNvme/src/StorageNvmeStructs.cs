using StorageLib;
using System.Runtime.InteropServices;

namespace StorageNvme;
public class StorageNvmeStructs {

    [StructLayout(LayoutKind.Sequential)]
    public struct NvmeIdentifyControllerData {
        // Identify Controller Data Structure : 4096 bytes
        // NVM Express® Base Specification, Revision 2.1, 5.1.13.2.1 Figure 312: Identify – Identify Controller Data Structure
        // Elements not relevant to the Storage CCR are marked internal and labeled outside scope
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        public byte[] VID; // byte 0:1 PCI Vendor ID (VID), little-endian
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        public byte[] SSVID; // byte 2:3 PCI Subsystem Vendor ID (SSVID), little-endian

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 20)]
        public byte[] SN; // byte 4:23 Serial Number (SN), string
           
        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 40)]
        public byte[] MN; // byte 24:63 Model Number (MN), string

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 8)]
        public byte[] FR; // byte 64:71 Firmware Revision (FR), string

        internal byte RAB; // byte 72 Recommended Arbitration Burst (RAB) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)]
        public byte[] IEEE; // byte 73:75 IEEE OUI Identifier (IEEE). Controller Vendor code, big endian

        internal byte CMIC; // byte 76 Controller Multi-Path I/O and Namespace Sharing Capabilities (CMIC) outside scope

        internal byte MDTS; // byte 77 Maximum Data Transfer Size (MDTS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CNTLID; // byte 78:79 Controller ID (CNTLID) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        public byte[] VER; // byte 80:83 Version (VER), little endian

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] RTD3R; // byte 84:87 RTD3 Resume Latency (RTD3R) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] RTD3E; // byte 88:91 RTD3 Entry Latency (RTD3E) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] OAES; // byte 92:95 Optional Asynchronous Events Supported (OAES) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] CTRATT; // byte 96:99 Controller Attributes (CTRATT) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] RRLS; // byte 100:101 Read Recovery Levels Supported (RRLS) outside scope

        internal byte BPCAP; // byte 102 Boot Partition Capabilities (BPCAP) outside scope

        internal byte Reserved103; // byte 103 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] NSSL; // byte 104:107 NVM Subsystem Shutdown Latency (NSSL) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] Reserved108; // byte 108:109 Reserved outside scope

        internal byte PLSI; // byte 110 Power Loss Signaling Information (PLSI) outside scope

        internal byte CNTRLTYPE; // byte 111 Controller Type (CNTRLTYPE) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] FGUID; // byte 112:127 FRU Globally Unique Identifier (FGUID)

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CRDT1; // byte 128:129 Command Retry Delay Time 1 (CRDT1) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CRDT2; // byte 130:131 Command Retry Delay Time 2 (CRDT2) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CRDT3; // byte 132:133 Command Retry Delay Time 3 (CRDT3) outside scope

        internal byte CRCAP; // byte 134 Controller Reachability Capabilities (CRCAP) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 105)]
        internal byte[] Reserved135; // byte 135:239 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 13)]
        internal byte[] Reserved240; // byte 240:252 Reserved for the NVMe Management Interface

        internal byte NVMSR; // byte 253 NVM Subsystem Report (NVMSR) outside scope outside scope

        internal byte VWCI; // byte 254 VPD Write Cycle Information (VWCI) outside scope

        internal byte MEC; // byte 255 Management Endpoint Capabilities (MEC) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] OACS; // byte 256:257 Optional Admin Command Support (OACS) outside scope

        internal byte ACL; // byte 258 Abort Command Limit (ACL) outside scope

        internal byte AERL; // byte 259 Asynchronous Event Request Limit (AERL) outside scope

        internal byte FRMW; // byte 260 Firmware Updates (FRMW) outside scope

        internal byte LPA; // byte 261 Log Page Attributes (LPA) outside scope

        internal byte ELPE; // byte 262 Error Log Page Entries (ELPE) outside scope

        internal byte NPSS; // byte 263 Number of Power States Support (NPSS) outside scope

        internal byte AVSCC; // byte 264 Admin Vendor Specific Command Configuration (AVSCC) outside scope

        internal byte APSTA; // byte 265 Autonomous Power State Transition Attributes (APSTA) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] WCTEMP; // byte 266:267 Warning Composite Temperature Threshold (WCTEMP) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CCTEMP; // byte 268:269 Critical Composite Temperature Threshold (CCTEMP) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MTFA; // byte 270:271 Maximum Time for Firmware Activation (MTFA) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] HMPRE; // byte 272:275 Host Memory Buffer Preferred Size (HMPRE) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] HMMIN; // byte 276:279 Host Memory Buffer Minimum Size (HMMIN) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        internal byte[] TNVMCAP; // byte 280:295 Total NVM Capacity (TNVMCAP) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        internal byte[] UNVMCAP; // byte 296:311 Unallocated NVM Capacity (UNVMCAP) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] RPMBS; // byte 312:315 Replay Protected Memory Block Support (RPMBS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] EDSTT; // byte 316:317 Extended Device Self-test Time (EDSTT) outside scope

        internal byte DSTO; // byte 318 Device Self-test Options (DSTO) outside scope

        internal byte FWUG; // byte 319 Firmware Update Granularity (FWUG) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] KAS; // byte 320:321 Keep Alive Support (KAS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] HCTMA; // byte 322:323 Host Controlled Thermal Management Attributes (HCTMA) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MNTMT; // byte 324:325 Minimum Thermal Management Temperature (MNTMT) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MXTMT; // byte 326:327 Maximum Thermal Management Temperature (MXTMT) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] SANICAP; // byte 328:331 Sanitize Capabilities (SANICAP) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] HMMINDS; // byte 332:335 Host Memory Buffer Minimum Descriptor Entry Size (HMMINDS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] HMMAXD; // byte 336:337 Host Memory Maximum Descriptors Entries (HMMAXD) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] NSETIDMAX; // byte 338:339 NVM Set Identifier Maximum (NSETIDMAX) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] ENDGIDMAX; // byte 340:341 Endurance Group Identifier Maximum (ENDGIDMAX) outside scope

        internal byte ANATT; // byte 342 ANA Transition Time (ANATT) outside scope

        internal byte ANACAP; // byte 343 Asymmetric Namespace Access Capabilities (ANACAP) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] ANAGRPMAX; // byte 344:347 ANA Group Identifier Maximum (ANAGRPMAX) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] NANAGRPID; // byte 348:351 Number of ANA Group Identifiers (NANAGRPID) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] PELS; // byte 352:355 Persistent Event Log Size (PELS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] DID; // byte 356:357 Domain Identifier (DID) outside scope

        internal byte KPIOC; // byte 358 Key Per I/O Capabilities (KPIOC) outside scope

        internal byte Reserved359; // byte 359 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MPTFAWR; // byte 360:361 Maximum Processing Time for Firmware Activation Without Reset (MPTFAWR) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 6)]
        internal byte[] Reserved362; // byte 362:367 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        internal byte[] MEGCAP; // byte 368:383 Max Endurance Group Capacity (MEGCAP) outside scope

        internal byte TMPTHHA; // byte 384 Temperature Threshold Hysteresis Attributes (TMPTHHA) outside scope

        internal byte Reserved385; // byte 385 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CQT; // byte 386:387 Command Quiesce Time (CQT) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 124)]
        internal byte[] Reserved388; // byte 388:511 Reserved outside scope

        internal byte SQES; // byte 512 Submission Queue Entry Size (SQES) outside scope

        internal byte CQES; // byte 513 Completion Queue Entry Size (CQES) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MAXCMD; // byte 514:515 Maximum Outstanding Commands (MAXCMD) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] NN; // byte 516:519 Number of Namespaces (NN) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] ONCS; // byte 520:521 Optional NVM Command Support (ONCS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] FUSES; // byte 522:523 Fused Operation Support (FUSES) outside scope

        internal byte FNA; // byte 524 Format NVM Attributes (FNA) outside scope

        internal byte VWC; // byte 525 Volatile Write Cache (VWC) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] AWUN; // byte 526:527 Atomic Write Unit Normal (AWUN) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] AWUPF; // byte 528:529 Atomic Write Unit Power Fail (AWUPF) outside scope

        internal byte ICSVSCC; // byte 530 I/O Command Set Vendor Specific Command Configuration (ICSVSCC) outside scope

        internal byte NWPC; // byte 531 Namespace Write Protection Capabilities (NWPC) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] ACWU; // byte 532:533 Atomic Compare & Write Unit (ACWU) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CDFS; // byte 534:535 Copy Descriptor Formats Supported (CDFS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] SGLS; // byte 536:539 SGL Support (SGLS) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] MNAN; // byte 540:543 Maximum Number of Allowed Namespaces (MNAN) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        internal byte[] MAXDNA; // byte 544:559 Maximum Domain Namespace Attachments (MAXDNA) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] MAXCNA; // byte 560:563 Maximum I/O Controller Namespace Attachments (MAXCNA) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] OAQD; // byte 564:567 Optimal Aggregated Queue Depth (OAQD) outside scope

        internal byte RHIRI; // byte 568 Recommended Host-Initiated Refresh Interval (RHIRI) outside scope

        internal byte HIRT; // byte 569 Host-Initiated Refresh Time (HIRT) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] CMMRTD; // byte 570:571 Controller Maximum Memory Range Tracking Descriptors (CMMRTD) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] NMMRTD; // byte 572:573 NVM Subsystem Maximum Memory Range Tracking Descriptors (NMMRTD) outside scope

        internal byte MINMRTG; // byte 574 Minimum Memory Range Tracking Granularity (MINMRTG) outside scope

        internal byte MAXMRTG; // byte 575 Maximum Memory Range Tracking Granularity (MAXMRTG) outside scope

        internal byte TRATTR; // byte 576 Tracking Attributes (TRATTR) outside scope

        internal byte Reserved577; // byte 577 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MCUDMQ; // byte 578:579 Maximum Controller User Data Migration Queues (MCUDMQ) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MNSUDMQ; // byte 580:581 Maximum NVM Subsystem User Data Migration Queues (MNSUDMQ) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MCMR; // byte 582:583 Maximum CDQ Memory Ranges (MCMR) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] NMCMR; // byte 584:585 NVM Subsystem Maximum CDQ Memory Ranges (NMCMR) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] MCDQPC; // byte 586:587 Maximum Controller Data Queue PRP Count (MCDQPC) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 180)]
        internal byte[] Reserved588; // byte 588:767 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 256)]
        public byte[] SUBNQN; // byte 768:1023 NVM Subsystem NVMe Qualified Name (SUBNQN)

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 768)]
        internal byte[] Reserved1024; // byte 1024:1791 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] IOCCSZ; // byte 1792:1795 I/O Queue Command Capsule Supported Size (IOCCSZ) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 4)]
        internal byte[] IORCSZ; // byte 1796:1799 I/O Queue Response Capsule Supported Size (IORCSZ) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] ICDOFF; // byte 1800:1801 In Capsule Data Offset (ICDOFF) outside scope

        internal byte FCATT; // byte 1802 Fabrics Controller Attributes (FCATT) outside scope

        internal byte MSDBD; // byte 1803 Maximum SGL Data Block Descriptors (MSDBD) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        internal byte[] OFCS; // byte 1804:1805 Optional Fabrics Commands Support (OFCS) outside scope

        internal byte DCTYPE; // byte 1806 Discovery Controller Type (DCTYPE) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 241)]
        internal byte[] Reserved1807; // byte 1807:2047 Reserved outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD0; // byte 2048:2079 Power State 0 Descriptor (PSD0) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD1; // byte 2080:2111 Power State 1 Descriptor (PSD1) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD2; // byte 2112:2143 Power State 2 Descriptor (PSD2) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD3; // byte 2144:2175 Power State 3 Descriptor (PSD3) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD4; // byte 2176:2207 Power State 4 Descriptor (PSD4) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD5; // byte 2208:2239 Power State 5 Descriptor (PSD5) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD6; // byte 2240:2271 Power State 6 Descriptor (PSD6) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD7; // byte 2272:2303 Power State 7 Descriptor (PSD7) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD8; // byte 2304:2335 Power State 8 Descriptor (PSD8) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD9; // byte 2336:2367 Power State 9 Descriptor (PSD9) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD10; // byte 2368:2399 Power State 10 Descriptor (PSD10) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD11; // byte 2400:2431 Power State 11 Descriptor (PSD11) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD12; // byte 2432:2463 Power State 12 Descriptor (PSD12) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD13; // byte 2464:2495 Power State 13 Descriptor (PSD13) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD14; // byte 2496:2527 Power State 14 Descriptor (PSD14) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD15; // byte 2528:2559 Power State 15 Descriptor (PSD15) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD16; // byte 2560:2591 Power State 16 Descriptor (PSD16) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD17; // byte 2592:2623 Power State 17 Descriptor (PSD17) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD18; // byte 2624:2655 Power State 18 Descriptor (PSD18) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD19; // byte 2656:2687 Power State 19 Descriptor (PSD19) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD20; // byte 2688:2719 Power State 20 Descriptor (PSD20) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD21; // byte 2720:2751 Power State 21 Descriptor (PSD21) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD22; // byte 2752:2783 Power State 22 Descriptor (PSD22) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD23; // byte 2784:2815 Power State 23 Descriptor (PSD23) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD24; // byte 2816:2847 Power State 24 Descriptor (PSD24) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD25; // byte 2848:2879 Power State 25 Descriptor (PSD25) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD26; // byte 2880:2911 Power State 26 Descriptor (PSD26) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD27; // byte 2912:2943 Power State 27 Descriptor (PSD27) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD28; // byte 2944:2975 Power State 28 Descriptor (PSD28) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD29; // byte 2976:3007 Power State 29 Descriptor (PSD29) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD30; // byte 3008:3039 Power State 30 Descriptor (PSD30) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        internal byte[] PSD31; // byte 3040:3071 Power State 31 Descriptor (PSD31) outside scope

        [MarshalAs(UnmanagedType.ByValArray, SizeConst = 1024)]
        internal byte[] VS; // byte 3072:4095 Vendor Specific (VS) outside scope


    }

    [StructLayout(LayoutKind.Sequential)]
    public struct NvmeCommandDwords {
        [MarshalAs(UnmanagedType.U4)] public uint DWord0;
        [MarshalAs(UnmanagedType.U4)] public uint DWord1;
        [MarshalAs(UnmanagedType.U4)] public uint DWord2;
        [MarshalAs(UnmanagedType.U4)] public uint DWord3;
        [MarshalAs(UnmanagedType.U4)] public uint DWord4;
        [MarshalAs(UnmanagedType.U4)] public uint DWord5;
        [MarshalAs(UnmanagedType.U4)] public uint DWord6;
        [MarshalAs(UnmanagedType.U4)] public uint DWord7;
        [MarshalAs(UnmanagedType.U4)] public uint DWord8;
        [MarshalAs(UnmanagedType.U4)] public uint DWord9;
        [MarshalAs(UnmanagedType.U4)] public uint DWord10;
        [MarshalAs(UnmanagedType.U4)] public uint DWord11;
        [MarshalAs(UnmanagedType.U4)] public uint DWord12;
        [MarshalAs(UnmanagedType.U4)] public uint DWord13;
        [MarshalAs(UnmanagedType.U4)] public uint DWord14;
        [MarshalAs(UnmanagedType.U4)] public uint DWord15;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct NvmeAdminCommand {
        public byte opcode;
        public byte flags;
        [MarshalAs(UnmanagedType.U2)] public ushort rsvd1;
        [MarshalAs(UnmanagedType.U4)] public uint nsid;
        [MarshalAs(UnmanagedType.U4)] public uint cdw2;
        [MarshalAs(UnmanagedType.U4)] public uint cdw3;
        [MarshalAs(UnmanagedType.U8)] public ulong metadata;
        [MarshalAs(UnmanagedType.U8)] public ulong addr;
        [MarshalAs(UnmanagedType.U4)] public uint metadataLength;
        [MarshalAs(UnmanagedType.U4)] public uint dataLength;
        [MarshalAs(UnmanagedType.U4)] public uint cdw10;
        [MarshalAs(UnmanagedType.U4)] public uint cdw11;
        [MarshalAs(UnmanagedType.U4)] public uint cdw12;
        [MarshalAs(UnmanagedType.U4)] public uint cdw13;
        [MarshalAs(UnmanagedType.U4)] public uint cdw14;
        [MarshalAs(UnmanagedType.U4)] public uint cdw15;
    }


    [StructLayout(LayoutKind.Explicit)]
    public struct NvmeCommand {
        // Both options here must be the same size (16 x 4 bytes = 64 bytes)
        // Technically ADMIN_COMMAND only makes sense if NVME_PASS_THROUGH_PARAMETERS.IsIOCommandSet is false
        [FieldOffset(0)] public NvmeCommandDwords Generic;
        [FieldOffset(0)] public NvmeAdminCommand Admin;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct CompletionQueueEntry {
        // COMPLETION_QUEUE_ENTRY
        [MarshalAs(UnmanagedType.U4)] public uint dw0;
        [MarshalAs(UnmanagedType.U4)] public uint dw1;
        [MarshalAs(UnmanagedType.U4)] public uint dw2;
        [MarshalAs(UnmanagedType.U4)] public uint dw3;
    }

    
}
