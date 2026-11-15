package net.enelson.sopli.lib.customblocks;

import org.bukkit.Bukkit;

public final class CustomBlockVisualServices {

    private static final String PACKAGE_NAME = "net.enelson.sopli.lib.customblocks.";

    private CustomBlockVisualServices() {
    }

    public static CustomBlockVisualService resolve() {
        String version = Bukkit.getBukkitVersion();
        if (version == null) {
            Bukkit.getLogger().severe("[SopLib] Bukkit version is null. Using noop CustomBlock visual service.");
            return new CustomBlockVisualServiceNoop();
        }

        String minecraftVersion = net.enelson.sopli.lib.version.VersionManager.normalize(version);
        String suffix = net.enelson.sopli.lib.version.ServerCompatibility.adapterSuffix(version);
        if (suffix != null) return tryLoad("CustomBlockVisualService_" + suffix);

        Bukkit.getLogger().severe("[SopLib] Unsupported CustomBlock visual service version: " + minecraftVersion);
        return new CustomBlockVisualServiceNoop();
    }

    private static CustomBlockVisualService tryLoad(String simpleName) {
        String fqcn = PACKAGE_NAME + simpleName;
        try {
            Class<?> clazz = Class.forName(fqcn);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            CustomBlockVisualService service = (CustomBlockVisualService) instance;
            Bukkit.getLogger().info("[SopLib] Loaded CustomBlock visual service: " + fqcn);
            return service;
        } catch (Throwable throwable) {
            Bukkit.getLogger().severe("[SopLib] Failed to load CustomBlock visual service: " + fqcn);
            throwable.printStackTrace();
            return new CustomBlockVisualServiceNoop();
        }
    }
}
