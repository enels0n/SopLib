package net.enelson.sopli.lib.region;

import org.bukkit.Bukkit;

public final class RegionServices {

    private static final String PACKAGE_NAME = "net.enelson.sopli.lib.region.";

    private RegionServices() {
    }

    public static RegionService resolve() {
        String version = Bukkit.getBukkitVersion();
        if (version == null) {
            Bukkit.getLogger().warning("[SopLib] Bukkit version is null. Using noop RegionService.");
            return new RegionServiceNoop();
        }

        String minecraftVersion = net.enelson.sopli.lib.version.VersionManager.normalize(version);
        String suffix = net.enelson.sopli.lib.version.ServerCompatibility.adapterSuffix(version);
        if (suffix != null) return tryLoad("RegionService_" + suffix);

        Bukkit.getLogger().info("[SopLib] No RegionService adapter for " + minecraftVersion + ", using noop.");
        return new RegionServiceNoop();
    }

    private static RegionService tryLoad(String simpleName) {
        String fqcn = PACKAGE_NAME + simpleName;
        try {
            Class<?> clazz = Class.forName(fqcn);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            RegionService service = (RegionService) instance;
            Bukkit.getLogger().info("[SopLib] Loaded RegionService: " + fqcn);
            return service;
        } catch (Throwable throwable) {
            Bukkit.getLogger().severe("[SopLib] Failed to load RegionService: " + fqcn);
            throwable.printStackTrace();
            return new RegionServiceNoop();
        }
    }
}
