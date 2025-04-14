package com.faridfaharaj.profitable;

import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.util.RandomUtil;
import com.faridfaharaj.profitable.hooks.VaultHook;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Configuration {

    //VISUAL
    public static TextColor COLORPROFITABLE = TextColor.color(0x64FF9D);

    public static TextColor COLORBULLISH = TextColor.color(0x8CD740);
    public static TextColor COLORBEARISH = TextColor.color(0xFA413B);

    public static final TextColor COLORTEXT = NamedTextColor.WHITE;
    public static final TextColor COLORERROR = NamedTextColor.RED;
    public static final TextColor COLORWARN = NamedTextColor.YELLOW;
    public static final TextColor COLORINFO = NamedTextColor.YELLOW;
    public static final TextColor COLOREMPTY = NamedTextColor.GRAY;

    public static boolean MULTIWORLD;
    public static boolean GENERATEASSETS;


    //MAINCURRENCY
    public static Asset MAINCURRENCYASSET = null;
    //DEFAULT ASSETS
    public static List<String> ALLOWEITEMS = new ArrayList<>();
    public static List<String> ALLOWENTITIES = new ArrayList<>();

    public static boolean HOOKED = false;


    public static void reloadConfig(Profitable profitable){
        profitable.reloadConfig();
        loadConfig(profitable);
    }

    public static void loadConfig(Profitable profitable){
        profitable.saveDefaultConfig();
        FileConfiguration config = profitable.getConfig();

        MULTIWORLD = config.getBoolean("database.database-per-world");
        GENERATEASSETS = config.getBoolean("exchange.commodities.generation.active");

        if(GENERATEASSETS){
            //comodities
            if (config.getBoolean("exchange.commodities.generation.item-whitelisting")) {

                Set<String> itemWhitelist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-item-whitelist"));

                for (Material material : Material.values()) {
                    String name = material.name();

                    if (itemWhitelist.contains(name)) {
                        ALLOWEITEMS.add(name);
                    }

                }

            } else {

                Set<String> itemBlacklist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-item-blacklist"));

                for (Material material : Material.values()) {
                    String name = material.name();

                    if (!itemBlacklist.contains(name)) {
                        ALLOWEITEMS.add(name);
                    }

                }

            }

            if (config.getBoolean("exchange.commodities.generation.entity-whitelisting")) {
                Set<String> entityWhitelist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-entity-whitelist"));

                for (EntityType entity : EntityType.values()) {
                    String name = entity.name();

                    Class<?> entityClass = entity.getEntityClass();
                    if (entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass) || name.equals("PLAYER")) {
                        continue;
                    }

                    if (entityWhitelist.contains(name)) {
                        ALLOWENTITIES.add(name);
                    }

                }

                profitable.getLogger().info("Commodities to be generated:");
                profitable.getLogger().info("- Items: " + ALLOWEITEMS);
                profitable.getLogger().info("- Entities: " + ALLOWENTITIES);

            } else {

                Set<String> entityBlacklist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-entity-blacklist"));

                for (EntityType entity : EntityType.values()) {
                    String name = entity.name();

                    Class<?> entityClass = entity.getEntityClass();
                    if (entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass) || name.equals("PLAYER")) {
                        continue;
                    }

                    if (!entityBlacklist.contains(name)) {
                        ALLOWENTITIES.add(name);
                    }
                }

            }

        }else{

            for (Material material : Material.values()) {
                String name = material.name();

                ALLOWEITEMS.add(name);

            }

            for (EntityType entity : EntityType.values()) {
                String name = entity.name();

                Class<?> entityClass = entity.getEntityClass();
                if (entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass) || name.equals("PLAYER")) {
                    continue;
                }

                ALLOWENTITIES.add(name);

            }



        }

        //hooks
        if(VaultHook.inithook(profitable)){
            HOOKED = true;
        }
        if(PlayerPointsHook.initHook(profitable)){
            HOOKED = true;
        }
    }

    public static void loadMainCurrency() throws IOException {
        FileConfiguration config = Profitable.getInstance().getConfig();


        String[] MCdata = config.getString("main-currency.currency", "EMD_Villager Emerald_#00ff00").split("_");
        Asset mainCurrency = Assets.getAssetData(MCdata[0]);

        if(mainCurrency == null){

            if(config.getBoolean("main-currency.create-last-resort-only")){

                if(VaultHook.isConnected()){
                    MAINCURRENCYASSET = VaultHook.getAsset();
                    return;
                }else if(PlayerPointsHook.isConnected()){
                    MAINCURRENCYASSET = PlayerPointsHook.getAsset();
                    return;
                }

            }

            TextColor color;
            String name;

            if(MCdata.length >= 2){

                name = MCdata[1];

            }else{
                name = MCdata[0];
            }
            if(MCdata.length >= 3){

                color = TextColor.fromHexString(MCdata[2]);

                if(color == null){
                    color = RandomUtil.randomTextColor();
                }

            }else {
                color = RandomUtil.randomTextColor();
            }

            MAINCURRENCYASSET = new Asset(MCdata[0], 1, color, name);
            Assets.addAsset(MAINCURRENCYASSET.getCode(), 1, Asset.metaData(MAINCURRENCYASSET));
        }else{
            MAINCURRENCYASSET = mainCurrency;
        }
    }

}
