package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.holderClasses.assets.ComEntity;
import com.faridfaharaj.profitable.data.holderClasses.assets.ComItem;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.World;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Assets {

    public static boolean registerAsset(World world, Asset asset) {

        String sql = "INSERT INTO assets (world, asset_id, asset_type, meta) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
            stmt.setString(2, asset.getCode());
            stmt.setInt(3, asset.getAssetType().getValue());
            stmt.setBytes(4, asset.metaData());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static void addAsset(World world, Asset asset) {
        String sql = "INSERT " + (Profitable.getInstance().getConfig().getInt("database.database-type") == 0 ? "OR ": "") + "IGNORE INTO assets (world, asset_id, asset_type, meta) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
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

    public static boolean updateAsset(World world, String assetID, Asset updatedAsset){
        String sql = "UPDATE assets SET asset_id = ?, meta = ? WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {

            stmt.setString(1,updatedAsset.getCode());
            stmt.setBytes(2, updatedAsset.metaData());
            stmt.setBytes(3, MessagingUtil.getWorldId(world));
            stmt.setString(4, assetID);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static Asset getAssetData(World world, String assetID) {
        String sql = "SELECT * FROM assets WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
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

    public static Collection<String> getAssetCodeType(World world, int type) {
        String sql = "SELECT * FROM assets WHERE world = ? AND asset_type = ?;";

        Collection<String> assetsFound = new ArrayList<>();
        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
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

    public static Collection<String> getAll(World world) {
        String sql = "SELECT asset_id FROM assets WHERE world = ?;";

        Collection<String> assetsFound = new ArrayList<>();
        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));

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

    public static boolean deleteAsset(World world, String asset) {
        String sql = "DELETE FROM assets WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
            stmt.setString(2, asset);
            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void generateAssets(World world){

        //Hooks asset generation----
        if(VaultHook.isConnected()){
            // Vault
            Assets.addAsset(world,VaultHook.getAsset());
        }
        if(PlayerPointsHook.isConnected()){
            // PlayerPoints
            Assets.addAsset(world,PlayerPointsHook.getAsset());
        }

        try{
            Configuration.loadMainCurrency(world);
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
                Assets.addAsset(world,asset);
            }

            //Base commodity entities
            for(String entity : Configuration.ALLOWENTITIES){
                Material material = Material.getMaterial(entity+"_SPAWN_EGG");
                if(material == null){
                    continue;
                }
                Asset asset = new ComEntity(entity, Configuration.COLORHIGHLIGHT, NamingUtil.nameCommodity(entity), new ItemStack(material));
                Assets.addAsset(world,asset);

            }
        }

    }

}
