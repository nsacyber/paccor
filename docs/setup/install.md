# Install

## Release packages

This is the simplest way to get started.

1. Download the package for your platform from the GitHub Releases page.
     * __Linux__: Choose from `.deb`, `.rpm`, or `.zip` packages.
       * `.deb`: Example command: `sudo apt-get install ./paccor_2.0r3_amd64.deb`
       * `.rpm`: Example command: `sudo dnf install ./paccor-2.0r3.x86_64.rpm`
       * `.zip`: Unpack the `.zip` package anywhere you like.
     * __Windows__: Unpack the `.zip` package anywhere you like.
2. Run the executable with the help command line option.
     * If you installed via a `.deb` or `.rpm` package, the executable is available at `/opt/paccor/bin/paccor -h`.
     * If you unpacked the `.zip` package, the executable is available at `bin/paccor -h`.
3. Run through the tutorials or getting started guide. Each tutorial links to input files that you can download into your working directory.


## From Source

These steps will build and install paccor into the source repository folder.

1. Follow the instructions to [Clone the repository](<github.md#clone-the-repository>)
2. You may want to check the [Requirements](requirements.md) page to verify the appropriate version of Java and Dotnet.
3. Ensure paccor can [build](build.md) cleanly.
4. From a terminal in the repository main folder, run the following command: `./gradlew clean installToSource`

Next:

- Read [Requirements](requirements.md) if you have not checked prerequisites yet
- Read [Build](build.md) if you want to compile and unpack the distribution yourself
- Jump to [Getting Started](../getting-started.md)
