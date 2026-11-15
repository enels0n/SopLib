package net.enelson.sopli.lib.corpse;

import org.bukkit.Bukkit;

public final class CorpseServices {

    private static final String PACKAGE_NAME = "net.enelson.sopli.lib.corpse.";

    private CorpseServices() {
    }

    public static CorpseService resolve() {
        String version = Bukkit.getBukkitVersion();
        if (version == null) {
            Bukkit.getLogger().warning("[SopLib] Bukkit version is null. Using noop CorpseService.");
            return new CorpseServiceNoop();
        }

        String minecraftVersion = net.enelson.sopli.lib.version.VersionManager.normalize(version);
        String suffix = net.enelson.sopli.lib.version.ServerCompatibility.adapterSuffix(version);
        if (suffix != null) return tryLoad("CorpseService_" + suffix);

        Bukkit.getLogger().info("[SopLib] No CorpseService adapter for " + minecraftVersion + ", using noop.");
        return new CorpseServiceNoop();
    }

    private static CorpseService tryLoad(String simpleName) {
        String fqcn = PACKAGE_NAME + simpleName;
        try {
            Class<?> clazz = Class.forName(fqcn);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            CorpseService service = (CorpseService) instance;
            Bukkit.getLogger().info("[SopLib] Loaded CorpseService: " + fqcn);
            return service;
        } catch (Throwable throwable) {
            Bukkit.getLogger().severe("[SopLib] Failed to load CorpseService: " + fqcn);
            throwable.printStackTrace();
            return new CorpseServiceNoop();
        }
    }
}
