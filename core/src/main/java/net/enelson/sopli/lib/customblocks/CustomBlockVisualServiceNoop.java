package net.enelson.sopli.lib.customblocks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class CustomBlockVisualServiceNoop implements CustomBlockVisualService {

    @Override
    public Entity createEntityWithoutBlock(Location location, ItemStack item, String id, CustomBlockVisualOptions options) {
        Bukkit.getLogger().severe("[SopLib] No CustomBlock visual adapter loaded. Cannot create block: " + id);
        return null;
    }

    @Override
    public void removeEntityWithoutBlock(Entity entity) {
    }

    @Override
    public void removeEntityWithoutBlock(UUID entityUuid) {
    }

    @Override
    public boolean isManagedEntity(Entity entity) {
        return false;
    }

    @Override
    public String toString() {
        return "CustomBlockVisualServiceNoop";
    }
}
