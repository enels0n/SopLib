// shade, Java 21
package net.enelson.sopli.lib;

import net.enelson.sopli.lib.external.ItemNBTUtils;
import net.enelson.sopli.lib.item.ItemUtils;
import net.enelson.sopli.lib.util.Util;
import net.enelson.sopli.lib.version.VersionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SopLibPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        String detectedVersion = getMinecraftVersion();

        Util util = VersionManager.loadForVersion(Util.class, detectedVersion);
        ItemUtils itemUtils = VersionManager.loadForVersion(ItemUtils.class, detectedVersion);
        ItemNBTUtils itemNbtUtils = VersionManager.loadForVersion(ItemNBTUtils.class, detectedVersion);

        if (util == null || itemUtils == null) {
            getLogger().severe("========================================");
            getLogger().severe(" Unsupported Minecraft version: " + detectedVersion);
            getLogger().severe(" SopLib will be disabled.");
            getLogger().severe("========================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // ВАЖНО: регистрируем инстанс для всех других плагинов
        getLogger().info("[SopLib] SopLib class loader: " + SopLib.class.getClassLoader());
        SopLib.setInstance(new SopLib(util, itemUtils, itemNbtUtils));
        getLogger().info("[SopLib] SopLib.getInstance() after set -> " + SopLib.getInstance());
    }

    @Override
    public void onDisable() {
        SopLib instance = SopLib.getInstance();
        if (instance != null) {
            instance.shutdown();
            SopLib.setInstance(null);
        }
    }

    private String getMinecraftVersion() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        if (bukkitVersion == null || bukkitVersion.isEmpty()) {
            return "unknown";
        }
        return bukkitVersion.split("-")[0];
    }
}
