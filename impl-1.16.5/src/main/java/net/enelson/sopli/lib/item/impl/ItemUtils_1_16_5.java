package net.enelson.sopli.lib.item.impl;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.external.ItemNBTUtils;
import net.enelson.sopli.lib.item.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemUtils_1_16_5 implements ItemUtils {
    private static final String CUSTOM_ITEM_KEY = "SopLibCustomItemKey";

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
    public ItemStack createItem(String material, Object model, String name, List<String> enchantments, List<String> lore,
                                List<String> nbts) {
        return createItem(material, 1, model, name, enchantments, lore, nbts);
    }

    @Override
    public void setCustomItemKey(ItemStack item, String key, String fallback) {
        if (item == null || item.getType() == Material.AIR || key == null || key.trim().isEmpty()) {
            return;
        }

        NBT.modify(item, (java.util.function.Consumer<de.tr7zw.nbtapi.iface.ReadWriteItemNBT>) nbt -> nbt.setString(CUSTOM_ITEM_KEY, key));
        if (fallback != null && !fallback.trim().isEmpty()) {
            setName(item, fallback);
        }
    }

    @Override
    public String getCustomItemKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "";
        }
        String key = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, String>) nbt -> nbt.getString(CUSTOM_ITEM_KEY));
        return key == null ? "" : key;
    }

    @Override
    public <T> T getNBT(ItemStack item, String key, Class<T> typeClass) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        NBTType nbtType = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, NBTType>) nbt -> (NBTType) nbt.getType(key));
        if (nbtType == null) {
            return null;
        }

        Object value;
        switch (nbtType) {
            case NBTTagByte:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, Byte>) nbt -> (byte) nbt.getByte(key));
                break;
            case NBTTagShort:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, Short>) nbt -> (short) nbt.getShort(key));
                break;
            case NBTTagInt:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, Integer>) nbt -> (int) nbt.getInteger(key));
                break;
            case NBTTagLong:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, Long>) nbt -> (long) nbt.getLong(key));
                break;
            case NBTTagFloat:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, Float>) nbt -> (float) nbt.getFloat(key));
                break;
            case NBTTagDouble:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, Double>) nbt -> (double) nbt.getDouble(key));
                break;
            case NBTTagString:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, String>) nbt -> nbt.getString(key));
                break;
            case NBTTagByteArray:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, byte[]>) nbt -> nbt.getByteArray(key));
                break;
            case NBTTagIntArray:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, int[]>) nbt -> nbt.getIntArray(key));
                break;
            case NBTTagLongArray:
                value = NBT.get(item, (java.util.function.Function<de.tr7zw.nbtapi.iface.ReadableItemNBT, long[]>) nbt -> nbt.getLongArray(key));
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
    public Map<String, Integer> getEnchants(ItemStack item) {
        Map<String, Integer> enchants = new HashMap<String, Integer>();
        if (item == null || item.getType() == Material.AIR) {
            return enchants;
        }

        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            enchants.put(entry.getKey().getKey().getKey(), entry.getValue());
        }
        return enchants;
    }

    @Override
    public ItemStack getHead(String value, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        NBT.modify(head, nbt -> {
            ReadWriteNBT skullOwner = nbt.getOrCreateCompound("SkullOwner");
            skullOwner.setUUID("Id", UUID.randomUUID());
            skullOwner.getOrCreateCompound("Properties").getCompoundList("textures").addCompound().setString("Value", value);
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
    public ItemStack getHeadTexture(String texture, String name) {
        if (texture == null || texture.trim().isEmpty()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (name != null && !name.trim().isEmpty()) {
                setName(head, name);
            }
            return head;
        }

        String value = texture.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return getHeadURL(value, name);
        }
        if (value.startsWith("eyJ")) {
            return getHead(value, name);
        }
        int index = value.lastIndexOf("/texture/");
        if (index >= 0) {
            value = value.substring(index + "/texture/".length());
        }
        return getHeadURL("https://textures.minecraft.net/texture/" + value, name);
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

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
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

            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
            if (enchantment != null) {
                meta.addEnchant(enchantment, level, true);
            }
        }

        item.setItemMeta(meta);
    }
}