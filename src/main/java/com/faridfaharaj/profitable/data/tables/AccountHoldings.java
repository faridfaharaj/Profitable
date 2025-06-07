package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccountHoldings {

    public static boolean setHolding(World world, String account, String asset, double quantity) {
        String sql = "INSERT INTO account_assets (world , account_name, asset_id, quantity) VALUES (?, ?, ?, ?) " + (Profitable.getInstance().getConfig().getInt("database.database-type") == 0? "ON CONFLICT(world, account_name, asset_id) DO UPDATE SET quantity = excluded.quantity;" : "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);");

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
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

    public static void deleteHolding(World world,String account, String asset) {
        String sql = "DELETE FROM account_assets WHERE world = ? AND account_name = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
            stmt.setString(2, account);
            stmt.setString(3, asset);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static double getAccountAssetBalance(World world,String account, String asset) {
        String sql = "SELECT * FROM account_assets WHERE world = ? AND account_name = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
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

    public static List<AssetCache> AssetBalancesToAssetData(World world,String account) {
        String sql = "SELECT aa.asset_id, a.asset_type, aa.quantity, a.meta, " +
                "IFNULL(c.close, 0) AS price, " +
                "(aa.quantity * IFNULL(c.close, 0)) AS value " +
                "FROM account_assets aa " +
                "JOIN assets a ON aa.world = a.world AND aa.asset_id = a.asset_id " +
                "LEFT JOIN candles_day c ON aa.world = c.world AND aa.asset_id = c.asset_id " +
                "AND c.time = (SELECT MAX(time) FROM candles_day WHERE world = aa.world AND asset_id = aa.asset_id) " +
                "WHERE aa.world = ? AND aa.account_name = ? " +
                "ORDER BY a.asset_type";

        List<AssetCache> balances = new ArrayList<>();

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, MessagingUtil.getWorldId(world));
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
                        balances.addFirst(new AssetCache(asset, new Candle(0, 1, 0,0, quantity)));
                        continue;
                    }

                    balances.add(new AssetCache(asset, new Candle(0, value, 0,0, quantity)));
                }

                if(balances.isEmpty() || !Objects.equals(balances.getFirst().getAsset().getCode(), Configuration.MAINCURRENCYASSET.getCode())){
                    balances.addFirst(new AssetCache(Configuration.MAINCURRENCYASSET, new Candle(0, 1, 0,0, 0)));
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return balances;
    }

}
