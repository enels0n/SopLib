package net.enelson.sopli.lib.customblocks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class CustomBlockVisualService_1_16_5 implements CustomBlockVisualService {

    private static final String TAG = "soplib_customblock";

    @Override
    public Entity createEntityWithoutBlock(Location location, ItemStack item, String id, CustomBlockVisualOptions options) {
        if (location == null || location.getWorld() == null || item == null || options == null) {
            return null;
        }

        Location spawn = location.clone().add(options.getOffsetX(), options.getOffsetY(), options.getOffsetZ());

        ArmorStand stand = location.getWorld().spawn(spawn, ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setSmall(true);
        stand.setMarker(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setHelmet(item.clone());
        
        float yaw = -options.getYaw();
        float pitch = options.isUsePitch() ? options.getPitch() : 0.0f;

        stand.setRotation(yaw, pitch);
        stand.setHeadPose(new EulerAngle(
                Math.toRadians(pitch),
                Math.toRadians(yaw),
                0.0d
        ));
        stand.addScoreboardTag(TAG);
        if (id != null && !id.isEmpty()) {
            stand.addScoreboardTag("soplib_customblock_id:" + id);
        }
        return stand;
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

    @Override
    public String toString() {
        return "CustomBlockVisualService_1_16_5";
    }
}
