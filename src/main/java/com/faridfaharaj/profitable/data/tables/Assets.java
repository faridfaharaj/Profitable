package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.holderClasses.assets.ComEntity;
import com.faridfaharaj.profitable.data.holderClasses.assets.ComItem;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

    public static void addAsset(Asset asset) {
        String sql = "INSERT " + (Profitable.getInstance().getConfig().getInt("database.database-type") == 0 ? "OR ": "") + "IGNORE INTO assets (world, asset_id, asset_type, meta) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset.getCode());
            stmt.setInt(3, asset.getAssetType().getValue());
            stmt.setBytes(4, asset.metaData());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateAsset(String assetID, Asset updatedAsset){
        String sql = "UPDATE assets SET asset_id = ?, meta = ? WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            stmt.setString(1,updatedAsset.getCode());
            stmt.setBytes(2, updatedAsset.metaData());
            stmt.setBytes(3, DataBase.getCurrentWorld());
            stmt.setString(4, assetID);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

                    return Asset.assetFromMeta(assetID, Asset.AssetType.fromValue(rs.getInt("asset_type")), meta);

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
            Assets.addAsset(VaultHook.getAsset());
        }
        if(PlayerPointsHook.isConnected()){
            // PlayerPoints
            Assets.addAsset(PlayerPointsHook.getAsset());
        }

        try{
            Configuration.loadMainCurrency();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(Configuration.GENERATEASSETS){
            //Base commodity items
            for(String item : Configuration.ALLOWEITEMS){
                Material material = Material.getMaterial(item);
                if(material == null){
                    continue;
                }
                Asset asset = new ComItem(item, Configuration.COLORHIGHLIGHT, NamingUtil.nameCommodity(item), new ItemStack(material));
                Assets.addAsset(asset);
            }

            //Base commodity entities
            for(String entity : Configuration.ALLOWENTITIES){
                Material material = Material.getMaterial(entity+"_SPAWN_EGG");
                if(material == null){
                    continue;
                }
                Asset asset = new ComEntity(entity, Configuration.COLORHIGHLIGHT, NamingUtil.nameCommodity(entity), new ItemStack(material));
                Assets.addAsset(asset);

            }
        }

    }

}
