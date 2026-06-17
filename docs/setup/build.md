# Build

## Build paccor

Notes: 

- `distZipLinux` and `distZipWin` will only work if the [component class tools are built](#build-componentclassregistry-tools). Omit them to just build and test the java code.
- `buildRpm` and `buildDeb` are only necessary to build Linux distribution packages.

```bash
./gradlew clean build release
```

- The same options can be built on Windows.

```powershell
./gradlew.bat ...
```

Key outputs:

- `build/libs/` for jars
- `build/distributions/` for packaged archives. Unpack the version that meets your system environment.

## Build ComponentClassRegistry tools

Notes:

- This build needs the .NET 10 SDK
- The SDK is only needed to build the tools from source. It is not needed to run the tools.

```text
dotnet build dotnet/ComponentClassRegistry/ComponentClassRegistry.sln
```

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
