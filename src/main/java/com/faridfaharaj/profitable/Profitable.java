package com.faridfaharaj.profitable;

import com.faridfaharaj.profitable.commands.*;
import net.kyori.adventure.platform.bukkit.*;

import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.*;



/*                                                                            *
 *   Profitable is an exchange trading simulation plugin for minecraft.       *
 *   Copyright (C) 2025  faridfaharaj                                         *
 *                                                                            *
 *   This program is free software: you can redistribute it and/or modify     *
 *   it under the terms of the GNU General Public License as published by     *
 *   the Free Software Foundation, either version 3 of the License, or        *
 *   (at your option) any later version.                                      *
 *                                                                            *
 *   This program is distributed in the hope that it will be useful,          *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            *
 *   GNU General Public License for more details.                             *
 *                                                                            *
 *   You should have received a copy of the GNU General Public License        *
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.   *
*/
public final class Profitable extends JavaPlugin {

    private static Profitable instance;

    private static BukkitAudiences audiences;

    public static File DATAPATH;

    public static Profitable getInstance() {
        return instance;
    }

    public static BukkitAudiences getBukkitAudiences() {
        return audiences;
    }

    public static void setInstance(Profitable profitable){
        instance = profitable;
    }

    @Override
    public void onEnable() {
        getLogger().info("====================    Profitable    ====================" );
        setInstance(this);
        audiences = BukkitAudiences.create(this);

        //config-----------------
        Configuration.loadConfig(this);

        //SQLite----------------------------
        DATAPATH = new File(getDataFolder().getAbsolutePath(), "data");
        if(!DATAPATH.exists()) {
            if(!DATAPATH.mkdirs()){
                getLogger().severe("couldn't create saves folder, plugin might not work as intended");
            }
        }

        if(!getConfig().getBoolean("database-per-world")){
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

        getCommand("asset").setExecutor(new AssetCommand());
        getCommand("asset").setTabCompleter(new AssetCommand.CommandTabCompleter());

        getCommand("top").setExecutor(new TopCommand());
        getCommand("top").setTabCompleter(new TopCommand.CommandTabCompleter());


        getCommand("account").setExecutor(new AccountCommand());
        getCommand("account").setTabCompleter(new AccountCommand.CommandTabCompleter());

        getCommand("wallet").setExecutor(new WalletCommand());
        getCommand("wallet").setTabCompleter(new WalletCommand.CommandTabCompleter());

        getCommand("orders").setExecutor(new OrdersCommand());
        getCommand("orders").setTabCompleter(new OrdersCommand.CommandTabCompleter());

        getCommand("delivery").setExecutor(new DeliveryCommand());
        getCommand("delivery").setTabCompleter(new DeliveryCommand.CommandTabCompleter());

        getCommand("claimtag").setExecutor(new ClaimtagCommand());


        getCommand("admin").setExecutor(new AdminCommand());
        getCommand("admin").setTabCompleter(new AdminCommand.CommandTabCompleter());


        getCommand("help").setExecutor(new HelpCommand());
        getCommand("help").setTabCompleter(new HelpCommand.CommandTabCompleter());


        getCommand("profitable").setExecutor(new PluginInfoCommand());

        //event handler------------------
        getServer().getPluginManager().registerEvents(new Events(), this);



        getLogger().info("==========    Profitable is ready to profit!    ==========" );
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
