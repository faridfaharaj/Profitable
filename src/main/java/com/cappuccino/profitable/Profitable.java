package com.cappuccino.profitable;

import com.cappuccino.profitable.commands.*;

import com.cappuccino.profitable.data.DataBase;
import com.cappuccino.profitable.tasks.TemporalItems;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.*;


public final class Profitable extends JavaPlugin {

    private static Profitable instance;

    public static File DATAPATH;

    public static Profitable getInstance() {
        return instance;
    }

    public static void setInstance(Profitable profitable){
        instance = profitable;
    }

    @Override
    public void onEnable() {
        setInstance(this);

        //config-----------------

        Configuration.loadConfig(this);

        //SQLite----------------------------
        DATAPATH = new File(getDataFolder().getAbsolutePath(), "data");
        if(!DATAPATH.exists()) {
            if(!DATAPATH.mkdirs()){
                getLogger().severe("couldn't create saves folder, plugin might not work as intended");
            }
        }

        if(!getConfig().getBoolean("multi-world-support")){
            try {
                DataBase.setConnection("serverWide");
                getLogger().info("Using single server-wide database");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else{
            getLogger().info("Using per-world databases");
        }

        getLogger().info("Loaded Sqlite database");


        //commands-------------------------


        getCommand("buy").setExecutor(new TransactCommand());
        getCommand("buy").setTabCompleter(new TransactCommand.CommandTabCompleter());

        getCommand("sell").setExecutor(new TransactCommand());
        getCommand("sell").setTabCompleter(new TransactCommand.CommandTabCompleter());

        getCommand("assets").setExecutor(new AssetsCommand());
        getCommand("assets").setTabCompleter(new AssetsCommand.CommandTabCompleter());

        getCommand("account").setExecutor(new AccountCommand());
        getCommand("account").setTabCompleter(new AccountCommand.CommandTabCompleter());

        getCommand("admin").setExecutor(new AdminCommand());
        getCommand("admin").setTabCompleter(new AdminCommand.CommandTabCompleter());


        getCommand("help").setExecutor(new HelpCommand());
        getCommand("help").setTabCompleter(new HelpCommand.CommandTabCompleter());

        getCommand("profitable").setExecutor(new PluginInfoCommand());

        //event handler------------------
        getServer().getPluginManager().registerEvents(new Events(), this);



        getLogger().info("Profitable is ready to profit!");
    }

    @Override
    public void onDisable() {
        DataBase.closeAllConnections();

        for(UUID playerid:TemporalItems.holdingTemp.keySet()){

            Player player = getServer().getPlayer(playerid);
            if(player != null){
                TemporalItems.removeTempItem(player);
            }
        }
    }

}
