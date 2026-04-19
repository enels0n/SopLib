package net.enelson.sopli.lib.region;

import org.bukkit.Location;

public class RegionServiceNoop implements RegionService {

    @Override
    public boolean isInside(String worldName, String regionId, Location location) {
        return false;
    }

    @Override
    public Location getTeleportCenter(String worldName, String regionId) {
        return null;
    }

    @Override
    public String toString() {
        return "RegionServiceNoop";
    }
}
