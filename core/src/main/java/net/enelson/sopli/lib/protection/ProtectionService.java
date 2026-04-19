package net.enelson.sopli.lib.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ProtectionService {

    boolean canBuild(Player player, Location location);

    boolean canPlaceCustomBlock(Player player, Location location);

    boolean canBreakCustomBlock(Player player, Location location);

    boolean isRegionMemberOrOwner(Player player, Location location);
}
