package net.enelson.sopli.lib.region;

import org.bukkit.Location;

public interface RegionService {

    boolean isInside(String worldName, String regionId, Location location);

    Location getTeleportCenter(String worldName, String regionId);
}
