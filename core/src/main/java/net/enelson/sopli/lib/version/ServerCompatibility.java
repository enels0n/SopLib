package net.enelson.sopli.lib.version;

import org.bukkit.Bukkit;

/** Shared adapter policy. Existence of an adapter is not an in-game certification. */
public final class ServerCompatibility {
    private ServerCompatibility() {}

    public static MinecraftVersion current() { return MinecraftVersion.parse(Bukkit.getBukkitVersion()); }

    public static String adapterSuffix(String raw) {
        MinecraftVersion version = MinecraftVersion.parse(raw);
        for (String legacy : new String[] { "1.16.5", "1.20.4", "1.21.1", "1.21.11" }) {
            if (version.equals(MinecraftVersion.parse(legacy))) return legacy.replace('.', '_');
        }
        if (version.major() == 26 && version.minor() >= 1 && version.minor() <= 3) {
            return "26_" + version.minor();
        }
        return null;
    }

    public static boolean hasAdapter(String raw) { return adapterSuffix(raw) != null; }

    /** Returns -1 rather than guessing for versions outside the maintained policy. */
    public static int minimumJava(String raw) {
        String adapter = adapterSuffix(raw);
        if (adapter == null) return -1;
        if (adapter.startsWith("26_")) return 25;
        if (adapter.startsWith("1_21_")) return 21;
        if (adapter.equals("1_20_4")) return 17;
        return 8;
    }
}
