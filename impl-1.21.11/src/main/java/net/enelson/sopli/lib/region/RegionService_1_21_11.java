package net.enelson.sopli.lib.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionService_1_21_11 implements RegionService {

    @Override
    public boolean isInside(String worldName, String regionId, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        RegionManager regionManager = getRegionManager(worldName, location.getWorld());
        if (regionManager == null) {
            return false;
        }

        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region == null) {
            return false;
        }

        ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return set.getRegions().contains(region);
    }

    @Override
    public Location getTeleportCenter(String worldName, String regionId) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        RegionManager regionManager = getRegionManager(worldName, world);
        if (regionManager == null) {
            return null;
        }

        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region == null) {
            return null;
        }

        BlockVector3 minimum = region.getMinimumPoint();
        BlockVector3 maximum = region.getMaximumPoint();
        double x = (minimum.getBlockX() + maximum.getBlockX()) / 2.0D + 0.5D;
        double z = (minimum.getBlockZ() + maximum.getBlockZ()) / 2.0D + 0.5D;
        double y = (minimum.getBlockY() + maximum.getBlockY()) / 2.0D;

        return new Location(world, x, y, z, 0.0F, 0.0F);
    }

    private RegionManager getRegionManager(String expectedWorldName, World world) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !plugin.isEnabled() || world == null) {
            return null;
        }

        if (expectedWorldName != null && !expectedWorldName.isEmpty() && !world.getName().equalsIgnoreCase(expectedWorldName)) {
            return null;
        }

        try {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "RegionService_1_21_11";
    }
}
