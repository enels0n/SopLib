package net.enelson.sopli.lib;

import net.enelson.sopli.lib.version.VersionManager;
import net.enelson.sopli.lib.version.ServerCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

final class DoctorCommand {
    private DoctorCommand() {}

    static void report(CommandSender sender) {
        String version = VersionManager.normalize(Bukkit.getBukkitVersion());
        sender.sendMessage("[SopLib doctor] Minecraft=" + version + "; Bukkit=" + Bukkit.getBukkitVersion());
        sender.sendMessage("Java=" + System.getProperty("java.version") + "; server=" + Bukkit.getVersion());
        boolean supported = ServerCompatibility.hasAdapter(version);
        sender.sendMessage((supported ? "[OK]" : "[WARN]") + " Adapter release family: " + version);
        int required = ServerCompatibility.minimumJava(version);
        String java = System.getProperty("java.specification.version");
        int actual = Integer.parseInt(java.startsWith("1.") ? java.substring(2) : java);
        sender.sendMessage(required < 0 ? "[WARN] Java minimum is unknown for this release"
                : (actual >= required ? "[OK]" : "[FAIL]") + " Java minimum=" + required);
        SopLib lib = SopLib.getInstance();
        if (lib == null) {
            sender.sendMessage("[FAIL] SopLib is not initialized");
            return;
        }
        adapter(sender, "Util", lib.getUtil());
        adapter(sender, "ItemUtils", lib.getItemUtils());
        adapter(sender, "Corpse", lib.getCorpseService());
        adapter(sender, "CustomBlocks", lib.getCustomBlockVisualService());
        adapter(sender, "Protection", lib.getProtectionService());
        adapter(sender, "Region", lib.getRegionService());
        for (String name : new String[] { "NBTAPI", "WorldGuard", "ProtocolLib", "ViaVersion" }) {
            Plugin dependency = Bukkit.getPluginManager().getPlugin(name);
            sender.sendMessage(dependency == null ? (name.equals("NBTAPI") ? "[WARN] Item NBT operations require missing " : "[INFO] Optional dependency absent: ") + name
                    : (dependency.isEnabled() ? "[OK] " : "[WARN] Disabled: ") + name + " " + dependency.getDescription().getVersion());
        }
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!plugin.getName().startsWith("Sop")) continue;
            sender.sendMessage((plugin.isEnabled() ? "[OK] " : "[WARN] Disabled: ") + plugin.getName()
                    + " " + plugin.getDescription().getVersion());
        }
        sender.sendMessage("[INFO] Loaded adapters do not prove in-game compatibility; test packets and integrations with a player.");
        sender.sendMessage("[SopLib doctor] Complete");
    }

    private static void adapter(CommandSender sender, String name, Object adapter) {
        String type = adapter == null ? "null" : adapter.getClass().getSimpleName();
        sender.sendMessage((adapter == null || type.toLowerCase(java.util.Locale.ROOT).contains("noop")
                ? "[WARN] " : "[OK] ") + name + "=" + type);
    }
}
