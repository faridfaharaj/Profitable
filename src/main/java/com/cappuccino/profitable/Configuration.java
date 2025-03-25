package com.cappuccino.profitable;

import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.util.VaultCompat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configuration {

    //MAINCURRENCY
    public static Asset MAINCURRENCYASSET;
    //DEFAULT ASSETS
    public static List<String> ALLOWEITEMS = new ArrayList<>();
    public static List<String> ALLOWENTITIES = new ArrayList<>();

    //VAULT
    public static boolean VAULTENABLED;
    public static Economy ECONOMY = null;


    public static void loadConfig(Profitable profitable){
        profitable.saveDefaultConfig();
        FileConfiguration config = profitable.getConfig();

        if(profitable.getConfig().getBoolean("vault-support")){
            ECONOMY = VaultCompat.getEconomy(profitable);
            VAULTENABLED = ECONOMY != null;
        }else{
            VAULTENABLED = false;
        }
        if(VAULTENABLED) profitable.getLogger().info("Connected to Vault");
        if(VAULTENABLED && profitable.getConfig().getBoolean("main-currency.vault-main-currency")){
            MAINCURRENCYASSET = new Asset(VaultCompat.getVaultCode(), 1, VaultCompat.getVaultColor(), VaultCompat.getVaultName());
        }else{
            MAINCURRENCYASSET = new Asset(config.getString("main-currency.currency-code").toUpperCase(), 1, ChatColor.valueOf(config.getString("main-currency.color").toUpperCase()), config.getString("main-currency.name"));
        }

        profitable.getLogger().info("Using " + MAINCURRENCYASSET.getCode() + " as main currency on the exchange");

        Set<String> itemWhitelist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.commodity-item-whitelist"));
        Set<String> itemBlacklist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.commodity-item-blacklist"));

        for (Material material : Material.values()) {
            String name = material.name();

            if (!itemWhitelist.isEmpty()) {

                if (itemWhitelist.contains(name.toLowerCase())) {
                    ALLOWEITEMS.add(name);
                }

            } else {

                if (!itemBlacklist.contains(name.toLowerCase())) {
                    ALLOWEITEMS.add(name);
                }

            }
        }

        Set<String> entityWhitelist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.commodity-entity-whitelist"));
        Set<String> entityBlacklist = new HashSet<>(Profitable.getInstance().getConfig().getStringList("exchange.commodities.commodity-entity-blacklist"));

        for (EntityType entity : EntityType.values()) {
            String name = entity.name();

            if (!entityWhitelist.isEmpty()) {

                if (entityWhitelist.contains(name.toLowerCase())) {
                    ALLOWENTITIES.add(name);
                }

            } else {

                if (!entityBlacklist.contains(name.toLowerCase())) {
                    ALLOWENTITIES.add(name);
                }

            }
        }
        profitable.getLogger().info("Generating Assets for allowed commodities: ");
        profitable.getLogger().info("-Items: " + ALLOWEITEMS);
        profitable.getLogger().info("-Entities: " + ALLOWENTITIES);

    }

}
