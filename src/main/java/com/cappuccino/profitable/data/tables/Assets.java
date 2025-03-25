package com.cappuccino.profitable.data.tables;

import com.cappuccino.profitable.data.DataBase;
import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.util.VaultCompat;
import org.bukkit.ChatColor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Assets {

    public static boolean registerAsset(String ticker, int assetType, byte[] meta) {

        if(Objects.equals(ticker, VaultCompat.getVaultCode())){
            return false;
        }

        String sql = "INSERT INTO assets (asset_id, asset_type, meta) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, ticker);
            stmt.setInt(2, assetType);
            stmt.setBytes(3, meta);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void addAsset(Connection connection, String ticker, int assetType, byte[] meta) {
        String sql = "INSERT OR IGNORE INTO assets (asset_id, asset_type, meta) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ticker);
            stmt.setInt(2, assetType);
            stmt.setBytes(3, meta);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Asset getAssetData(String assetID) {
        String sql = "SELECT * FROM assets WHERE asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, assetID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()){

                    byte[] meta = rs.getBytes("meta");
                    ChatColor color;
                    String name;

                    List<String> stringList = new ArrayList<>();
                    List<Double> numericList = new ArrayList<>();

                    try (ByteArrayInputStream bis = new ByteArrayInputStream(meta);
                         DataInputStream dis = new DataInputStream(bis)) {

                        color = ChatColor.valueOf(dis.readUTF());
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
                        color = ChatColor.WHITE;
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

    public static Collection<String> getRegisteredAssetType(int type) {
        String sql = "SELECT * FROM assets WHERE asset_type = ?;";

        Collection<String> assetsFound = new ArrayList<>();
        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setInt(1, type);

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

    public static Collection<String> getAll() {
        String sql = "SELECT asset_id FROM assets;";

        Collection<String> assetsFound = new ArrayList<>();
        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {

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

    public static void deleteAsset(String asset) {
        String sql = "DELETE FROM assets WHERE asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
