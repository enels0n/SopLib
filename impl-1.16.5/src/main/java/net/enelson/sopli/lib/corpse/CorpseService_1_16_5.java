package net.enelson.sopli.lib.corpse;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class CorpseService_1_16_5 implements CorpseService {

    private static final String TAG = "soplib_corpse";

    @Override
    public CorpseHandle createCorpse(Location location, String corpseName, String skinOwnerName) {
        if (location == null || location.getWorld() == null) {
            return new CorpseServiceNoop().createCorpse(location, corpseName, skinOwnerName);
        }

        ArmorStand stand = location.getWorld().spawn(location.clone().add(0.5D, -1.1D, 0.5D), ArmorStand.class);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        stand.setVisible(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(false);
        stand.setMarker(false);
        stand.setCustomName(corpseName);
        stand.setCustomNameVisible(true);
        stand.addScoreboardTag(TAG);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null && skinOwnerName != null && !skinOwnerName.trim().isEmpty()) {
            meta.setOwner(skinOwnerName);
            head.setItemMeta(meta);
        }
        stand.setHelmet(head);

        final UUID entityUuid = stand.getUniqueId();
        return new CorpseHandle() {
            @Override
            public UUID getEntityUuid() {
                return entityUuid;
            }

            @Override
            public boolean isValid() {
                Entity entity = Bukkit.getEntity(entityUuid);
                return entity != null && entity.isValid();
            }

            @Override
            public void remove() {
                removeCorpse(entityUuid);
            }
        };
    }

    @Override
    public void removeCorpse(UUID entityUuid) {
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
        return "CorpseService_1_16_5";
    }
}
