package net.enelson.sopli.lib.version;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class VersionManager {
    public static String normalize(String version) {
        if (version == null) return "unknown";
        return version.split("-")[0];
    }

    private static String extractVersionFromClassName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        String[] parts = name.split("_");
        if (parts.length < 3) return "unknown";

        String major = parts[1];
        String minor = parts[2];
        String patch = parts.length >= 4 ? parts[3] : "0";
        return major + "." + minor + "." + patch;
    }

    public static <T> T loadForVersion(Class<T> type, String serverVersion) {
        String normalized = normalize(serverVersion);
        String[] svParts = normalized.split("\\.");
        String svMajorMinor = svParts.length >= 2 ? svParts[0] + "." + svParts[1] : normalized;

        ServiceLoader<T> loader = ServiceLoader.load(type, VersionManager.class.getClassLoader());
        Iterator<T> iterator = loader.iterator();
        T exact = null;
        T sameMinor = null;

        while (true) {
            final boolean hasNext;
            try {
                hasNext = iterator.hasNext();
            } catch (ServiceConfigurationError error) {
                continue;
            }

            if (!hasNext) {
                break;
            }

            final T impl;
            try {
                impl = iterator.next();
            } catch (ServiceConfigurationError error) {
                continue;
            }

            String implVersion = extractVersionFromClassName(impl.getClass());
            String[] ivParts = implVersion.split("\\.");
            String ivMajorMinor = ivParts.length >= 2 ? ivParts[0] + "." + ivParts[1] : implVersion;

            if (implVersion.equals(normalized)) {
                exact = impl;
                break;
            }

            if (sameMinor == null && ivMajorMinor.equals(svMajorMinor)) {
                sameMinor = impl;
            }
        }

        if (exact != null) return exact;
        if (sameMinor != null) return sameMinor;
        return null;
    }
}