package net.enelson.sopli.lib.item.impl;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.external.ItemNBTUtils;
import net.enelson.sopli.lib.item.ItemUtils;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemUtils_1_20_4 implements ItemUtils {

	@Override
	public ItemStack createItem(String material, int amount, Object model, String name, List<String> enchantments,
			List<String> lore, List<String> nbts) {

		Material mat = Material.matchMaterial(material);
		if (mat == null) {
			throw new IllegalArgumentException("Unknown material: " + material);
		}

		if (amount <= 0) {
			amount = 1;
		}

		ItemStack item = new ItemStack(mat, amount);

		if (model != null) {
			setCustomModelData(item, model);
		}

		if (name != null && !name.trim().isEmpty()) {
			setName(item, name);
		}

		if (enchantments != null && !enchantments.isEmpty()) {
			setEnchantments(item, enchantments);
		}

		if (lore != null && !lore.isEmpty()) {
			setLore(item, lore);
		}

		if (nbts != null && !nbts.isEmpty()) {
			ItemNBTUtils.setTags(item, nbts);
		}

		return item;
	}

	@Override
	public ItemStack createItem(String material, Object model, String name, List<String> enchantments,
			List<String> lore, List<String> nbts) {
		return createItem(material, 1, model, name, enchantments, lore, nbts);
	}

	@Override
	public Map<String, Integer> getEnchants(ItemStack item) {
		Map<String, Integer> enchants = new HashMap<String, Integer>();
		if (item == null || item.getType() == Material.AIR) {
			return enchants;
		}

		for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
			NamespacedKey key = entry.getKey().getKey();
			enchants.put(key.getKey(), entry.getValue());
		}

		return enchants;
	}

	@Override
	public void setCustomItemKey(ItemStack item, String key, String fallback) {
		if (item == null || item.getType() == Material.AIR || key == null || key.trim().isEmpty()) {
			return;
		}

		String safeKey = key.replace("\"", "\\\"");
		String safeFallback = fallback == null ? "" : fallback.replace("\"", "\\\"");

		ReadWriteNBT nbt = NBT.parseNBT(
				"{\"minecraft:item_name\":{\"translate\":\"" + safeKey + "\",\"fallback\":\"" + safeFallback + "\"}}");

		NBT.modifyComponents(item, data -> {
			data.mergeCompound(nbt);
		});
	}

	@Override
	public String getCustomItemKey(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return "";
		}

		ReadableNBT root = NBT.itemStackToNBT(item);
		if (root == null) {
			return "";
		}

		ReadableNBT components = root.getCompound("components");
		if (components == null) {
			return "";
		}

		ReadableNBT itemName = components.getCompound("minecraft:item_name");
		return itemName != null ? itemName.getOrDefault("translate", "") : "";
	}

	@Override
	public void setCustomModelData(ItemStack item, Object... model) {
		if (item == null || item.getType() == Material.AIR || model == null || model.length == 0 || model[0] == null) {
			return;
		}

		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return;
		}

		Object raw = model[0];
		int value;

		if (raw instanceof Number) {
			value = ((Number) raw).intValue();
		} else {
			value = Integer.parseInt(raw.toString());
		}

		meta.setCustomModelData(value);
		item.setItemMeta(meta);
	}

	@Override
	public <T> T getNBT(ItemStack item, String key, Class<T> typeClass) {
		if (item == null || item.getType() == Material.AIR) {
			return null;
		}
		NBTType nbtType = NBT.get(item, nbt -> (NBTType) nbt.getType(key));
		if (nbtType == null) {
			return null;
		}
		Object value = null;
		switch (nbtType) {
		case NBTTagByte:
			value = NBT.get(item, nbt -> (byte) nbt.getByte(key));
			break;
		case NBTTagShort:
			value = NBT.get(item, nbt -> (short) nbt.getShort(key));
			break;
		case NBTTagInt:
			value = NBT.get(item, nbt -> (int) nbt.getInteger(key));
			break;
		case NBTTagLong:
			value = NBT.get(item, nbt -> (long) nbt.getLong(key));
			break;
		case NBTTagFloat:
			value = NBT.get(item, nbt -> (float) nbt.getFloat(key));
			break;
		case NBTTagDouble:
			value = NBT.get(item, nbt -> (double) nbt.getDouble(key));
			break;
		case NBTTagString:
			value = NBT.get(item, nbt -> (String) nbt.getString(key));
			break;
		case NBTTagByteArray:
			value = NBT.get(item, nbt -> (byte[]) nbt.getByteArray(key));
			break;
		case NBTTagIntArray:
			value = NBT.get(item, nbt -> (int[]) nbt.getIntArray(key));
			break;
		case NBTTagLongArray:
			value = NBT.get(item, nbt -> (long[]) nbt.getLongArray(key));
			break;
		default:
			value = null;
			break;
		}
		if (value == null) {
			return null;
		}
		return typeClass.cast(value);
	}

	@Override
	public ItemStack getHead(String value, String name) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);

		NBT.modify(head, nbt -> {
			ReadWriteNBT skullOwner = nbt.getOrCreateCompound("SkullOwner");
			skullOwner.setUUID("Id", UUID.randomUUID());

			skullOwner.getOrCreateCompound("Properties").getCompoundList("textures").addCompound().setString("Value",
					value);
		});

		if (name != null && !name.trim().isEmpty()) {
			setName(head, name);
		}

		return head;
	}

	@Override
	public ItemStack getHeadURL(String url, String name) {
		String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
		String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
		return getHead(base64, name);
	}

	@Override
	public void setLore(ItemStack item, List<String> lore) {
		if (item == null || item.getType() == Material.AIR) {
			return;
		}

		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return;
		}

		if (lore == null || lore.isEmpty()) {
			meta.setLore(null);
			item.setItemMeta(meta);
			return;
		}

		meta.setLore(SopLib.getInstance().getUtil().translateColorList(lore));
		item.setItemMeta(meta);
	}

	@Override
	public void setName(ItemStack item, String name) {
		if (item == null || item.getType() == Material.AIR || name == null) {
			return;
		}

		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return;
		}

		meta.setDisplayName(translateColor(name));
		item.setItemMeta(meta);
	}

	@Override
	public void setEnchantments(ItemStack item, List<String> enchantments) {
		if (item == null || item.getType() == Material.AIR || enchantments == null || enchantments.isEmpty()) {
			return;
		}

		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return;
		}

		for (String raw : enchantments) {
			if (raw == null || raw.trim().isEmpty()) {
				continue;
			}

			String[] split = raw.split("[:; ]+", 2);
			String enchantName = split[0].trim().toLowerCase();
			int level = 1;

			if (split.length > 1) {
				try {
					level = Integer.parseInt(split[1].trim());
				} catch (NumberFormatException ignored) {
					level = 1;
				}
			}

			Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantName));
			if (enchantment != null) {
				meta.addEnchant(enchantment, level, true);
			}
		}

		item.setItemMeta(meta);
	}

	private String translateColor(String value) {
		return ChatColor.translateAlternateColorCodes('&', value);
	}
}