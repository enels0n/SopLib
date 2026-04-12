package net.enelson.sopli.lib.version;

public abstract class AbstractVersioned {
    private final String version;

    protected AbstractVersioned(String version) {
        this.version = version;
    }

    public boolean supports(String mcVersion) {
        return this.version.equals(mcVersion);
    }

    public String getVersion() {
        return version;
    }
}