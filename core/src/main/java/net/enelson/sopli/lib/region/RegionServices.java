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

        if ("1.16.5".equals(minecraftVersion)) {
            return tryLoad("RegionService_1_16_5");
        }

        if ("1.20.4".equals(minecraftVersion)) {
            return tryLoad("RegionService_1_20_4");
        }

        if ("1.21.1".equals(minecraftVersion)) {
            return tryLoad("RegionService_1_21_1");
        }

        if ("1.21.11".equals(minecraftVersion)) {
            return tryLoad("RegionService_1_21_11");
        }

        if (minecraftVersion.matches("26\\.1(?:\\.\\d+)*")) {
            return tryLoad("RegionService_26_1");
        }

        if (minecraftVersion.matches("26\\.2(?:\\.\\d+)*")) {
            return tryLoad("RegionService_26_2");
        }

        if (minecraftVersion.matches("26\\.3(?:\\.\\d+)*")) {
            return tryLoad("RegionService_26_3");
        }

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
