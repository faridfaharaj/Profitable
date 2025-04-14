package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Assets {

    public static boolean registerAsset(String symbol, int assetType, byte[] meta) {

        if(Objects.equals(symbol, VaultHook.getAsset().getCode())){
            return false;
        }

        String sql = "INSERT INTO assets (world, asset_id, asset_type, meta) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, symbol);
            stmt.setInt(3, assetType);
            stmt.setBytes(4, meta);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void addAsset(String ticker, int assetType, byte[] meta) {
        String sql = "INSERT " + (Profitable.getInstance().getConfig().getInt("database.database-type") == 0 ? "OR ": "") + "IGNORE INTO assets (world, asset_id, asset_type, meta) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, ticker);
            stmt.setInt(3, assetType);
            stmt.setBytes(4, meta);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateAsset(String assetID, Asset updatedAsset){
        String sql = "UPDATE assets SET asset_id = ?, meta = ? WHERE world = ? AND asset_id = ?;";
        System.out.println("updateasset");

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            System.out.println("dsoasod");

            stmt.setString(1,updatedAsset.getCode());
            stmt.setBytes(2, Asset.metaData(updatedAsset));
            stmt.setBytes(3, DataBase.getCurrentWorld());
            stmt.setString(4, assetID);

            System.out.println("Updating asset: " + assetID + " to " + updatedAsset.getCode());
            System.out.println("World: " + Arrays.toString(DataBase.getCurrentWorld()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("error");
        return false;
    }

    public static Asset getAssetData(String assetID) {
        String sql = "SELECT * FROM assets WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, assetID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()){

                    byte[] meta = rs.getBytes("meta");
                    TextColor color;
                    String name;

                    List<String> stringList = new ArrayList<>();
                    List<Double> numericList = new ArrayList<>();

                    try (ByteArrayInputStream bis = new ByteArrayInputStream(meta);
                         DataInputStream dis = new DataInputStream(bis)) {

                        color = TextColor.color(dis.readInt());
                        name = dis.readUTF();


                        int lengthStrings = dis.readInt();
                        if(lengthStrings > 0){
                            for(int i = 0; i<lengthStrings; i++){
                                stringList.add(dis.readUTF());
                            }
                        }

                        int lengthNumeric = dis.readInt();
                        if(lengthNumeric > 0){
                            for(int i = 0; i<lengthNumeric; i++){
                                numericList.add(dis.readDouble());
                            }
                        }


                    } catch (IOException e) {
                        color = NamedTextColor.WHITE;
                        name = assetID.toLowerCase();
                    }

                    return new Asset(assetID, rs.getInt("asset_type"), color, name, stringList, numericList);

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Collection<String> getAssetCodeType(int type) {
        String sql = "SELECT * FROM assets WHERE world = ? AND asset_type = ?;";

        Collection<String> assetsFound = new ArrayList<>();
        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setInt(2, type);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()){
                    assetsFound.add(rs.getString("asset_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assetsFound;
    }

    public static List<Asset> getAssetFancyType(int type) {
        String sql = "SELECT * FROM assets WHERE world = ? AND asset_type = ?;";

        List<Asset> assetsFound = new ArrayList<>();
        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setInt(2, type);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()){
                    assetsFound.add(
                        Asset.assetFromMeta(rs.getString("asset_id"), rs.getInt("asset_type"), rs.getBytes("meta"))
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assetsFound;
    }

    public static Collection<String> getAll() {
        String sql = "SELECT asset_id FROM assets WHERE world = ?;";

        Collection<String> assetsFound = new ArrayList<>();
        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()){
                    assetsFound.add(rs.getString("asset_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assetsFound;
    }

    public static boolean deleteAsset(String asset) {
        String sql = "DELETE FROM assets WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void generateAssets(){

        //Hooks asset generation----
        if(VaultHook.isConnected()){
            // Vault
            try {
                Assets.addAsset(VaultHook.getAsset().getCode(), 1, Asset.metaData(VaultHook.getAsset()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(PlayerPointsHook.isConnected()){
            // PlayerPoints
            try {
                Assets.addAsset(PlayerPointsHook.getAsset().getCode(), 1, Asset.metaData(PlayerPointsHook.getAsset()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try{
            Configuration.loadMainCurrency();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(Configuration.GENERATEASSETS){
            //Base commodity items
            for(String item : Configuration.ALLOWEITEMS){
                try {
                    Assets.addAsset(item, 2, Asset.metaData(Configuration.COLOREMPTY.value(), NamingUtil.nameCommodity(item)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            //Base commodity entities
            for(String entity : Configuration.ALLOWENTITIES){
                try {
                    Assets.addAsset(entity, 3, Asset.metaData(Configuration.COLOREMPTY.value(), NamingUtil.nameCommodity(entity)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}
