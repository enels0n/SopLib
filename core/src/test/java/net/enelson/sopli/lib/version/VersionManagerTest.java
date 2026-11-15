package net.enelson.sopli.lib.version;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionManagerTest {
    @Test public void normalizesPaperBuilds() {
        assertEquals("26.3", VersionManager.normalize("26.3.build.230-stable"));
        assertEquals("26.1.2", VersionManager.normalize("26.1.2.build.74-stable"));
        assertEquals("26.2", VersionManager.normalize(" 26.2.build.135-stable "));
    }

    @Test public void preservesLegacyVersions() {
        assertEquals("1.21.11", VersionManager.normalize("1.21.11-R0.1-SNAPSHOT"));
        assertEquals("1.16.5", VersionManager.normalize("1.16.5-R0.1-SNAPSHOT"));
        assertEquals("26.3", VersionManager.normalize("26.3"));
        assertEquals("unknown", VersionManager.normalize(null));
    }
}
