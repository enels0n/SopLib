package net.enelson.sopli.lib.customblocks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CustomBlockVisualService_1_20_4 implements CustomBlockVisualService {

    private static final String TAG = "soplib_customblock";

    @Override
    public Entity createEntityWithoutBlock(Location location, ItemStack item, String id, CustomBlockVisualOptions options) {
        if (location == null || location.getWorld() == null || item == null || options == null) {
            return null;
        }

        ItemDisplay display = location.getWorld().spawn(location, ItemDisplay.class);

        display.setPersistent(true);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setSilent(true);
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setItemStack(item.clone());
        display.setTransformation(createTransformation(options));

        display.addScoreboardTag(TAG);
        if (id != null && !id.isEmpty()) {
            display.addScoreboardTag("soplib_customblock_id:" + id);
        }

        return display;
    }

    @Override
    public void removeEntityWithoutBlock(Entity entity) {
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
    }

    @Override
    public void removeEntityWithoutBlock(UUID entityUuid) {
        if (entityUuid == null) {
            return;
        }

        Entity entity = Bukkit.getEntity(entityUuid);
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public boolean isManagedEntity(Entity entity) {
        return entity != null && entity.getScoreboardTags().contains(TAG);
    }

    private Transformation createTransformation(CustomBlockVisualOptions options) {
        Quaternionf rotation = new Quaternionf()
                .rotateY((float) Math.toRadians(-options.getYaw()));

        if (options.isUsePitch()) {
            rotation.rotateX((float) Math.toRadians(options.getPitch()));
        }

        return new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                rotation,
                new Vector3f(options.getScaleX(), options.getScaleY(), options.getScaleZ()),
                new Quaternionf()
        );
    }

    @Override
    public String toString() {
        return "CustomBlockVisualService_1_20_4";
    }
}
