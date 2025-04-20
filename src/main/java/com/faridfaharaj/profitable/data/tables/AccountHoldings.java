package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountHoldings {

    public static boolean setHolding(String account, String asset, double quantity) {
        String sql = "INSERT INTO account_assets (world , account_name, asset_id, quantity) VALUES (?, ?, ?, ?) " + (Profitable.getInstance().getConfig().getInt("database.database-type") == 0? "ON CONFLICT(world, account_name, asset_id) DO UPDATE SET quantity = excluded.quantity;" : "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);");

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, account);
            stmt.setString(3, asset);
            stmt.setDouble(4, quantity);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static double getAccountAssetBalance(String account, String asset) {
        String sql = "SELECT * FROM account_assets WHERE world = ? AND account_name = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, account);
            stmt.setString(3, asset);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("quantity");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static Component AssetBalancesToString(String account, int assetType) {
        String sql = "SELECT pa.asset_id, pa.quantity, a.meta FROM account_assets pa JOIN assets a ON pa.asset_id = a.asset_id AND pa.world = a.world WHERE pa.world = ? AND pa.account_name = ? AND a.asset_type = ?;";

        Component component = Component.text("Currencies:");
        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, account);
            stmt.setInt(3, assetType);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String assetCode = rs.getString("asset_id");
                    byte[] meta = rs.getBytes("meta");

                    Asset asset = Asset.assetFromMeta(assetCode, assetType, meta);

                    double quantity = rs.getDouble("quantity");
                    component = component.appendNewline().append(MessagingUtil.assetAmmount(asset, quantity));
                }

                if(component.children().isEmpty()){
                    component = Component.text("Empty").color(Configuration.COLOREMPTY);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return component;
    }

}
