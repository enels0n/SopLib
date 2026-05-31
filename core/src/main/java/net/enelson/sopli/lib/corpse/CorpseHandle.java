package net.enelson.sopli.lib.corpse;

import java.util.UUID;

public interface CorpseHandle {

    UUID getEntityUuid();

    boolean isValid();

    void remove();
}
