package net.enelson.sopli.lib;

import net.enelson.sopli.lib.external.ItemNBTUtils;
import net.enelson.sopli.lib.item.ItemUtils;
import net.enelson.sopli.lib.util.Util;

public final class SopLib {

	private static SopLib INSTANCE;

	private final Util util;
	private final ItemUtils itemUtils;
	private final ItemNBTUtils itemNbtUtils;

	public SopLib(Util util, ItemUtils itemUtils, ItemNBTUtils itemNbtUtils) {
		this.util = util;
		this.itemUtils = itemUtils;
		this.itemNbtUtils = itemNbtUtils;
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

	@Override
	public String toString() {
		return "SopLib{" + "util=" + util + ", itemUtils=" + itemUtils + ", itemNbtUtils=" + itemNbtUtils + '}';
	}
}
