package net.enelson.sopli.lib.protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProtectionService_1_21_11 implements ProtectionService {

    @Override
    public boolean canBuild(Player player, Location location) {
        if (player == null || location == null || location.getWorld() == null) {
            return true;
        }

        Plugin wgPlugin = player.getServer().getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
            return true;
        }

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location weLocation = BukkitAdapter.adapt(location);
        World weWorld = BukkitAdapter.adapt(location.getWorld());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, weWorld)) {
            return true;
        }

        return query.testState(weLocation, localPlayer, Flags.BUILD);
    }

    @Override
    public boolean canPlaceCustomBlock(Player player, Location location) {
        return canBuild(player, location);
    }

    @Override
    public boolean canBreakCustomBlock(Player player, Location location) {
        return canBuild(player, location);
    }

    @Override
    public boolean isRegionMemberOrOwner(Player player, Location location) {
        if (player == null || location == null || location.getWorld() == null) {
            return true;
        }

        Plugin wgPlugin = player.getServer().getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
            return true;
        }

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) {
            return true;
        }

        ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        if (set.size() == 0) {
            return true;
        }

        List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>(set.getRegions());
        Collections.sort(regions, new Comparator<ProtectedRegion>() {
            @Override
            public int compare(ProtectedRegion first, ProtectedRegion second) {
                return Integer.compare(second.getPriority(), first.getPriority());
            }
        });

        ProtectedRegion region = regions.get(0);
        return region.isOwner(localPlayer) || region.isMember(localPlayer);
    }

    @Override
    public String toString() {
        return "ProtectionService_1_21_11";
    }
}
