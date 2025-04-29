namespace CliLib;

public enum ClientExitCodes {
    SUCCESS = 0, // Full successful program completion 
    FAIL = 1, // Unknown/Generic failure resulting in exit 
    NOT_PRIVILEGED = 23, // Client not run as root
    CLI_PARSE_ERROR = 24, // Command line parsing failure
    GATHER_HW_MANIFEST_FAIL = 43, // SMBIOS Hardware information retrieval failure
}
