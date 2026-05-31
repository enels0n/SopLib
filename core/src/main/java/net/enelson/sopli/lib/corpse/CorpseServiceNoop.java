package net.enelson.sopli.lib.corpse;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CorpseServiceNoop implements CorpseService {

    @Override
    public CorpseHandle createCorpse(Location location, String corpseName, String skinOwnerName) {
        return new CorpseHandle() {
            @Override
            public UUID getEntityUuid() {
                return null;
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public void removeCorpse(UUID entityUuid) {
    }

    @Override
    public boolean isManagedEntity(Entity entity) {
        return false;
    }
}
