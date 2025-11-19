package com.faridfaharaj.profitable.hooks;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Accounts;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public final class VaultHook extends EconomyHook{

    private static Economy economy = null;

    public VaultHook(Profitable profitable) {
        super(profitable);
    }

    @Override
    public boolean initHook(Profitable profitable){
        if(profitable.getConfig().getBoolean("vault-support")){
            economy = VaultHook.findEconomy(profitable);
            if(economy != null){
                profitable.getLogger().info("Connected to Vault");
                return true;
            }

        }
        return false;
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

    @Override
    public Economy getApi() {
        return economy;
    }

    @Override
    public void depositAccount(String account, double ammount){
        UUID uuid = Accounts.getAccUUID(account);
        if(uuid != null){
            Player player = Profitable.getInstance().getServer().getPlayer(uuid);
            if(player != null){
                EconomyResponse es = getApi().depositPlayer(player, ammount);
            }else{
                OfflinePlayer offlinePlayer = Profitable.getInstance().getServer().getOfflinePlayer(uuid);
                EconomyResponse es = getApi().depositPlayer(offlinePlayer, ammount);
            }
        }
    }

    @Override
    public void withdrawAccount(String account, double ammount) {
        UUID uuid = Accounts.getAccUUID(account);
        if(uuid != null){
            Player player = Profitable.getInstance().getServer().getPlayer(uuid);
            if(player != null){
                EconomyResponse es = getApi().withdrawPlayer(player, ammount);
            }else{
                OfflinePlayer offlinePlayer = Profitable.getInstance().getServer().getOfflinePlayer(uuid);
                EconomyResponse es = getApi().withdrawPlayer(offlinePlayer, ammount);
            }
        }
    }

}
