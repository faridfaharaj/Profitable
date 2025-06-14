package com.faridfaharaj.profitable.data;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.util.MessagingUtil;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataBase {

    private static Connection connection;

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

        // TEMPORAL ######
        try{
            Files.move(Paths.get(Profitable.getInstance().getDataFolder().getAbsolutePath()+"/data/server_Wide.db"), Paths.get(Profitable.getInstance().getDataFolder().getAbsolutePath() + "/Data.db"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
        // TEMPORAL ######

        String data = "jdbc:sqlite:" + Profitable.getInstance().getDataFolder().getAbsolutePath() + "/Data.db";
        connection = DriverManager.getConnection(data);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA threadsafety=1;");
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
    /*public static byte[] getCurrentWorld() {
        return currentWorldid;
    }

     */

    public static void migrateDatabase(Connection connection) throws IOException {
        Profitable.getInstance().getLogger().info("Migrating database...");

        int currentVersion = 0;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS profitable_database_version(version INT NOT NULL PRIMARY KEY);");
        }catch (SQLException e){
            e.printStackTrace();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version FROM profitable_database_version")) {
            currentVersion = rs.next() ? rs.getInt("version") : 0;
        }catch (SQLException e){
            e.printStackTrace();
        }

        List<String> migrationFiles = getMigrationFiles().stream()
                .sorted().toList();

        if(migrationFiles.size() == currentVersion){
            Profitable.getInstance().getLogger().info("No migration needed!");
            return;
        }

        // vvvv ##################################### TEMPORAL ##################################### vvvv
        boolean isOld = false;
        try (ResultSet tables = connection.getMetaData().getTables(null, null, "assets", new String[]{"TABLE"})) {
            isOld = tables.next();
        } catch (SQLException ignored) {}

        if (isOld) {
            String temporalSql = new String(Profitable.getInstance().getResource("db/migration/temporal_migration.tmp").readAllBytes());
            String[] sqls = temporalSql.split(";");

            try (Statement stmt = connection.createStatement()) {
                for (String sql : sqls) {
                    if (!sql.isBlank()) stmt.execute(sql.trim());
                }

                stmt.execute("INSERT INTO profitable_database_version (version) VALUES (1)");
            }catch (SQLException e){
                e.printStackTrace();
            }

            Profitable.getInstance().getLogger().info("Partially Migrated Pre-0.2.0 database");
            Profitable.getInstance().getLogger().warning("ORDERS and PER-WORLD DATA weren't migrated!");
            return;
        }
        // ^^^^ ##################################### TEMPORAL ##################################### ^^^^

        for (int i = currentVersion; i < migrationFiles.size(); i++) {
            String file = migrationFiles.get(i);
            String fileContent = new String(Profitable.getInstance().getResource(file).readAllBytes());
            String[] sqls = fileContent.split(";");

            try (Statement stmt = connection.createStatement()) {
                for (String sql : sqls) {
                    if (!sql.isBlank()) stmt.execute(sql.trim());
                }

                String version = file.substring(file.indexOf("V") + 1, file.indexOf("__"));
                stmt.execute("INSERT INTO profitable_database_version (version) VALUES (" + version + ")");
            }catch (SQLException ignored){

            }
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
