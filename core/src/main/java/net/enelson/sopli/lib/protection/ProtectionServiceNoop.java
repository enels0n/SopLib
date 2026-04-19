package net.enelson.sopli.lib.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionServiceNoop implements ProtectionService {

    @Override
    public boolean canBuild(Player player, Location location) {
        return true;
    }

    @Override
    public boolean canPlaceCustomBlock(Player player, Location location) {
        return true;
    }

    @Override
    public boolean canBreakCustomBlock(Player player, Location location) {
        return true;
    }

    @Override
    public boolean isRegionMemberOrOwner(Player player, Location location) {
        return true;
    }

    @Override
    public String toString() {
        return "ProtectionServiceNoop";
    }
}
