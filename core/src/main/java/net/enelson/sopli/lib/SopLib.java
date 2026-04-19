package net.enelson.sopli.lib;

import net.enelson.sopli.lib.customblocks.CustomBlockVisualService;
import net.enelson.sopli.lib.customblocks.CustomBlockVisualServices;
import net.enelson.sopli.lib.database.DatabaseService;
import net.enelson.sopli.lib.external.ItemNBTUtils;
import net.enelson.sopli.lib.item.ItemUtils;
import net.enelson.sopli.lib.protection.ProtectionService;
import net.enelson.sopli.lib.protection.ProtectionServices;
import net.enelson.sopli.lib.region.RegionService;
import net.enelson.sopli.lib.region.RegionServices;
import net.enelson.sopli.lib.text.TextUtils;
import net.enelson.sopli.lib.util.Util;

public final class SopLib {
    private static SopLib INSTANCE;

    private final Util util;
    private final ItemUtils itemUtils;
    private final ItemNBTUtils itemNbtUtils;
    private final CustomBlockVisualService customBlockVisualService;
    private final ProtectionService protectionService;
    private final RegionService regionService;
    private final TextUtils textUtils;
    private final DatabaseService databaseService;

    public SopLib(Util util, ItemUtils itemUtils, ItemNBTUtils itemNbtUtils) {
        this.util = util;
        this.itemUtils = itemUtils;
        this.itemNbtUtils = itemNbtUtils;
        this.customBlockVisualService = CustomBlockVisualServices.resolve();
        this.protectionService = ProtectionServices.resolve();
        this.regionService = RegionServices.resolve();
        this.textUtils = new TextUtils();
        this.databaseService = new DatabaseService();
    }

    public static SopLib getInstance() {
        return INSTANCE;
    }

    public static void setInstance(SopLib instance) {
        INSTANCE = instance;
    }

    public Util getUtil() {
        return util;
    }

    public ItemUtils getItemUtils() {
        return itemUtils;
    }

    public ItemNBTUtils getItemNbtUtils() {
        return itemNbtUtils;
    }

    public CustomBlockVisualService getCustomBlockVisualService() {
        return customBlockVisualService;
    }

    public ProtectionService getProtectionService() {
        return protectionService;
    }

    public RegionService getRegionService() {
        return regionService;
    }

    public TextUtils getTextUtils() {
        return textUtils;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public void shutdown() {
        databaseService.shutdown();
    }

    @Override
    public String toString() {
        return "SopLib{" +
                "util=" + util +
                ", itemUtils=" + itemUtils +
                ", itemNbtUtils=" + itemNbtUtils +
                ", customBlockVisualService=" + customBlockVisualService +
                ", protectionService=" + protectionService +
                ", regionService=" + regionService +
                ", textUtils=" + textUtils +
                ", databaseService=" + databaseService +
                '}';
    }
}
