package com.faridfaharaj.profitable.util;

import com.faridfaharaj.profitable.Profitable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NamingUtil {

    private static final Map<String, String> commodityNaming = new HashMap<>();

    private static final Pattern UNWANTED_SUFFIXES = Pattern.compile("_(block|ingot|nugget|bucket)$");

    static {
        commodityNaming.put("COW", "Live Cattle");
        commodityNaming.put("PIG", "Lean Hogs");
        commodityNaming.put("SHEEP", "Lamb");
        commodityNaming.put("CHICKEN", "Broilers");

        commodityNaming.put("ACACIA_PLANKS", "Lumber");
        commodityNaming.put("ACACIA_LOG", "Timber");

        commodityNaming.put("BIRCH_PLANKS", "Lumber");
        commodityNaming.put("BIRCH_LOG", "Timber");

        commodityNaming.put("CHERRY_PLANKS", "Lumber");
        commodityNaming.put("CHERRY_LOG", "Timber");

        commodityNaming.put("DARK_OAK_PLANKS", "Lumber");
        commodityNaming.put("DARK_OAK_LOG", "Timber");

        commodityNaming.put("JUNGLE_PLANKS", "Lumber");
        commodityNaming.put("JUNGLE_LOG", "Timber");

        commodityNaming.put("MANGROVE_PLANKS", "Lumber");
        commodityNaming.put("MANGROVE_LOG", "Timber");

        commodityNaming.put("OAK_PLANKS", "Lumber");
        commodityNaming.put("OAK_LOG", "Timber");

        commodityNaming.put("SPRUCE_PLANKS", "Lumber");
        commodityNaming.put("SPRUCE_LOG", "Timber");
    }




    private static final String[] assetTypeNaming = {"" , "assets.categories.forex", "assets.categories.commodity", "assets.categories.commodity", "Commodity", "Commodity", "Stock"};

    public static String nameCommodity(String code) {
        String name = commodityNaming.get(code);
        if (name != null) {
            return name;
        }
        name = UNWANTED_SUFFIXES.matcher(code).replaceAll("").toLowerCase().replace("_", " ");

        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public static String nameType(int assetType) {
        if (assetType < 1 || assetType >= assetTypeNaming.length) {
            return "Unknown";
        }
        return Profitable.getLang().getString(assetTypeNaming[assetType]);
    }
}
