package com.cappuccino.profitable.data.tables;

import com.cappuccino.profitable.data.DataBase;
import com.cappuccino.profitable.data.holderClasses.Asset;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    public static void setHolding(String account, String asset, double quantity) {
        String sql = "INSERT INTO account_assets (account_name, asset_id, quantity) VALUES (?, ?, ?) ON CONFLICT(account_name, asset_id) DO UPDATE SET quantity = excluded.quantity;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, account);
            stmt.setString(2, asset);
            stmt.setDouble(3, quantity);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public static List<String> AssetBalancesToString(String account, int assetType) {
        String sql = "SELECT pa.asset_id, pa.quantity, a.meta FROM account_assets pa JOIN assets a ON pa.asset_id = a.asset_id WHERE pa.account_name = ? AND a.asset_type = ?;";

        List<String> balancesStrings = new ArrayList<>();

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, account);
            stmt.setInt(2, assetType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String asset = rs.getString("asset_id");
                    double quantity = rs.getDouble("quantity");
                    byte[] meta = rs.getBytes("meta");

                    balancesStrings.add(Asset.holdingToChat(asset, quantity, meta));
                }
            } catch (IOException e) {
                balancesStrings.add("error");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return balancesStrings;
    }

}
