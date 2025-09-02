# Build

## Build ComponentClassRegistry tools

Notes:

- This build needs the .NET 10 SDK
- The SDK is only needed to build the tools from source. It is not needed to run the tools.

```bash or powershell
dotnet build dotnet/ComponentClassRegistry/ComponentClassRegistry.sln
```

## Build paccor

Notes: 

- `distZipLinux` and `distZipWin` will only work if the component class tools are built. Omit them to just build and test the java code.
- `buildRpm` and `buildDeb` are only necessary to build Linux distribution packages.

```bash
./gradlew clean build buildRpm buildDeb distZipLinux distZipWin
```

- The same options can be built on Windows.

```powershell
./gradlew.bat ...
```

Key outputs:

- `build/libs/` for jars
- `build/distributions/` for packaged archives

If you want a runnable local install layout from source, build an archive and unpack it:

```bash
./gradlew clean build distZip
mkdir -p local-install
unzip build/distributions/paccor-*.zip -d local-install
```

Then run `bin/paccor` from the unpacked directory instead of relying on an in-tree build path.

## Build generated docs inputs

```bash
./gradlew generateDocs
```

That task extracts ASN.1 notation from Javadoc, generates schema artifacts, and stages the generated reference fragments consumed by the MkDocs site.

## Build the docs site

If you have the MkDocs toolchain installed locally:

```bash
mkdocs build
```

The site consumes the curated Markdown under `docs/` plus the generated reference content staged by `./gradlew generateDocs`.
