package net.enelson.sopli.lib.protection;

import org.bukkit.Bukkit;

public final class ProtectionServices {

    private static final String PACKAGE_NAME = "net.enelson.sopli.lib.protection.";

    private ProtectionServices() {
    }

    public static ProtectionService resolve() {
        String version = Bukkit.getBukkitVersion();
        if (version == null) {
            Bukkit.getLogger().warning("[SopLib] Bukkit version is null. Using noop ProtectionService.");
            return new ProtectionServiceNoop();
        }

        String minecraftVersion = version.split("-")[0];

        if ("1.16.5".equals(minecraftVersion)) {
            return tryLoad("ProtectionService_1_16_5");
        }

        if ("1.21.1".equals(minecraftVersion)) {
            return tryLoad("ProtectionService_1_21_1");
        }

        if ("1.21.11".equals(minecraftVersion)) {
            return tryLoad("ProtectionService_1_21_11");
        }

        Bukkit.getLogger().info("[SopLib] No ProtectionService adapter for " + minecraftVersion + ", using noop.");
        return new ProtectionServiceNoop();
    }

    private static ProtectionService tryLoad(String simpleName) {
        String fqcn = PACKAGE_NAME + simpleName;
        try {
            Class<?> clazz = Class.forName(fqcn);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            ProtectionService service = (ProtectionService) instance;
            Bukkit.getLogger().info("[SopLib] Loaded ProtectionService: " + fqcn);
            return service;
        } catch (Throwable throwable) {
            Bukkit.getLogger().severe("[SopLib] Failed to load ProtectionService: " + fqcn);
            throwable.printStackTrace();
            return new ProtectionServiceNoop();
        }
    }
}
