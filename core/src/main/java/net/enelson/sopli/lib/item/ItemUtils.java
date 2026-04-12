package net.enelson.sopli.lib.item;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface ItemUtils {
    ItemStack createItem(String material, int amount, Object model, String name, List<String> enchantments, List<String> lore, List<String> nbts);
    ItemStack createItem(String material, Object model, String name, List<String> enchantments, List<String> lore, List<String> nbts);
    void setCustomItemKey(ItemStack item, String key, String fallback);
    String getCustomItemKey(ItemStack item);
	<T> T getNBT(ItemStack item, String key, Class<T> typeClass);
    
    Map<String, Integer> getEnchants(ItemStack item);
	ItemStack getHead(String value, String name);
	ItemStack getHeadURL(String url, String name);
	void setLore(ItemStack item, List<String> lore);
	void setName(ItemStack item, String name);
	void setCustomModelData(ItemStack item, Object... model);
	void setEnchantments(ItemStack item, List<String> enchantments);
}
