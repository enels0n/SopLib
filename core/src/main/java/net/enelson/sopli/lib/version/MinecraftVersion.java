package net.enelson.sopli.lib.version;

import java.util.Arrays;
import java.util.Objects;

/** Numeric Minecraft version, independent of Paper build numbers. */
public final class MinecraftVersion implements Comparable<MinecraftVersion> {
    private final String value;
    private final int[] parts;

    private MinecraftVersion(String value, int[] parts) {
        this.value = value;
        this.parts = parts;
    }

    public static MinecraftVersion parse(String raw) {
        String value = VersionManager.normalize(raw);
        if (!value.matches("\\d+\\.\\d+(?:\\.\\d+)*")) return unknown();
        String[] tokens = value.split("\\.");
        int[] parts = new int[tokens.length];
        try {
            for (int i = 0; i < tokens.length; i++) parts[i] = Integer.parseInt(tokens[i]);
        } catch (NumberFormatException invalid) {
            return unknown();
        }
        int length = parts.length;
        while (length > 2 && parts[length - 1] == 0) length--;
        parts = Arrays.copyOf(parts, length);
        return new MinecraftVersion(value, parts);
    }

    private static MinecraftVersion unknown() { return new MinecraftVersion("unknown", new int[0]); }
    public boolean isKnown() { return parts.length >= 2; }
    public int major() { return isKnown() ? parts[0] : -1; }
    public int minor() { return isKnown() ? parts[1] : -1; }
    public boolean isRelease(int major, int minor) { return major() == major && minor() == minor; }
    public boolean isAtLeast(String other) {
        MinecraftVersion target = parse(other);
        return isKnown() && target.isKnown() && compareTo(target) >= 0;
    }

    @Override public int compareTo(MinecraftVersion other) {
        Objects.requireNonNull(other, "other");
        if (!isKnown() || !other.isKnown()) throw new IllegalStateException("Cannot compare an unknown Minecraft version");
        for (int i = 0; i < Math.max(parts.length, other.parts.length); i++) {
            int comparison = Integer.compare(i < parts.length ? parts[i] : 0, i < other.parts.length ? other.parts[i] : 0);
            if (comparison != 0) return comparison;
        }
        return 0;
    }

    @Override public boolean equals(Object other) {
        return other instanceof MinecraftVersion && Arrays.equals(parts, ((MinecraftVersion) other).parts);
    }
    @Override public int hashCode() { return Arrays.hashCode(parts); }
    @Override public String toString() { return value; }
}
