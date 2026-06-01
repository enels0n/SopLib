package net.enelson.sopli.lib.corpse;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

public class CorpseService_1_21_11 implements CorpseService {

    private static final String TAG = "soplib_corpse";

    @Override
    public CorpseHandle createCorpse(Location location, String corpseName, String skinOwnerName) {
        if (location == null || location.getWorld() == null) {
            return new CorpseServiceNoop().createCorpse(location, corpseName, skinOwnerName);
        }

        ArmorStand stand = location.getWorld().spawn(location.clone().add(0.5D, -1.45D, 0.5D), ArmorStand.class);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        stand.setVisible(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(false);
        stand.setMarker(false);
        stand.setCanPickupItems(false);
        stand.setCollidable(false);
        stand.setCustomName(corpseName);
        stand.setCustomNameVisible(true);
        stand.setHeadPose(new EulerAngle(Math.toRadians(18), 0, 0));
        stand.setRightArmPose(new EulerAngle(Math.toRadians(272), 0, Math.toRadians(12)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(272), 0, Math.toRadians(-12)));
        stand.addScoreboardTag(TAG);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null && skinOwnerName != null && !skinOwnerName.trim().isEmpty()) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(skinOwnerName));
            head.setItemMeta(meta);
        }
        stand.setHelmet(head);
        stand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        stand.setChestplate(createArmorPiece(Material.LEATHER_CHESTPLATE));
        stand.setLeggings(createArmorPiece(Material.LEATHER_LEGGINGS));
        stand.setBoots(createArmorPiece(Material.LEATHER_BOOTS));

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
        return "CorpseService_1_21_11";
    }

    private ItemStack createArmorPiece(Material material) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(44, 34, 30));
            item.setItemMeta(meta);
        }
        return item;
    }
}
