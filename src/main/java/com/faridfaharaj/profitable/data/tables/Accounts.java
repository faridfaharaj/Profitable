package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.util.MessagingUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class Accounts {

    private static HashMap<UUID, String> currentAccounts = new HashMap<>();

    public static HashMap getCurrentAccounts(){
        return  currentAccounts;
    }

    public static String getAccount(Player player){

        return currentAccounts.computeIfAbsent(player.getUniqueId(),
                k -> {
            String uuidString = k.toString();
            registerDefaultAccount(uuidString);
            MessagingUtil.sendSuccsess(player, "Logged into default account");
            return uuidString;
        }

        );

    }

    public static int nextClaimID(){

        String sql = "SELECT COALESCE(MAX(entity_claim_id), 99) + 1 FROM accounts;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;

    }

    public static boolean registerAccount(String name, String password) {
        String sql = "INSERT INTO accounts (world ,account_name, password, salt, item_delivery_pos, entity_delivery_pos, entity_claim_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        int claimid = nextClaimID();

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            byte[][] hashedpassword = hashPassword(password);

            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, name);
            stmt.setBytes(3, hashedpassword[0]);
            stmt.setBytes(4, hashedpassword[1]);

            stmt.setObject(5, null, Types.BINARY);
            stmt.setObject(6, null, Types.BINARY);

            stmt.setInt(7, claimid);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean registerDefaultAccount(String name) {
        String sql = "INSERT " + (Profitable.getInstance().getConfig().getInt("database.database-type") == 0 ? "OR ": "") + "IGNORE INTO accounts (world, account_name, password, salt, item_delivery_pos, entity_delivery_pos, entity_claim_id) VALUES (? ,?, ?, ?, ?, ?, ?)";

        int claimid = nextClaimID();

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, name);
            stmt.setString(3, "un-passwordable");
            stmt.setString(4, "password1234");

            stmt.setObject(5, null, Types.BLOB);
            stmt.setObject(6, null, Types.BLOB);

            stmt.setInt(7, claimid);

            if(stmt.executeUpdate() > 0){
                double initialBalance = Profitable.getInstance().getConfig().getDouble("main-currency.initial-balance");
                if(initialBalance > 0){
                    AccountHoldings.setHolding(name, Configuration.MAINCURRENCYASSET.getCode(), initialBalance);
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Map.Entry<byte[], byte[]> getPasswordHash(String name){

        String sql = "SELECT * FROM accounts WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] pasword = rs.getBytes("password");
                    byte[] salt = rs.getBytes("salt");
                    if(pasword == null || salt == null){
                        return null;
                    }

                    return Map.entry(pasword, salt);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static boolean changePassword(String name, String password) {
        String sql = "UPDATE accounts SET password = ?, salt = ? WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            byte[][] hashedpassword = hashPassword(password);

            stmt.setBytes(1, hashedpassword[0]);
            stmt.setBytes(2, hashedpassword[1]);

            stmt.setBytes(3, DataBase.getCurrentWorld());
            stmt.setString(4, name);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean changeItemDelivery(String name, Location location) {

        String sql = "UPDATE accounts SET item_delivery_pos = ? WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            stmt.setBytes(1, encodeLocation(location));

            stmt.setBytes(2, DataBase.getCurrentWorld());
            stmt.setString(3, name);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static boolean changeEntityDelivery(String name, Location location) {
        String sql = "UPDATE accounts SET entity_delivery_pos = ? WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            stmt.setBytes(1, encodeLocation(location));

            stmt.setBytes(2, DataBase.getCurrentWorld());
            stmt.setString(3, name);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static String getEntityClaimId(String name) {
        String sql = "SELECT * FROM accounts WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    return ("§eE "+rs.getInt("entity_claim_id"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Location getItemDelivery(String name) {
        String sql = "SELECT * FROM accounts WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] itemDeliveryPos = rs.getBytes("item_delivery_pos");

                    if (!rs.wasNull()) {

                        return decodeLocation(itemDeliveryPos);

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Location getEntityDelivery(String name) {
        String sql = "SELECT * FROM accounts WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] itemDeliveryPos = rs.getBytes("entity_delivery_pos");

                    if (!rs.wasNull()) {

                        return decodeLocation(itemDeliveryPos);

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean deleteAccount(String asset) {
        String sql = "DELETE FROM accounts WHERE world = ? AND account_name = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            int affected = stmt.executeUpdate();

            return 0 < affected;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void logOut(UUID playerid){
        currentAccounts.remove(playerid);
    }

    public static boolean logIn(UUID playerid, String name, String password){

        if(comparePasswords(name, password)){
            currentAccounts.put(playerid, name);
            return true;
        }

        return false;
    }

    public static boolean comparePasswords(String name, String password){
        Map.Entry<byte[], byte[]> hashedpassword  = getPasswordHash(name);

        if(hashedpassword == null){
            return false;
        }

        byte[] comparedHash = hashPassword(password, hashedpassword.getValue());

        if (hashedpassword.getKey().length != comparedHash.length) {
            return false;
        }

        for (int i = 0; i < hashedpassword.getKey().length; i++) {
            if (hashedpassword.getKey()[i] != comparedHash[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[][] hashPassword(String password) {
        int iterations = 10000;
        int keyLength = 128;
        char[] chars = password.toCharArray();

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        try {
            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();

            byte[][] hashes = new byte[2][];

            hashes[0] = hash;
            hashes[1] = salt;

            return hashes;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static byte[] hashPassword(String password, byte[] salt) {
        int iterations = 10000;
        int keyLength = 128;
        char[] chars = password.toCharArray();

        try {
            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static byte[] encodeLocation(Location location) throws IOException {
        UUID worlduid = location.getWorld().getUID();
        ByteBuffer buffer = ByteBuffer.allocate(40); // 16 bytes for UUID + 3 doubles (8 bytes each)

        buffer.putLong(worlduid.getMostSignificantBits());
        buffer.putLong(worlduid.getLeastSignificantBits());
        buffer.putDouble(location.getX());
        buffer.putDouble(location.getY());
        buffer.putDouble(location.getZ());

        return buffer.array();
    }

    public static Location decodeLocation(byte[] locationBytes) throws IOException {


        if(locationBytes == null){
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(locationBytes);

        World world = Profitable.getInstance().getServer().getWorld(new UUID(buffer.getLong(),buffer.getLong()));

        if(world == null){
            return null;
        }

        return new Location(world, buffer.getDouble(), buffer.getDouble(), buffer.getDouble());

    }

}
