package com.faridfaharaj.profitable.hooks;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.tables.Assets;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private static Asset ASSET;
    private static Economy economy = null;
    private static boolean isConnected;

    public static boolean inithook(Profitable profitable){
        if(profitable.getConfig().getBoolean("vault-support")){

            economy = VaultHook.findEconomy(profitable);
            isConnected = economy != null;

        }else{

            isConnected = false;

        }

        if(isConnected) {
            Asset asset = Assets.getAssetData(profitable.getConfig().getString("hooks.playerpoints.asset"));
            if(asset == null){
                ASSET = Configuration.MAINCURRENCYASSET;
            }else {
                ASSET = asset;
            }
            profitable.getLogger().info("Connected to Vault");
        }
        return isConnected;
    }

    private static Economy findEconomy(Plugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }

        return rsp.getProvider();
    }

    public static Economy getEconomy(){
        return economy;
    }

    public static Asset getAsset(){
        return ASSET;
    }

    public static boolean isConnected(){
        return isConnected;
    }

}
