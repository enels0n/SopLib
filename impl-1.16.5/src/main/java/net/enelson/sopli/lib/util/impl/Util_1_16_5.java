package net.enelson.sopli.lib.util.impl;

import net.enelson.sopli.lib.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Util_1_16_5 implements Util {

    @Override
    public void sendVersionedMessage(Player player, String message) {
        player.sendMessage(ChatColor.GREEN + "[1.16.5] " + message);
    }

    @Override
    public Location getDeserializedLocation(String s) {
        final String[] split = s.split(",");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
                Double.parseDouble(split[3]));
    }

    @Override
    public String getSerializedLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    @Override
    public String getCardinalDirection(Entity e) {
        double rotation = (e.getLocation().getYaw() - 90.0F) % 360.0F;

        if (rotation < 0.0D) {
            rotation += 360.0D;
        }
        if ((0.0D <= rotation) && (rotation < 45.0D))
            return "W";
        if ((45.0D <= rotation) && (rotation < 135.0D))
            return "N";
        if ((135.0D <= rotation) && (rotation < 225.0D))
            return "E";
        if ((225.0D <= rotation) && (rotation < 315.0D))
            return "S";
        if ((315.0D <= rotation) && (rotation < 360.0D)) {
            return "W";
        }
        return null;
    }

    @Override
    public List<String> translateColorList(List<String> list) {
        List<String> newList = new ArrayList<String>();
        for (String l : list) {
            newList.add(ChatColor.translateAlternateColorCodes('&', l));
        }
        return newList;
    }
}