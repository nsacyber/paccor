param(
    [parameter(Mandatory=$true)]
    [ValidateNotNull()]
    [string]$filename
)

### User customizable values
$APP_HOME=(Split-Path -parent $PSCommandPath)
$COMPONENTS_URI="" # Specify the optional components URI field
$COMPONENTS_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
$PROPERTIES_URI="" # Specify the optional properties URI field
$PROPERTIES_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
$JSON_SCRIPT="$APP_HOME/json.ps1" # Defines JSON structure and provides methods for producing relevant JSON

### Registry Options
#### Control which registries will be run
$INCLUDE_SMBIOS_REGISTRY=$true
$INCLUDE_PCIE_REGISTRY=$true
$INCLUDE_STORAGE_REGISTRY=$true
$INCLUDE_TCG_REGISTRY=$false
#### Expected paths of
$SMBIOS_REGISTRY_UTILITY="$APP_HOME/SmbiosCli.exe"
$PCIE_REGISTRY_UTILITY="$APP_HOME/PcieCli.exe"
$STORAGE_REGISTRY_UTILITY="$APP_HOME/StorageCli.exe"

## Some of the commands below require admin.
If(!(New-Object Security.Principal.WindowsPrincipal(
        [Security.Principal.WindowsIdentity]::GetCurrent())).IsInRole(
            [Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Output "Please run as admin"
	exit
}

### JSON
. $JSON_SCRIPT

### Base Registry Platform and Component data
$TCG_REGISTRY_SCRIPT="$APP_HOME/tcg_ccr.ps1" # Functions to collect hardware information labeled with the original TCG Component Class Registry
. $TCG_REGISTRY_SCRIPT

# Powershell Progress Group IDs:
#     1: Progress through allcomponents
#     2: Progress through each registry
#     3: Progress per component
#
# Don't forget to use -Completed !

### Gather component details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 10
$platformList=$(parseSystemData)
$platformObject=$(jsonPlatformObject "$platformList")
$smbiosRegistryData=""
$pcieRegistryData=""
$storageRegistryData=""
$tcgRegistryData=""
if ($INCLUDE_SMBIOS_REGISTRY -and (Test-Path -Path "$SMBIOS_REGISTRY_UTILITY")) {
    $smbiosRegistryData=$(& "$SMBIOS_REGISTRY_UTILITY" "--components-only")
}
if ($INCLUDE_PCIE_REGISTRY -and (Test-Path -Path "$PCIE_REGISTRY_UTILITY")) {
    $pcieRegistryData=$(& "$PCIE_REGISTRY_UTILITY" "--components-only")
}
if ($INCLUDE_STORAGE_REGISTRY -and (Test-Path -Path "$STORAGE_REGISTRY_UTILITY")) {
    $storageRegistryData=$(& "$STORAGE_REGISTRY_UTILITY" "--components-only")
}
if ($INCLUDE_TCG_REGISTRY) {
    $tcgRegistryData=$(collectOldTcgRegistryComponents)
}
$componentArray=$(jsonComponentArray "$smbiosRegistryData" "$pcieRegistryData" "$storageRegistryData" "$tcgRegistryData")

### Gather property details
Write-Progress -Id 1 -Activity "Gathering properties" -PercentComplete 80
$osCaption=(Get-WmiObject -Class Win32_OperatingSystem).caption
if ($null -ne $osCaption) {
    $osCaption = $osCaption.Trim()
}
$property1=(jsonProperty "caption" "$osCaption")  ## Example1
$property2=(jsonProperty "caption" "$osCaption") # "$JSON_STATUS_ADDED") ## Example2 with optional third status argument

### Collate the property details
$propertyArray=(jsonPropertyArray "$property1" "$property2")
#$propertyArray=(jsonPropertyArray)

### Collate the URI details, if parameters above are blank, the fields will be excluded from the final JSON structure
$componentsUri=""
if ($COMPONENTS_URI) {
    $componentsUri=$(jsonComponentsUri "$COMPONENTS_URI" "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH")
}
$propertiesUri=""
if ($PROPERTIES_URI) {
    $propertiesUri=$(jsonPropertiesUri "$PROPERTIES_URI" "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH")
}

Write-Progress -Id 1 -Activity "Forming final output" -PercentComplete 90
### Construct the final JSON object
$FINAL_JSON_OBJECT=$(jsonIntermediateFile "$platformObject" "$componentArray" "$componentsUri" "$propertyArray" "$propertiesUri")

Write-Progress -Id 1 -Activity "Done" -Completed
[IO.File]::WriteAllText($filename, "$FINAL_JSON_OBJECT")
