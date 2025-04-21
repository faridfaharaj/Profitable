package com.faridfaharaj.profitable.hooks;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;

import java.io.IOException;

public class PlayerPointsHook {

    private static Asset ASSET;
    private static PlayerPointsAPI api;
    private static boolean isConnected;

    public static PlayerPointsAPI getApi(){
        return api;
    }

    public static boolean initHook(Profitable profitable){
        if(profitable.getConfig().getBoolean("player-points-support")){
            if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
                api = PlayerPoints.getInstance().getAPI();
                isConnected = api != null;
            }
        }else {
            isConnected = false;
        }

        if(isConnected) {
            ASSET = Asset.StringToCurrency(profitable.getConfig().getString("main-currency.playerpoints-currency", "PTS_Player Points_#ff6d92"));
            profitable.getLogger().info("Connected to PlayerPoints");
        }
        return isConnected;
    }

    public static Asset getAsset(){
        return ASSET;
    }

    public static boolean isConnected(){
        return isConnected;
    }

}
