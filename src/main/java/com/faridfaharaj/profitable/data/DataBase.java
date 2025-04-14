package com.faridfaharaj.profitable.data;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.util.MessagingUtil;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataBase {

    private static Connection connection;
    private static byte[] currentWorldid;

    public static void connectMySQL() throws SQLException{

        String  host = Profitable.getInstance().getConfig().getString("database.mysql.host"),
                port = Profitable.getInstance().getConfig().getString("database.mysql.port"),
                database = Profitable.getInstance().getConfig().getString("database.mysql.database"),
                options = Profitable.getInstance().getConfig().getString("database.mysql.options"),

                username = Profitable.getInstance().getConfig().getString("database.mysql.username"),
                password = Profitable.getInstance().getConfig().getString("database.mysql.password");

        String link = "jdbc:mysql://" + host + ":" + port + "/" + database + options;

        connection = DriverManager.getConnection(
                link,
                username,
                password
        );

    }

    public static void connectSQLite() throws SQLException {

        String data = "jdbc:sqlite:" + Profitable.getInstance().getDataFolder().getAbsolutePath() + "/Data.db";
        connection = DriverManager.getConnection(data);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static void closeConnection() throws SQLException {
        connection.close();
    }

    // Get the current database connection
    public static Connection getConnection() {
        return connection;
    }

    // Get the current world id
    public static byte[] getCurrentWorld() {
        return currentWorldid;
    }

    //update world
    public static void updateWorld(World world) throws IOException {
        currentWorldid = MessagingUtil.UUIDtoBytes(world.getUID());
    }

    public static void universalUpdateWorld(CommandSender sender) {
        World world;
        if(sender instanceof Player player){
            world = player.getWorld();
        }else {
            world = Profitable.getInstance().getServer().getWorlds().getFirst();
        }

        try {
            updateWorld(world);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void migrateDatabase(Connection connection) throws SQLException, IOException {
        Profitable.getInstance().getLogger().info("Migrating database...");

        int currentVersion;
        Statement stmt = connection.createStatement();

        String tables = "CREATE TABLE IF NOT EXISTS profitable_database_version(version INT NOT NULL PRIMARY KEY);";
        stmt.execute(tables);

        ResultSet rs = stmt.executeQuery("SELECT version FROM profitable_database_version");
        currentVersion = rs.next() ? rs.getInt("version") : 0;

        List<String> migrationFiles = getMigrationFiles().stream()
                .sorted().toList();

        if(migrationFiles.size() == currentVersion){
            Profitable.getInstance().getLogger().info("No migration needed!");
            return;
        }

        for(int i = currentVersion; i < migrationFiles.size(); i++){
            String file = migrationFiles.get(i);
            String[] sqls = new String(Profitable.getInstance().getResource(file).readAllBytes()).split(";");

            for(String sql: sqls){
                stmt.execute(sql);
            }

            String version = file.substring(file.indexOf("V")+1, file.indexOf("__"));
            stmt.execute("INSERT INTO profitable_database_version (version) VALUES (" + version + ")");
        }

        Profitable.getInstance().getLogger().info("Migrated database successfully!");

    }

    public static List<String> getMigrationFiles(){
        List<String> files = new ArrayList<>();
        try {
            CodeSource src = Profitable.getInstance().getClass().getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.startsWith("db/migration") && name.endsWith(".sql") && !entry.isDirectory()) {
                            files.add(name);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }
}
