#!/bin/bash

REF=$1
PREF=$2
BUILD=$3

if [[ -z "${REF// }" ]]; then
    REF="main"
fi
if [[ -z "${PREF// }" ]]; then
    PREF="main"
fi

# Check out HIRS branch
cd /hirs
git fetch origin && git pull origin main && git reset --hard
git checkout $REF && git reset --hard

# Check out paccor branch
cd /paccor
git fetch origin && git pull origin main && git reset --hard
git checkout $PREF && git reset --hard

if [[ -z "${BUILD// }" ]]; then
    exit
fi

rm -rf /hirs/**/build
rm -rf /paccor/build

cd /hirs
./gradlew clean bootWar

cd /paccor
./gradlew clean build 

# Build Provisioner
cd /hirs/HIRS_Provisioner.NET
dotnet restore
/hirs/HIRS_Provisioner.NET/hirs
dotnet test
dotnet deb install
dotnet rpm install
dotnet zip install
dotnet deb -r linux-x64 -c Debug
dotnet rpm -r linux-x64 -c Debug
dotnet zip -r linux-x64 -c Debug
dotnet deb -r linux-x64 -c Release
dotnet rpm -r linux-x64 -c Release
dotnet zip -r linux-x64 -c Release

# Build HardwareManifestPlugin
cd /paccor/dotnet/HardwareManifestPlugin
dotnet restore
cd /paccor/dotnet/HardwareManifestPlugin/HardwareManifestPlugin
dotnet build -r linux-x64 -c Debug
dotnet build -r linux-x64 -c Release
cd /paccor/dotnet/HardwareManifestPlugin/HardwareManifestPluginManager
dotnet build -r linux-x64 -c Debug
dotnet build -r linux-x64 -c Release

# Build paccor_scripts
cd /paccor/dotnet/paccor_scripts
dotnet restore
cd /paccor/dotnet/paccor_scripts/paccor_scripts
dotnet build -r linux-x64 -c Release

# Build Component Class registries
cd /paccor/dotnet/ComponentClassRegistry
dotnet restore
cd /paccor/dotnet/ComponentClassRegistry/PcieCli
dotnet deb install
dotnet rpm install
dotnet zip install
dotnet deb -r linux-x64 -c Debug
dotnet rpm -r linux-x64 -c Debug
dotnet zip -r linux-x64 -c Debug
dotnet zip -r win-x64 -c Debug
dotnet deb -r linux-x64 -c Release
dotnet rpm -r linux-x64 -c Release
dotnet zip -r linux-x64 -c Release
dotnet zip -r win-x64 -c Release
cd /paccor/dotnet/ComponentClassRegistry/SmbiosCli
dotnet deb install
dotnet rpm install
dotnet zip install
dotnet deb -r linux-x64 -c Debug
dotnet rpm -r linux-x64 -c Debug
dotnet zip -r linux-x64 -c Debug
dotnet zip -r win-x64 -c Debug
dotnet deb -r linux-x64 -c Release
dotnet rpm -r linux-x64 -c Release
dotnet zip -r linux-x64 -c Release
dotnet zip -r win-x64 -c Release
cd /paccor/dotnet/ComponentClassRegistry/StorageCli
dotnet deb install
dotnet rpm install
dotnet zip install
dotnet deb -r linux-x64 -c Debug
dotnet rpm -r linux-x64 -c Debug
dotnet zip -r linux-x64 -c Debug
dotnet zip -r win-x64 -c Debug
dotnet deb -r linux-x64 -c Release
dotnet rpm -r linux-x64 -c Release
dotnet zip -r linux-x64 -c Release
dotnet zip -r win-x64 -c Release

cd /paccor
./gradlew buildDeb buildRpm distZipLinux distZipWin


