# Install

## Pre-built release

1. Download the package for your platform from the GitHub Releases page.
     * __Linux__: Install the `.deb` or `.rpm` package. Alternatively, the `.zip` package can be unpacked anywhere you like.
     * __Windows__: Unpack the `.zip` package anywhere you like.
2. Run the executable `bin/paccor`.
3. Run the tutorial commands from any directory that has access to `bin/paccor`. Download the linked input files into your working directory, or substitute any local paths that fit your environment.

This is the right path if you already have issuer certificates, holder certificates, and manifest JSON from your own environment.

## Source checkout

These steps will build and install paccor into the source repository folder.

1. Follow the instructions to [Clone the repository](<github.md#clone-the-repository>)
2. Read the [Requirements](requirements.md) page to install the appropriate version of Java and Dotnet.
3. From a terminal in the repository root, run the following command: `./gradlew clean installToSource`

Next:

- Read [Requirements](requirements.md) if you have not checked prerequisites yet
- Read [Build](build.md) if you want to compile and unpack the distribution yourself
- Jump to [Getting Started](../getting-started.md)
