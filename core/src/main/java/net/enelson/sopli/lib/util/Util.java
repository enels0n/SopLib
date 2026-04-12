package net.enelson.sopli.lib.util;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface Util {
    void sendVersionedMessage(Player player, String message);
	Location getDeserializedLocation(String s);
	String getSerializedLocation(Location loc);
	String getCardinalDirection(Entity e);
	List<String> translateColorList(List<String> list);
}
