package net.enelson.sopli.lib.corpse;

import java.util.UUID;

public interface CorpseHandle {

    UUID getEntityUuid();

    default UUID getInteractionEntityUuid() {
        return getEntityUuid();
    }

    default int getVisualEntityId() {
        return -1;
    }

    boolean isValid();

    void remove();
}
