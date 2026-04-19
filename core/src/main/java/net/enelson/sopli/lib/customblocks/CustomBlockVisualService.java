package net.enelson.sopli.lib.customblocks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public interface CustomBlockVisualService {

    Entity createEntityWithoutBlock(Location location, ItemStack item, String id, CustomBlockVisualOptions options);

    void removeEntityWithoutBlock(Entity entity);

    void removeEntityWithoutBlock(UUID entityUuid);

    boolean isManagedEntity(Entity entity);
}
