package net.enelson.sopli.lib.corpse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public abstract class AbstractModernPlayerCorpseService implements CorpseService {

    private static final String TAG = "soplib_corpse_anchor";

    private final Map<UUID, VisualCorpse> corpses = new ConcurrentHashMap<UUID, VisualCorpse>();

    @Override
    public CorpseHandle createCorpse(Location location, String corpseName, String skinOwnerName) {
        if (location == null || location.getWorld() == null) {
            return new CorpseServiceNoop().createCorpse(location, corpseName, skinOwnerName);
        }

        ArmorStand anchor = location.getWorld().spawn(location.clone().add(0.5D, -0.95D, 0.5D), ArmorStand.class);
        anchor.setGravity(false);
        anchor.setInvulnerable(true);
        anchor.setSilent(true);
        anchor.setVisible(false);
        anchor.setBasePlate(false);
        anchor.setArms(false);
        anchor.setSmall(false);
        anchor.setMarker(false);
        anchor.setCustomName(null);
        anchor.setCustomNameVisible(false);
        anchor.addScoreboardTag(TAG);
        try {
            anchor.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        } catch (Throwable ignored) {
        }

        UUID anchorUuid = anchor.getUniqueId();
        try {
            VisualCorpse corpse = createVisualCorpse(location, corpseName, skinOwnerName);
            corpses.put(anchorUuid, corpse);
            broadcastSpawn(corpse);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return new CorpseHandle() {
            @Override
            public UUID getEntityUuid() {
                return anchorUuid;
            }

            @Override
            public UUID getInteractionEntityUuid() {
                return anchorUuid;
            }

            @Override
            public int getVisualEntityId() {
                VisualCorpse corpse = corpses.get(anchorUuid);
                return corpse != null ? corpse.entityId : -1;
            }

            @Override
            public boolean isValid() {
                Entity entity = Bukkit.getEntity(anchorUuid);
                return entity != null && entity.isValid();
            }

            @Override
            public void remove() {
                removeCorpse(anchorUuid);
            }
        };
    }

    @Override
    public void removeCorpse(UUID entityUuid) {
        if (entityUuid == null) {
            return;
        }

        Entity anchor = Bukkit.getEntity(entityUuid);
        if (anchor != null) {
            anchor.remove();
        }

        VisualCorpse corpse = corpses.remove(entityUuid);
        if (corpse == null) {
            return;
        }

        try {
            Object destroyPacket = createDestroyPacket(corpse.entityId);
            Object removeInfoPacket = createPlayerInfoRemovePacket(Collections.singletonList(corpse.profileUuid));
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                sendPacket(viewer, destroyPacket);
                if (corpse.teamRemovePacket != null) {
                    sendPacket(viewer, corpse.teamRemovePacket);
                }
                if (removeInfoPacket != null) {
                    sendPacket(viewer, removeInfoPacket);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public boolean isManagedEntity(Entity entity) {
        return entity != null && entity.getScoreboardTags().contains(TAG);
    }

    protected abstract String getImplementationName();

    private VisualCorpse createVisualCorpse(Location location, String corpseName, String skinOwnerName) throws Exception {
        Object minecraftServer = getMinecraftServer();
        Object serverLevel = getWorldHandle(location);
        Object gameProfile = createGameProfile(corpseName, skinOwnerName);
        Object clientInformation = createClientInformation();
        Object serverPlayer = createServerPlayer(minecraftServer, serverLevel, gameProfile, clientInformation);

        UUID profileUuid = (UUID) invokeMethod(gameProfile, "getId");
        String profileName = (String) invokeMethod(gameProfile, "getName");

        moveEntity(serverPlayer, location.clone().add(0.5D, 0.15D, 0.5D), 0.0F, 0.0F);
        configureCorpsePose(serverPlayer, location);

        int entityId = ((Number) invokeMethod(serverPlayer, "getId")).intValue();
        UUID entityUuid = (UUID) invokeMethod(serverPlayer, "getUUID");

        Object playerInfoPacket = createPlayerInfoAddPacket(serverPlayer);
        Object spawnPacket = createSpawnPacket(serverPlayer, location);
        Object metadataPacket = createMetadataPacket(entityId, serverPlayer);
        Object removeInfoPacket = createPlayerInfoRemovePacket(Collections.singletonList(profileUuid));

        return new VisualCorpse(entityId, entityUuid, profileUuid, profileName, playerInfoPacket, spawnPacket, metadataPacket, removeInfoPacket, null, null);
    }

    private void broadcastSpawn(final VisualCorpse corpse) throws Exception {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            sendPacket(viewer, corpse.playerInfoPacket);
            if (corpse.teamCreatePacket != null) {
                sendPacket(viewer, corpse.teamCreatePacket);
            }
            sendPacket(viewer, corpse.spawnPacket);
            sendPacket(viewer, corpse.metadataPacket);
        }

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("SopLib"), new Runnable() {
            @Override
            public void run() {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    try {
                        sendPacket(viewer, corpse.removeInfoPacket);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }, 20L);
    }

    private Object getMinecraftServer() throws Exception {
        Method getServer = Bukkit.getServer().getClass().getMethod("getServer");
        return getServer.invoke(Bukkit.getServer());
    }

    private Object getWorldHandle(Location location) throws Exception {
        Method getHandle = location.getWorld().getClass().getMethod("getHandle");
        return getHandle.invoke(location.getWorld());
    }

    private Object createGameProfile(String corpseName, String skinOwnerName) throws Exception {
        Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        Constructor<?> constructor = gameProfileClass.getConstructor(UUID.class, String.class);
        String name = buildCorpseProfileName(corpseName, skinOwnerName);
        Object profile = constructor.newInstance(UUID.randomUUID(), name);

        Player onlinePlayer = skinOwnerName != null && !skinOwnerName.trim().isEmpty() ? Bukkit.getPlayerExact(skinOwnerName) : null;
        if (onlinePlayer != null) {
            Object sourceProfile = extractProfile(onlinePlayer);
            if (sourceProfile != null) {
                copyProfileProperties(sourceProfile, profile);
            }
        }

        return profile;
    }

    private String buildCorpseProfileName(String corpseName, String skinOwnerName) {
        String name = corpseName != null ? corpseName.trim() : "";
        if (name.isEmpty()) {
            name = skinOwnerName != null ? skinOwnerName.trim() : "";
        }
        if (name.isEmpty()) {
            name = "Corpse";
        }
        return name;
    }

    private Object extractProfile(Player player) {
        try {
            Method getProfile = player.getClass().getMethod("getProfile");
            return getProfile.invoke(player);
        } catch (Exception ignored) {
        }
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            for (String methodName : new String[] { "getGameProfile", "fI", "fJ" }) {
                try {
                    Method method = handle.getClass().getMethod(methodName);
                    return method.invoke(handle);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void copyProfileProperties(Object sourceProfile, Object targetProfile) {
        try {
            Method getProperties = sourceProfile.getClass().getMethod("getProperties");
            Object sourceProperties = getProperties.invoke(sourceProfile);
            Object targetProperties = getProperties.invoke(targetProfile);

            Method values = sourceProperties.getClass().getMethod("values");
            Collection<Object> propertyValues = (Collection<Object>) values.invoke(sourceProperties);

            Method put = targetProperties.getClass().getMethod("put", Object.class, Object.class);
            Method getName = propertyValues.iterator().next().getClass().getMethod("name");
            for (Object property : propertyValues) {
                put.invoke(targetProperties, getName.invoke(property), property);
            }
        } catch (Exception ignored) {
        }
    }

    private Object createClientInformation() throws Exception {
        Class<?> clientInformationClass = Class.forName("net.minecraft.server.level.ClientInformation");
        for (String methodName : new String[] { "createDefault", "a" }) {
            try {
                Method method = clientInformationClass.getMethod(methodName);
                return method.invoke(null);
            } catch (NoSuchMethodException ignored) {
            }
        }
        Constructor<?> constructor = clientInformationClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object[] defaults = new Object[constructor.getParameterTypes().length];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            defaults[i] = defaultValue(parameterTypes[i]);
        }
        return constructor.newInstance(defaults);
    }

    private Object createServerPlayer(Object minecraftServer, Object serverLevel, Object gameProfile, Object clientInformation) throws Exception {
        Class<?> serverPlayerClass = Class.forName("net.minecraft.server.level.ServerPlayer");
        for (Constructor<?> constructor : serverPlayerClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != 4) {
                continue;
            }
            if (!parameterTypes[0].isInstance(minecraftServer) || !parameterTypes[1].isInstance(serverLevel)
                    || !parameterTypes[2].isInstance(gameProfile) || !parameterTypes[3].isInstance(clientInformation)) {
                continue;
            }
            return constructor.newInstance(minecraftServer, serverLevel, gameProfile, clientInformation);
        }
        throw new NoSuchMethodException("No compatible ServerPlayer constructor found for " + getImplementationName());
    }

    private void moveEntity(Object entity, Location location, float yaw, float pitch) throws Exception {
        for (Method method : entity.getClass().getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if ("moveTo".equals(method.getName()) && parameterTypes.length == 5
                    && parameterTypes[0] == double.class && parameterTypes[1] == double.class
                    && parameterTypes[2] == double.class && parameterTypes[3] == float.class
                    && parameterTypes[4] == float.class) {
                method.invoke(entity, location.getX(), location.getY(), location.getZ(), yaw, pitch);
                return;
            }
        }

        Method setPos = entity.getClass().getMethod("setPos", double.class, double.class, double.class);
        setPos.invoke(entity, location.getX(), location.getY(), location.getZ());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void configureCorpsePose(Object serverPlayer, Location location) throws Exception {
        invokeBooleanSetter(serverPlayer, "setNoGravity", true);
        invokeBooleanSetter(serverPlayer, "setSilent", true);
        invokeBooleanSetter(serverPlayer, "setInvulnerable", true);

        Object bedPos = createBlockPosition(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
        if (tryConfigureSleepingPose(serverPlayer, bedPos)) {
            return;
        }

        Class<?> poseClass = Class.forName("net.minecraft.world.entity.Pose");
        Object sleepingPose = Enum.valueOf((Class<Enum>) poseClass.asSubclass(Enum.class), "SLEEPING");
        invokeMethod(serverPlayer, "setPose", poseClass, sleepingPose);
        tryInvokeCompatibleMethod(serverPlayer, new String[] { "b", "c" }, bedPos.getClass(), bedPos);
    }

    private boolean tryConfigureSleepingPose(Object serverPlayer, Object bedPos) {
        try {
            Field fauxSleeping = findField(serverPlayer.getClass(), "fauxSleeping");
            if (fauxSleeping != null) {
                fauxSleeping.setAccessible(true);
                fauxSleeping.set(serverPlayer, Boolean.TRUE);
            }

            Method sleepMethod = findCompatibleMethod(serverPlayer.getClass(), new String[] { "startSleepInBed", "a" }, bedPos.getClass(), boolean.class);
            if (sleepMethod != null) {
                sleepMethod.invoke(serverPlayer, bedPos, Boolean.TRUE);
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private Object createPlayerInfoAddPacket(Object serverPlayer) throws Exception {
        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
        Object templateViewer = null;
        for (Player online : Bukkit.getOnlinePlayers()) {
            templateViewer = online;
            break;
        }
        if (templateViewer == null) {
            throw new IllegalStateException("Cannot create player corpse packet without an online viewer template");
        }

        Method getHandle = templateViewer.getClass().getMethod("getHandle");
        Object templateHandle = getHandle.invoke(templateViewer);

        Object packet = null;
        for (Method method : packetClass.getMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!packetClass.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && Collection.class.isAssignableFrom(parameterTypes[0])) {
                packet = method.invoke(null, Collections.singletonList(templateHandle));
                break;
            }
        }
        if (packet == null) {
            throw new NoSuchMethodException("Unable to create template ClientboundPlayerInfoUpdatePacket");
        }

        Field entriesField = packetClass.getDeclaredField("c");
        entriesField.setAccessible(true);
        entriesField.set(packet, Collections.singletonList(createPlayerInfoEntry(serverPlayer)));
        return packet;
    }

    private Object createPlayerInfoEntry(Object serverPlayer) throws Exception {
        Class<?> entryClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$b");
        Object profile = invokeMethod(serverPlayer, "getGameProfile");
        UUID uuid = (UUID) invokeMethod(profile, "getId");

        Class<?> gamemodeClass = Class.forName("net.minecraft.world.level.EnumGamemode");
        Method byId = gamemodeClass.getMethod("a", int.class);
        Object survivalMode = byId.invoke(null, Integer.valueOf(0));

        Class<?> componentClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
        Method literal = componentClass.getMethod("a", String.class);
        Object displayName = literal.invoke(null, invokeMethod(profile, "getName"));

        Constructor<?> constructor = entryClass.getConstructor(
                UUID.class,
                Class.forName("com.mojang.authlib.GameProfile"),
                boolean.class,
                int.class,
                gamemodeClass,
                componentClass,
                Class.forName("net.minecraft.network.chat.RemoteChatSession$a")
        );
        return constructor.newInstance(uuid, profile, Boolean.TRUE, Integer.valueOf(0), survivalMode, displayName, null);
    }

    private Object createPlayerInfoRemovePacket(List<UUID> uuids) throws Exception {
        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
        Constructor<?> constructor = packetClass.getConstructor(List.class);
        return constructor.newInstance(uuids);
    }

    private Object createSpawnPacket(Object serverPlayer, Location location) throws Exception {
        Method spawnMethod = findCompatibleMethod(serverPlayer.getClass(), new String[] { "getAddEntityPacket", "P" });
        if (spawnMethod != null && spawnMethod.getParameterTypes().length == 0) {
            return spawnMethod.invoke(serverPlayer);
        }

        Object trackerEntry = createTrackerEntry(serverPlayer, location);
        if (spawnMethod != null && spawnMethod.getParameterTypes().length == 1) {
            return spawnMethod.invoke(serverPlayer, trackerEntry);
        }

        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
        for (Constructor<?> constructor : packetClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 2 && parameterTypes[0].isInstance(serverPlayer) && parameterTypes[1].isInstance(trackerEntry)) {
                return constructor.newInstance(serverPlayer, trackerEntry);
            }
        }

        throw new NoSuchMethodException("No compatible spawn packet method found for " + getImplementationName());
    }

    private Object createMetadataPacket(int entityId, Object entity) throws Exception {
        Method getEntityData = findCompatibleMethod(entity.getClass(), new String[] { "getEntityData", "aj", "ai" });
        if (getEntityData == null) {
            throw new NoSuchMethodException("No compatible entity data method found for " + getImplementationName());
        }
        Object entityData = getEntityData.invoke(entity);

        Method packData = findCompatibleMethod(entityData.getClass(), new String[] { "packAll", "getNonDefaultValues", "c", "packDirty" });
        if (packData == null) {
            throw new NoSuchMethodException("No compatible metadata packer found for " + getImplementationName());
        }
        Object packed = packData.invoke(entityData);
        if (!(packed instanceof List)) {
            throw new IllegalStateException("Metadata packer did not return a List");
        }

        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");
        Constructor<?> constructor = packetClass.getConstructor(int.class, List.class);
        return constructor.newInstance(Integer.valueOf(entityId), packed);
    }

    private Object createDestroyPacket(int entityId) throws Exception {
        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
        Constructor<?> constructor = packetClass.getConstructor(int[].class);
        return constructor.newInstance(new int[] { entityId });
    }

    private Object createHiddenNameTeamCreatePacket(String profileName) {
        try {
            Object scoreboard = createNmsScoreboard();
            Object team = createNmsScoreboardTeam(scoreboard, profileName);
            if (team == null) {
                return null;
            }
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
            Method createMethod = findCompatibleMethod(packetClass, new String[] { "a" }, team.getClass(), boolean.class);
            return createMethod != null ? createMethod.invoke(null, team, Boolean.TRUE) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object createHiddenNameTeamRemovePacket(String profileName) {
        try {
            Object scoreboard = createNmsScoreboard();
            Object team = createNmsScoreboardTeam(scoreboard, profileName);
            if (team == null) {
                return null;
            }
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
            Method removeMethod = findCompatibleMethod(packetClass, new String[] { "a" }, team.getClass());
            return removeMethod != null ? removeMethod.invoke(null, team) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object createNmsScoreboard() throws Exception {
        Class<?> scoreboardClass = Class.forName("net.minecraft.world.scores.Scoreboard");
        return scoreboardClass.getConstructor().newInstance();
    }

    private Object createNmsScoreboardTeam(Object scoreboard, String profileName) throws Exception {
        Class<?> teamClass = Class.forName("net.minecraft.world.scores.ScoreboardTeam");
        Constructor<?> constructor = teamClass.getConstructor(scoreboard.getClass(), String.class);
        String teamName = buildHiddenTeamName(profileName);
        Object team = constructor.newInstance(scoreboard, teamName);

        Class<?> visibilityClass = Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility");
        Method byString = visibilityClass.getMethod("a", String.class);
        Object neverVisibility = byString.invoke(null, "never");
        Method setNameTagVisibility = findCompatibleMethod(teamClass, new String[] { "a" }, visibilityClass);
        if (setNameTagVisibility != null) {
            setNameTagVisibility.invoke(team, neverVisibility);
        }

        Method addPlayerToTeam = findCompatibleMethod(scoreboard.getClass(), new String[] { "a" }, String.class, teamClass);
        if (addPlayerToTeam != null) {
            addPlayerToTeam.invoke(scoreboard, profileName, team);
        } else {
            Method getEntriesMethod = findCompatibleMethod(teamClass, new String[] { "g" });
            Object entries = getEntriesMethod != null ? getEntriesMethod.invoke(team) : null;
            if (entries instanceof Collection) {
                ((Collection) entries).add(profileName);
            }
        }

        return team;
    }

    private String buildHiddenTeamName(String profileName) {
        String normalized = (profileName == null ? "corpse" : profileName).toLowerCase();
        String baseName = "slc_" + normalized;
        if (baseName.length() <= 16) {
            return baseName;
        }
        return baseName.substring(0, 16);
    }

    private Object createTrackerEntry(Object entity, Location location) throws Exception {
        Object worldHandle = getWorldHandle(location);
        Class<?> trackerEntryClass = resolveTrackerEntryClass();

        for (Constructor<?> constructor : trackerEntryClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            if (parameterTypes.length == 6
                    && parameterTypes[0].isInstance(worldHandle)
                    && parameterTypes[1].isInstance(entity)
                    && parameterTypes[2] == int.class
                    && parameterTypes[3] == boolean.class
                    && Consumer.class.isAssignableFrom(parameterTypes[4])
                    && Set.class.isAssignableFrom(parameterTypes[5])) {
                Consumer<Object> packetSender = packet -> {
                };
                return constructor.newInstance(worldHandle, entity, Integer.valueOf(0), Boolean.FALSE, packetSender, Collections.emptySet());
            }

            if (parameterTypes.length == 5 && parameterTypes[0].isInstance(worldHandle) && parameterTypes[1].isInstance(entity)) {
                Class<?> synchronizerClass = findNestedClass(trackerEntryClass, "Synchronizer");
                Object packetSender = synchronizerClass != null
                        ? Proxy.newProxyInstance(synchronizerClass.getClassLoader(), new Class<?>[] { synchronizerClass }, new NoOpInvocationHandler())
                        : null;
                return constructor.newInstance(worldHandle, entity, Integer.valueOf(0), Boolean.FALSE, packetSender);
            }
        }

        throw new NoSuchMethodException("No compatible tracker entry constructor found");
    }

    private Class<?> resolveTrackerEntryClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.server.level.EntityTrackerEntry");
        } catch (ClassNotFoundException ignored) {
            return Class.forName("net.minecraft.server.level.ServerEntity");
        }
    }

    private void sendPacket(Player viewer, Object packet) throws Exception {
        if (packet == null) {
            return;
        }

        Method getHandle = viewer.getClass().getMethod("getHandle");
        Object handle = getHandle.invoke(viewer);
        Object connection = getFieldOrGetter(handle, "connection", "c");
        Method sendMethod = findCompatibleMethod(connection.getClass(), new String[] { "send", "b", "sendPacket" }, packet.getClass());
        if (sendMethod == null) {
            for (Method method : connection.getClass().getMethods()) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(packet.getClass())) {
                    sendMethod = method;
                    break;
                }
            }
        }
        if (sendMethod == null) {
            throw new NoSuchMethodException("No compatible packet send method found for " + connection.getClass().getName());
        }
        sendMethod.invoke(connection, packet);
    }

    private Object getFieldOrGetter(Object instance, String... names) throws Exception {
        for (String name : names) {
            try {
                Field field = instance.getClass().getField(name);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
            }
            try {
                Method method = instance.getClass().getMethod(name);
                return method.invoke(instance);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchFieldException("No compatible field/getter found on " + instance.getClass().getName());
    }

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private Method findCompatibleMethod(Class<?> type, String[] names, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (!matchesName(method.getName(), names)) {
                    continue;
                }
                if (parameterTypes.length > 0 && !parametersAssignable(method.getParameterTypes(), parameterTypes)) {
                    continue;
                }
                method.setAccessible(true);
                return method;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private boolean matchesName(String actual, String[] expected) {
        for (String name : expected) {
            if (name.equals(actual)) {
                return true;
            }
        }
        return false;
    }

    private boolean parametersAssignable(Class<?>[] declared, Class<?>[] actual) {
        if (declared.length != actual.length) {
            return false;
        }
        for (int i = 0; i < declared.length; i++) {
            if (!wrap(declared[i]).isAssignableFrom(wrap(actual[i]))) {
                return false;
            }
        }
        return true;
    }

    private void invokeBooleanSetter(Object target, String methodName, boolean value) {
        try {
            Method method = target.getClass().getMethod(methodName, boolean.class);
            method.invoke(target, Boolean.valueOf(value));
        } catch (Exception ignored) {
        }
    }

    private Object invokeMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private void invokeMethod(Object target, String methodName, Class<?> parameterType, Object arg) throws Exception {
        Method method = target.getClass().getMethod(methodName, parameterType);
        method.invoke(target, arg);
    }

    private boolean tryInvokeCompatibleMethod(Object target, String[] methodNames, Class<?> parameterType, Object arg) {
        try {
            Method method = findCompatibleMethod(target.getClass(), methodNames, parameterType);
            if (method == null) {
                return false;
            }
            method.invoke(target, arg);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Object createBlockPosition(int x, int y, int z) throws Exception {
        Class<?> blockPosClass = Class.forName("net.minecraft.core.BlockPosition");
        Constructor<?> constructor = blockPosClass.getConstructor(int.class, int.class, int.class);
        return constructor.newInstance(Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z));
    }

    private Class<?> findNestedClass(Class<?> owner, String simpleName) {
        for (Class<?> nested : owner.getDeclaredClasses()) {
            if (nested.getSimpleName().equals(simpleName)) {
                return nested;
            }
        }
        return null;
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return Boolean.FALSE;
        }
        if (type == byte.class) {
            return Byte.valueOf((byte) 0);
        }
        if (type == short.class) {
            return Short.valueOf((short) 0);
        }
        if (type == int.class) {
            return Integer.valueOf(0);
        }
        if (type == long.class) {
            return Long.valueOf(0L);
        }
        if (type == float.class) {
            return Float.valueOf(0.0F);
        }
        if (type == double.class) {
            return Double.valueOf(0.0D);
        }
        if (type == char.class) {
            return Character.valueOf('\0');
        }
        return null;
    }

    private Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private static final class VisualCorpse {
        private final int entityId;
        private final UUID entityUuid;
        private final UUID profileUuid;
        private final String profileName;
        private final Object playerInfoPacket;
        private final Object spawnPacket;
        private final Object metadataPacket;
        private final Object removeInfoPacket;
        private final Object teamCreatePacket;
        private final Object teamRemovePacket;

        private VisualCorpse(int entityId, UUID entityUuid, UUID profileUuid, String profileName,
                             Object playerInfoPacket, Object spawnPacket, Object metadataPacket, Object removeInfoPacket,
                             Object teamCreatePacket, Object teamRemovePacket) {
            this.entityId = entityId;
            this.entityUuid = entityUuid;
            this.profileUuid = profileUuid;
            this.profileName = profileName;
            this.playerInfoPacket = playerInfoPacket;
            this.spawnPacket = spawnPacket;
            this.metadataPacket = metadataPacket;
            this.removeInfoPacket = removeInfoPacket;
            this.teamCreatePacket = teamCreatePacket;
            this.teamRemovePacket = teamRemovePacket;
        }
    }

    private static final class NoOpInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?> returnType = method.getReturnType();
            if (returnType == Boolean.TYPE) {
                return Boolean.FALSE;
            }
            if (returnType == Integer.TYPE) {
                return Integer.valueOf(0);
            }
            if (returnType == Long.TYPE) {
                return Long.valueOf(0L);
            }
            if (returnType == Float.TYPE) {
                return Float.valueOf(0.0F);
            }
            if (returnType == Double.TYPE) {
                return Double.valueOf(0.0D);
            }
            if (returnType == Short.TYPE) {
                return Short.valueOf((short) 0);
            }
            if (returnType == Byte.TYPE) {
                return Byte.valueOf((byte) 0);
            }
            if (returnType == Character.TYPE) {
                return Character.valueOf('\0');
            }
            return null;
        }
    }
}
