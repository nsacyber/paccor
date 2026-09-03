package paccor.cli.pv;

import picocli.CommandLine.IVersionProvider;

/**
 * Gets the version from build.gradle.
 */
public final class VersionProvider implements IVersionProvider {
    @Override
    public String[] getVersion() {
        String version = VersionProvider.class.getPackage().getImplementationVersion();
        return new String[] {version == null ? "development" : version};
    }
}
