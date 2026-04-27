# paccor

`paccor` creates, signs, inspects, and validates TCG Platform Certificates from documented JSON inputs and collected platform data.

The project supports the TCG Platform Certificate profile families currently documented in this repository, including v2.1, v1.1, and v1.0 workflows where the profile applies.

## Documentation

The primary documentation entry point is the GitHub Pages site:

- https://nsacyber.github.io/paccor/

Recommended starting points:

- [Getting Started](https://nsacyber.github.io/paccor/getting-started/)
- [Certificate Tutorials](https://nsacyber.github.io/paccor/tutorials/paccor/)
- [CLI Commands](https://nsacyber.github.io/paccor/reference/cli-commands/)
- [Signing Algorithms](https://nsacyber.github.io/paccor/reference/signing-algorithms/)
- [Build and Docs Setup](https://nsacyber.github.io/paccor/setup/build/)

The Markdown source for the published site lives under `docs/`.

## What paccor does

- Builds platform-certificate data from hardware manifests, policy JSON, and extension inputs
- Generates to-be-signed certificate envelopes with `certgen`
- Assembles signed certificates with `assemble`
- Verifies output with `validate`
- Inspects certificate contents with `view`

Current top-level CLI commands:

```text
paccor certgen
paccor assemble
paccor validate
paccor view
```

## Quick Start

If you want the shortest path through the project, use the guided documentation:

1. Open the GitHub Pages site.
2. Follow the [Getting Started](https://nsacyber.github.io/paccor/getting-started/) guide.
3. Use the profile-specific tutorials when you need a reproducible certificate flow.

Release packages are published on the GitHub Releases page:

- https://github.com/nsacyber/paccor/releases

## Build From Source

Prerequisites:

- Java 25
- Gradle-compatible environment using the included wrapper
- .NET 10 SDK if you need to build the ComponentClassRegistry tools from source

Build the Java project:

```bash
./gradlew clean build
```

Build packaged distributions when needed:

```bash
./gradlew clean build buildRpm buildDeb distZipLinux distZipWin
```

Generate the documentation inputs consumed by the site:

```bash
./gradlew generateDocs
```

Build the documentation site locally if you have MkDocs installed:

```bash
mkdocs build
```

## Repository Layout

- `docs/` - GitHub Pages and MkDocs documentation source
- `src/` - Java implementation
- `dotnet/` - hardware collection and ComponentClassRegistry tooling
- `scripts/` - helper flows, including guided certificate-generation scripts

## Legacy Reference

The original PDF user guide remains available in the published docs:

- https://nsacyber.github.io/paccor/_assets/platformCertificateCreator.pdf

## Project Status

This repository is maintained by the NSA Cybersecurity Directorate.
