package com.faridfaharaj.profitable;

import com.faridfaharaj.profitable.commands.*;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.tcoded.folialib.FoliaLib;
import net.kyori.adventure.platform.bukkit.*;

import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private static FoliaLib foliaLib;

    public static Profitable getInstance() {
        return instance;
    }
    public static BukkitAudiences getBukkitAudiences() {
        return audiences;
    }
    public static FoliaLib getfolialib() {
        return foliaLib;
    }

    @Override
    public void onEnable() {
        getLogger().info("====================    Profitable    ====================" );

        instance = this;
        audiences = BukkitAudiences.create(this);
        foliaLib = new FoliaLib(this);

        foliaLib.getScheduler().runAsync(task -> {
            checkForUpdate(this);
        });

        //config-----------------
        Configuration.loadConfig(this);

        //DATABASE---------
        try {

            switch (getConfig().getInt("database.database-type")){
                case 0:
                    DataBase.connectSQLite();
                    getLogger().info("Connected to SQLite database");
                    break;
                case 1:
                    DataBase.connectMySQL();
                    getLogger().info("Connected to MySQL database");
                    break;

            }
            DataBase.migrateDatabase(DataBase.getConnection());

        } catch (SQLException e) {
            getLogger().severe("Error loading database");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // MainCurrency
        if(Configuration.MULTIWORLD){
            for(World world : this.getServer().getWorlds()){
                try {
                    DataBase.updateWorld(world);
                    Assets.generateAssets();
                    Accounts.registerDefaultAccount("server");
                    Accounts.changeEntityDelivery("server", new Location(Profitable.getInstance().getServer().getWorlds().getFirst(), 0, 0 ,0));
                    Accounts.changeItemDelivery("server", new Location(Profitable.getInstance().getServer().getWorlds().getFirst(), 0, 0 ,0));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            getLogger().info("Using per-world data");
        }else{
            Assets.generateAssets();
            Accounts.registerDefaultAccount("server");
            Accounts.changeEntityDelivery("server", new Location(Profitable.getInstance().getServer().getWorlds().getFirst(), 0, 0 ,0));
            Accounts.changeItemDelivery("server", new Location(Profitable.getInstance().getServer().getWorlds().getFirst(), 0, 0 ,0));
            getLogger().info("Using single server-wide data");
        }

        getLogger().info("Using " + Configuration.MAINCURRENCYASSET.getCode() + " as main currency on the exchange");

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
        try {
            DataBase.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(UUID playerid:TemporalItems.holdingTemp.keySet()){

            Player player = getServer().getPlayer(playerid);
            if(player != null){
                TemporalItems.removeTempItem(player);
            }
        }
    }

    public void checkForUpdate(Profitable plugin) {
        try {
            String projectSlug = "profitable";
            String url = "https://api.modrinth.com/v2/project/" + projectSlug + "/version";

            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            String json = jsonBuilder.toString();
            Pattern pattern = Pattern.compile("\"version_number\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                String latestVersion = matcher.group(1).replace("v", "");
                String currentVersion = plugin.getDescription().getVersion();

                if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                    plugin.getLogger().warning("UPDATE AVAILABLE! Latest: " + latestVersion);
                    plugin.getLogger().info("Download latest here: https://modrinth.com/plugin/profitable");
                } else {
                    plugin.getLogger().info("Up to date!");
                }
            } else {
                plugin.getLogger().warning("Could not parse version from Modrinth API response.");
            }

        } catch (Exception ex) {
            plugin.getLogger().warning("Could not check for updates: " + ex.getMessage());
        }
    }

}
