package com.faridfaharaj.profitable.data;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.util.TextUtil;
import com.faridfaharaj.profitable.util.VaultCompat;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DataBase {

    private static final Map<String, Connection> connections = new HashMap<>();
    private static Connection currentConnection;

    public static void setConnection(String worldName) throws SQLException {

        boolean multiworld = Profitable.getInstance().getConfig().getBoolean("multi-world-support");

        currentConnection = connections.computeIfAbsent(multiworld? worldName:"server_Wide",
                (wrld) -> {
                    try {

                        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Profitable.DATAPATH + "/" + wrld + ".db");
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("PRAGMA foreign_keys = ON;");
                        }
                        createTablesIfAbsent(connection);

                        //Base asset generation----

                        //Main currency
                        Assets.addAsset(connection , Configuration.MAINCURRENCYASSET.getCode(), 1, Asset.metaData(Configuration.MAINCURRENCYASSET.getColor().value(), Configuration.MAINCURRENCYASSET.getName()));
                        if(Configuration.VAULTENABLED && !Objects.equals(Configuration.MAINCURRENCYASSET.getCode(), VaultCompat.getVaultCode())){
                            Assets.addAsset(connection, VaultCompat.getVaultCode(), 1, VaultCompat.getVaultCurrencyMeta());
                        }

                        //Base commodity items
                        for(String item : Configuration.ALLOWEITEMS){
                            Assets.addAsset(connection, item, 2, Asset.metaData(Configuration.COLOREMPTY.value(), TextUtil.nameCommodity(item)));
                        }

                        //Base commodity entities
                        for(String entity : Configuration.ALLOWENTITIES){
                            Assets.addAsset(connection, entity, 3, Asset.metaData(Configuration.COLOREMPTY.value(), TextUtil.nameCommodity(entity)));
                        }

                        return connection;

                    } catch (SQLException e) {

                        Profitable.getInstance().getLogger().severe("Couldn't create connection to world: " + wrld);
                        e.printStackTrace();
                        throw new RuntimeException(e);

                    } catch (IOException e) {
                        Profitable.getInstance().getLogger().severe("Some bytes got fucked up, \nThere is a chance that an asset couldn't be created");
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static void createTablesIfAbsent(Connection conn) throws SQLException {
        List<String> TablesSQL = List.of(
                "CREATE TABLE IF NOT EXISTS assets (asset_id TEXT PRIMARY KEY, asset_type INTEGER, meta BLOB);",
                "CREATE INDEX IF NOT EXISTS idx_assets_type ON assets(asset_type)",

                "CREATE TABLE IF NOT EXISTS accounts (account_name TEXT PRIMARY KEY, password BLOB, salt BLOB, item_delivery_pos BLOB, entity_delivery_pos BLOB, entity_claim_id INTEGER);",

                "CREATE TABLE IF NOT EXISTS account_assets (account_name TEXT, asset_id TEXT, quantity DOUBLE, PRIMARY KEY (account_name, asset_id), FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE, FOREIGN KEY (account_name) REFERENCES accounts(account_name) ON DELETE CASCADE);",
                "CREATE INDEX IF NOT EXISTS idx_account_assets_account_name_asset_id ON account_assets(account_name, asset_id)",

                "CREATE TABLE IF NOT EXISTS orders (order_uuid TEXT PRIMARY KEY, owner TEXT, asset_id TEXT, sideBuy BOOLEAN, price DOUBLE, units DOUBLE, FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE, FOREIGN KEY (owner) REFERENCES accounts(account_name) ON DELETE CASCADE);",
                "CREATE INDEX IF NOT EXISTS idx_orders_asset_id_sideBuy_price ON orders(asset_id, sideBuy, price)",
                "CREATE INDEX IF NOT EXISTS idx_orders_owner ON orders(owner)",

                "CREATE TABLE IF NOT EXISTS candles_day (time INTEGER, open DOUBLE, close DOUBLE, high DOUBLE, low DOUBLE, volume DOUBLE, asset_id TEXT, PRIMARY KEY (time, asset_id), FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE);",
                "CREATE INDEX IF NOT EXISTS idx_candles_day_asset_id_time ON candles_day(asset_id, time)",

                "CREATE TABLE IF NOT EXISTS candles_week (time INTEGER, open DOUBLE, close DOUBLE, high DOUBLE, low DOUBLE, volume DOUBLE, asset_id TEXT, PRIMARY KEY (time, asset_id), FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE);",
                "CREATE INDEX IF NOT EXISTS idx_candles_week_asset_id_time ON candles_week(asset_id, time)",

                "CREATE TABLE IF NOT EXISTS candles_month (time INTEGER, open DOUBLE, close DOUBLE, high DOUBLE, low DOUBLE, volume DOUBLE, asset_id TEXT, PRIMARY KEY (time, asset_id), FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE);",
                "CREATE INDEX IF NOT EXISTS idx_candles_month_asset_id_time ON candles_month(asset_id, time)"
        );

        try (Statement stmt = conn.createStatement()) {
            for(String table : TablesSQL){
                stmt.executeUpdate(table);
            }

        } catch (SQLException e) {
            Profitable.getInstance().getLogger().severe("Error creating table: " + e.getMessage());
            throw e;
        }
    }

    public static void closeWorldConnection(String worldName) {
        boolean multiworld = Profitable.getInstance().getConfig().getBoolean("multi-world-support");
        String dbName = multiworld ? worldName : "server_Wide";

        if (connections.containsKey(dbName)) {
            try {
                connections.get(dbName).close();
                connections.remove(dbName);
                Profitable.getInstance().getLogger().info("Closed database connection for world: " + dbName);
            } catch (SQLException e) {
                Profitable.getInstance().getLogger().severe("Error closing database for world: " + dbName);
                e.printStackTrace();
            }
        }
    }

    // Close the database connection
    public static void closeAllConnections() {
        try {
            if (!connections.isEmpty()) {
                for(Connection connection: connections.values()){
                    connection.close();
                    Profitable.getInstance().getLogger().info("connection closed");
                }
                connections.clear();
                Profitable.getInstance().getLogger().info("All SQLite connections closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get the current database connection
    public static Connection getCurrentConnection() {
        return currentConnection;
    }
}
