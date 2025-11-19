package com.faridfaharaj.profitable.hooks;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Assets;
import net.milkbowl.vault.economy.EconomyResponse;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerPointsHook extends EconomyHook{

    private static PlayerPointsAPI api;

    public PlayerPointsHook(Profitable profitable) {
        super(profitable);
    }

    @Override
    public PlayerPointsAPI getApi(){
        return api;
    }

    @Override
    public boolean initHook(Profitable profitable){
        if(profitable.getConfig().getBoolean("player-points-support")){
            if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
                api = PlayerPoints.getInstance().getAPI();
                if(api != null){
                    // ASSET = Configuration.MAINCURRENCYASSET;
                    profitable.getLogger().info("Connected to PlayerPoints");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void depositAccount(String account, double ammount){
        UUID uuid = Accounts.getAccUUID(account);
        if(uuid != null){
            getApi().give(uuid,(int) ammount);
        }
    }

    @Override
    public void withdrawAccount(String account, double ammount) {
        UUID uuid = Accounts.getAccUUID(account);
        if(uuid != null){
            getApi().take(uuid,(int) ammount);
        }
    }

}
