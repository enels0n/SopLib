package net.enelson.sopli.lib.external;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBT;

public class ItemNBTUtils {
	public static void setTags(ItemStack item, List<String> tags) {
		NBT.modify(item, nbt -> {
			for(String t : tags) {
				nbt.setString(t.split("::")[0], t.split("::")[1]);
			}
		});
	}
}
