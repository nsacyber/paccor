# Build

## Build paccor

This will compile the java code and run the tests.

```bash
./gradlew clean build 
```

- The same options can be built on Windows.

```powershell
./gradlew.bat clean build
```

## Build paccor packages

This will build the component class tools and also create the ZIP files, RPM and DEB packages, docs files, and install to the source repo.

Note: 

- Distributable packages can only be built if the .NET SDK is installed. Gradle is configured to run dotnet publish 
on the required files. Alternatively, you can see the section that describes
how [component class tools are built](#build-componentclassregistry-tools).

```bash
./gradlew clean release
```

- The same options can be built on Windows.

```powershell
./gradlew.bat clean release
```

## Key outputs

- `build/libs/` for jars
- `build/distributions/` for packaged archives. Unpack the version that meets your system environment.

## Build ComponentClassRegistry tools

Notes:

- This build needs the .NET 10 SDK
- The SDK is only needed to build the tools from source. It is not needed to run the tools.

```text
dotnet build dotnet/ComponentClassRegistry/ComponentClassRegistry.sln
```

Gradle can also be used to build this solution.

```text
./gradlew dotnetPublish
```

## Build generated docs inputs

```bash
./gradlew generateDocs
```

That task extracts ASN.1 notation from Javadoc, generates schema artifacts, and stages the generated reference fragments consumed by the MkDocs site.

## Build the docs site

You can access the documentation site on the internet at https://nsacyber.github.io/paccor/

If you have MkDocs installed locally:

```bash
mkdocs build
```

The site uses the curated files under `docs/` plus the generated reference content created by `./gradlew generateDocs`.
