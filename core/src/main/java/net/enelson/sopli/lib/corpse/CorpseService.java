package net.enelson.sopli.lib.corpse;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface CorpseService {

    CorpseHandle createCorpse(Location location, String corpseName, String skinOwnerName);

    void removeCorpse(UUID entityUuid);

    boolean isManagedEntity(Entity entity);
}
