package net.enelson.sopli.lib.version;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class VersionManager {
    public static String normalize(String version) {
        if (version == null) return "unknown";
        return version.trim().split("\\.build\\.", 2)[0].split("-")[0];
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
        String suffix = ServerCompatibility.adapterSuffix(serverVersion);
        if (suffix == null) return null;

        ServiceLoader<T> loader = ServiceLoader.load(type, VersionManager.class.getClassLoader());
        Iterator<T> iterator = loader.iterator();

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

            if (impl.getClass().getSimpleName().endsWith("_" + suffix)) return impl;
        }

        return null;
    }
}
