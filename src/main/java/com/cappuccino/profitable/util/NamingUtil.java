package com.cappuccino.profitable.util;

import org.bukkit.ChatColor;

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




    private static final String[] assetTypeNaming = {"" , "Forex", "Commodity", "Commodity", "Commodity", "Commodity", "Stock"};

    public static String nameCommodity(String code) {
        String name = commodityNaming.get(code);
        if (name != null) {
            return name;
        }

        return UNWANTED_SUFFIXES.matcher(code).replaceAll("").toLowerCase();
    }

    public static String nameType(int assetType) {
        if (assetType < 1 || assetType >= assetTypeNaming.length) {
            return "Unknown";
        }
        return assetTypeNaming[assetType];
    }

    public static String profitablePrefix(){
        return ChatColor.AQUA +"[Profitable] " + ChatColor.RESET;
    }

    public static String profitableTopSeparator(){
        return ChatColor.AQUA +"---------------- [Profitable] -----------------" + ChatColor.RESET;
    }

    public static String profitableBottomSeparator(){
        return ChatColor.AQUA + "--------------------------------------------" + ChatColor.RESET;
    }

    public static String genericSeparator(ChatColor color){
        return color + "--------------------------------------------" + ChatColor.RESET;
    }

    public static String genericMissingPerm(){
        return ChatColor.RED + "You don't have permission to do that";
    }

}
