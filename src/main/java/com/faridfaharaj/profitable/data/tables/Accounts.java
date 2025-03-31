package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.util.TextUtil;
import com.faridfaharaj.profitable.util.RandomUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
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
            TextUtil.sendSuccsess(player, "Logged into default account");
            return uuidString;
        }

        );

    }

    public static boolean registerAccount(String name, String password) {
        String sql = "INSERT INTO accounts (account_name, password, salt, item_delivery_pos, entity_delivery_pos, entity_claim_id) VALUES (?, ?, ?, ?, ?, (SELECT COALESCE(MAX(entity_claim_id), 99) + 1 FROM accounts))";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

            Map.Entry<byte[], byte[]> hashedpassword = hashPassword(password);

            stmt.setString(1, name);
            stmt.setBytes(2, hashedpassword.getKey());
            stmt.setBytes(3, hashedpassword.getValue());

            stmt.setObject(4, null, Types.BLOB);
            stmt.setObject(5, null, Types.BLOB);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean registerDefaultAccount(String name) {
        String sql = "INSERT OR IGNORE INTO accounts (account_name, password, salt, item_delivery_pos, entity_delivery_pos, entity_claim_id) VALUES (?, ?, ?, ?, ?, (SELECT COALESCE(MAX(entity_claim_id), 99) + 1 FROM accounts))";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, RandomUtil.RANDOM.nextInt(10000)+"sd");
            stmt.setString(3, RandomUtil.RANDOM.nextInt(10000)+"sd");

            stmt.setObject(4, null, Types.BLOB);
            stmt.setObject(5, null, Types.BLOB);

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

    public static Map.Entry<byte[], byte[]> hashPassword(String password) {
        int iterations = 10000;
        int keyLength = 128;
        char[] chars = password.toCharArray();

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        try {
            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Map.entry(hash, salt);
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

    public static Map.Entry<byte[], byte[]> getPasswordHash(String name){

        String sql = "SELECT * FROM accounts WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, name);

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
        String sql = "UPDATE accounts SET password = ?, salt = ? WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

            Map.Entry<byte[], byte[]> hashedpassword = hashPassword(password);

            stmt.setBytes(1, hashedpassword.getKey());
            stmt.setBytes(2, hashedpassword.getValue());

            stmt.setString(3, name);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean changeItemDelivery(String name, Location location) {

        String sql = "UPDATE accounts SET item_delivery_pos = ? WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

            stmt.setBytes(1, encodeLocation(location));

            stmt.setString(2, name);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static boolean changeEntityDelivery(String name, Location location) {
        String sql = "UPDATE accounts SET entity_delivery_pos = ? WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

            stmt.setBytes(1, encodeLocation(location));

            stmt.setString(2, name);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static boolean isItemDeliveryNull(String account) {


        String sql = "SELECT 1 FROM accounts WHERE account_name = ? AND item_delivery_pos IS NULL LIMIT 1";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

            stmt.setString(1, account);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public static boolean isEntityDeliveryNull(String account) {


        String sql = "SELECT 1 FROM accounts WHERE account_name = ? AND entity_delivery_pos IS NULL LIMIT 1";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

            stmt.setString(1, account);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public static String getEntityClaimId(String name) {
        String sql = "SELECT * FROM accounts WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    return ("§eE "+rs.getInt("entity_claim_id"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "null";
    }

    public static Location getItemDelivery(String name) {
        String sql = "SELECT * FROM accounts WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, name);

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
        String sql = "SELECT * FROM accounts WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, name);

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
        String sql = "DELETE FROM accounts WHERE account_name = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            int affected = stmt.executeUpdate();

            return 0 < affected;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] encodeLocation(Location location) throws IOException {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            dos.writeUTF(location.getWorld().getName());
            dos.writeDouble(location.getX());
            dos.writeDouble(location.getY());
            dos.writeDouble(location.getZ());

            return bos.toByteArray();
        }

    }

    public static Location decodeLocation(byte[] locationBytes) throws IOException {


        if(locationBytes == null){
            return null;
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(locationBytes);
             DataInputStream dis = new DataInputStream(bis)) {

            World world = Profitable.getInstance().getServer().getWorld(dis.readUTF());
            return new Location(world, dis.readDouble(), dis.readDouble(), dis.readDouble());

        }

    }

}
