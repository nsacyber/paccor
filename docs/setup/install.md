# Install

## Pre-built release

1. Download the package for your platform from the GitHub Releases page.
2. Unpack the archive or install the `.deb` or `.rpm` package.
3. Run the launcher from `bin/paccor`.
4. Run the tutorial commands from any directory that has access to `bin/paccor`. Download the linked input files into your working directory, or substitute any local paths that fit your environment.

This is the right path if you already have issuer certificates, holder certificates, and manifest JSON from your own environment.

## Source checkout

```bash
git clone https://github.com/nsacyber/paccor.git
cd paccor
```

A source checkout is useful for contributors and test authors. The end-user tutorials are written for a runnable `bin/paccor` plus linked repository-backed input files instead of the source tree layout.

Next:

- Read [Requirements](requirements.md) if you have not checked prerequisites yet
- Read [Build](build.md) if you want to compile and unpack the distribution yourself
- Jump to [Getting Started](../getting-started.md) if you already have a runnable `paccor`
