package net.enelson.sopli.lib.compat;

import org.bukkit.Particle;

public final class Particles {
    private Particles() {}

    public static Particle dust() { return resolve("DUST", "REDSTONE"); }

    public static Particle resolve(String preferred, String... aliases) {
        for (String name : combine(preferred, aliases)) {
            try { return Particle.valueOf(name.toUpperCase(java.util.Locale.ROOT)); }
            catch (IllegalArgumentException ignored) { }
        }
        throw new IllegalStateException("Particle unavailable on this server: " + preferred
                + "; aliases=" + java.util.Arrays.toString(aliases));
    }

    private static String[] combine(String preferred, String[] aliases) {
        String[] names = new String[aliases.length + 1];
        names[0] = preferred;
        System.arraycopy(aliases, 0, names, 1, aliases.length);
        return names;
    }
}
