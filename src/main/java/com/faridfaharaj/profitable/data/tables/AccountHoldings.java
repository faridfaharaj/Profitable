package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetButtonData;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public static void deleteHolding(String account, String asset) {
        String sql = "DELETE FROM account_assets WHERE world = ? AND account_name = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, account);
            stmt.setString(3, asset);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

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

    public static Component AssetBalancesToString(String account) {
        String sql = "SELECT aa.asset_id, a.asset_type, aa.quantity, a.meta, " +
                "IFNULL(c.close, 0) AS price, " +
                "(aa.quantity * IFNULL(c.close, 0)) AS value " +
                "FROM account_assets aa " +
                "JOIN assets a ON aa.world = a.world AND aa.asset_id = a.asset_id " +
                "LEFT JOIN candles_day c ON aa.world = c.world AND aa.asset_id = c.asset_id " +
                "AND c.time = (SELECT MAX(time) FROM candles_day WHERE world = aa.world AND asset_id = aa.asset_id) " +
                "WHERE aa.world = ? AND aa.account_name = ? " +
                "ORDER BY a.asset_type";

        Component component = Component.text("Currency:");
        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, account);

            try (ResultSet rs = stmt.executeQuery()) {

                double totalValue = 0;
                int type = 1;
                while (rs.next()) {
                    String assetCode = rs.getString("asset_id");
                    byte[] meta = rs.getBytes("meta");
                    int iteratedType = rs.getInt("asset_type");
                    if(type != iteratedType){
                        type = iteratedType;
                        component = component.appendNewline().appendNewline().append(Component.text(NamingUtil.nameType(iteratedType)+ ":"));
                    }

                    Asset asset = Asset.assetFromMeta(assetCode, iteratedType, meta);

                    double quantity = rs.getDouble("quantity");
                    component = component.appendNewline().append(MessagingUtil.assetAmmount(asset, quantity));

                    if(!Objects.equals(assetCode, Configuration.MAINCURRENCYASSET.getCode())){
                        totalValue += rs.getDouble("value");
                    }else {
                        totalValue += quantity;
                    }
                }

                if(totalValue == 0){
                    component = Component.text("Empty").color(Configuration.COLOREMPTY);
                }else{
                    component = component.appendNewline().appendNewline().append(Component.text("Portfolio Value: ")).append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, totalValue));
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return component;
    }

    public static List<AssetButtonData> AssetBalancesToAssetData(String account) {
        String sql = "SELECT aa.asset_id, a.asset_type, aa.quantity, a.meta, " +
                "IFNULL(c.close, 0) AS price, " +
                "(aa.quantity * IFNULL(c.close, 0)) AS value " +
                "FROM account_assets aa " +
                "JOIN assets a ON aa.world = a.world AND aa.asset_id = a.asset_id " +
                "LEFT JOIN candles_day c ON aa.world = c.world AND aa.asset_id = c.asset_id " +
                "AND c.time = (SELECT MAX(time) FROM candles_day WHERE world = aa.world AND asset_id = aa.asset_id) " +
                "WHERE aa.world = ? AND aa.account_name = ? " +
                "ORDER BY a.asset_type";

        List<AssetButtonData> balances = new ArrayList<>();

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, account);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String assetCode = rs.getString("asset_id");
                    byte[] meta = rs.getBytes("meta");
                    int iteratedType = rs.getInt("asset_type");

                    Asset asset = Asset.assetFromMeta(assetCode, iteratedType, meta);

                    double quantity = rs.getDouble("quantity");

                    double value;
                    if(!Objects.equals(assetCode, Configuration.MAINCURRENCYASSET.getCode())){
                        value = rs.getDouble("value");
                    }else {
                        value = 1;
                    }

                    balances.add(new AssetButtonData(asset, new Candle(0, value, 0,0, quantity)));
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return balances;
    }

}
