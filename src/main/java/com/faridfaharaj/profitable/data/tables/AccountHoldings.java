package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountHoldings {

    public static void createHolding(String account, String asset, double quantity) {
        String sql = "INSERT INTO assets (asset_id) VALUES (?) " +
                "ON CONFLICT (asset_id) DO NOTHING; " +
                "INSERT INTO account_assets (account_name, asset_id, quantity) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (account_name, asset_id) DO UPDATE " +
                "SET quantity = excluded.quantity;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.setString(2, account);
            stmt.setString(3, asset);
            stmt.setDouble(4, quantity);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean setHolding(String account, String asset, double quantity) {
        String sql = "INSERT INTO account_assets (account_name, asset_id, quantity) VALUES (?, ?, ?) ON CONFLICT(account_name, asset_id) DO UPDATE SET quantity = excluded.quantity;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, account);
            stmt.setString(2, asset);
            stmt.setDouble(3, quantity);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static double getAccountAssetBalance(String account, String asset) {
        String sql = "SELECT * FROM account_assets WHERE account_name = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, account);
            stmt.setString(2, asset);

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
        String sql = "SELECT pa.asset_id, pa.quantity, a.meta FROM account_assets pa JOIN assets a ON pa.asset_id = a.asset_id WHERE pa.account_name = ? AND a.asset_type = ?;";

        Component component = Component.text("Currencies:");
        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, account);
            stmt.setInt(2, assetType);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String asset = rs.getString("asset_id");
                    double quantity = rs.getDouble("quantity");
                    byte[] meta = rs.getBytes("meta");

                    component = component.appendNewline().append(Asset.holdingToChat(asset, quantity, meta));
                }

                if(component.children().isEmpty()){
                    component = Component.text("Empty").color(Configuration.COLOREMPTY);
                }
            } catch (IOException e) {
                e.printStackTrace();
                component = Component.text("ERROR").color(Configuration.COLORERROR);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return component;
    }

}
