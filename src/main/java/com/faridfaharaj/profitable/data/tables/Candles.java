package com.faridfaharaj.profitable.data.tables;


import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Candles {

    //this could be way more performant, needs fixing  <------------

    static String[] tables = {"day", "week", "month"};
    static long[] intervals = {24000, 168000, 720000};

    public static boolean updateDay(String asset, World world, double price, double volume){

        for(int i = 0; i<3; i++){

            String sqlite = "INSERT INTO candles_"+ tables[i] +" (world, time, open, close, high, low, volume, asset_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(world, time, asset_id) DO UPDATE SET" +
                    "    high = MAX(high, excluded.high)," +
                    "    low = MIN(low, excluded.low)," +
                    "    close = excluded.close, " +
                    "    volume = volume + excluded.volume;";

            String mysql = "INSERT INTO candles_" + tables[i] + " (world, time, open, close, high, low, volume, asset_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "high = GREATEST(high, VALUES(high)), " +
                    "low = LEAST(low, VALUES(low)), " +
                    "close = VALUES(close), " +
                    "volume = volume + VALUES(volume);";


            try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(Profitable.getInstance().getConfig().getInt("database.database-type") == 0? sqlite:mysql)) {
                stmt.setBytes(1, DataBase.getCurrentWorld());
                stmt.setLong(2, (world.getFullTime() / intervals[i]) * intervals[i]);
                stmt.setDouble(3,price);
                stmt.setDouble(4,price);
                stmt.setDouble(5,price);
                stmt.setDouble(6,price);
                stmt.setDouble(7,volume);
                stmt.setString(8,asset);

                stmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

        }

        return true;
    }

    public static Candle getLastDay(String asset, long time) {
        String sql = "SELECT time, open, close, high, low, volume FROM candles_day WHERE world = ? AND asset_id = ? AND time = ? " +
                "UNION ALL " +
                "SELECT time, close AS open, close, close AS high, close AS low, 0 AS volume FROM candles_day WHERE world = ? AND asset_id = ? AND time = (SELECT MAX(time) FROM candles_day WHERE world = ? AND asset_id = ?) LIMIT 1;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            stmt.setLong(3, (time / intervals[0]) * intervals[0]);
            stmt.setBytes(4, DataBase.getCurrentWorld());
            stmt.setString(5, asset);
            stmt.setBytes(6, DataBase.getCurrentWorld());
            stmt.setString(7, asset);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Candle(
                            rs.getDouble("open"),
                            rs.getDouble("close"),
                            rs.getDouble("high"),
                            rs.getDouble("low"),
                            rs.getDouble("volume")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Candle(0, 0, 0, 0, 0);
    }

    public static List<AssetCache> getAssetsNPrice(int type, long time) {
        List<AssetCache> result = new ArrayList<>();

        String sql = """
        WITH ranked_candles AS (
            SELECT
                *,
                ROW_NUMBER() OVER (
                    PARTITION BY world, asset_id
                    ORDER BY 
                        CASE WHEN time = ? THEN 0 ELSE 1 END,  -- prefer exact match
                        time DESC
                ) as rn
            FROM candles_day
            WHERE world = ?
        )
        SELECT
            a.asset_id,
            a.asset_type,
            a.meta,
            c.open,
            c.close,
            c.high,
            c.low,
            c.volume
        FROM assets a
        LEFT JOIN ranked_candles c
            ON a.world = c.world AND a.asset_id = c.asset_id AND c.rn = 1
        WHERE a.world = ? AND a.asset_type = ?;
    """;

        byte[] world = DataBase.getCurrentWorld();
        long roundedTime = (time / intervals[0]) * intervals[0];

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, roundedTime); // for ROW_NUMBER priority
            stmt.setBytes(2, world);      // for candle filtering
            stmt.setBytes(3, world);      // for final asset filter
            stmt.setInt(4, type);         // for asset_type filter

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String assetID = rs.getString("asset_id");

                    if (!Objects.equals(assetID, Configuration.MAINCURRENCYASSET.getCode())) {
                        int assetType = rs.getInt("asset_type");
                        byte[] meta = rs.getBytes("meta");

                        Asset asset = Asset.assetFromMeta(assetID, assetType, meta);

                        double open = rs.getDouble("open");
                        double close = rs.getDouble("close");
                        double high = rs.getDouble("high");
                        double low = rs.getDouble("low");
                        double volume = rs.getDouble("volume");

                        Candle candle = new Candle(open, close, high, low, volume);

                        result.add(new AssetCache(asset, candle));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<Candle> getInterval(String asset, long time, int interval) {

        String sql = "SELECT * FROM candles_" + tables[interval] + " WHERE world = ? AND asset_id = ? AND time > ? ORDER BY time ASC;";

        List<Candle> candles = new ArrayList<>();

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            stmt.setLong(3, time);

            try (ResultSet rs = stmt.executeQuery()) {

                long lastTime = -1;
                double lastPrice = 0;

                double maxPrice = 0;
                double minPrice = Double.MAX_VALUE;
                double maxVol = 0;

                while (rs.next()) {

                    long candletime = rs.getLong("time");
                    if(lastTime != -1){
                        int missingDays = (int)((candletime - lastTime) / intervals[interval]) - 1;
                        for(int i = 0 ; i < missingDays; i++){

                            candles.add(new Candle(
                                    lastPrice,
                                    lastPrice,
                                    lastPrice,
                                    lastPrice,
                                    0
                            ));

                        }
                    }

                    lastTime = candletime;
                    lastPrice = rs.getDouble("close");

                    double volume = rs.getDouble("volume");
                    double low = rs.getDouble("low");
                    double high = rs.getDouble("high");

                    maxPrice = Math.max(maxPrice, high);
                    minPrice = Math.min(minPrice, low);
                    maxVol = Math.max(maxVol, volume);

                    candles.add(new Candle(
                            rs.getDouble("open"),
                            lastPrice,
                            high,
                            low,
                            volume
                    ));

                }

                candles.add(new Candle(-1, -1, maxPrice, minPrice, maxVol));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return candles;
    }


    public static Component getHotAssets(long time, int cat) {
        String sql;

        switch (cat){
            case 0:
                //hot
                sql = "SELECT asset_id, open, close FROM candles_month WHERE world = ? AND time = ? ORDER BY ABS((close - open) / open) DESC LIMIT 8;";
                break;
            case 1:
                //performing
                sql = "SELECT asset_id, open, close FROM candles_month WHERE world = ? AND time = ? ORDER BY ((close - open) / open) DESC LIMIT 8;";
                break;
            case 2:
                //liquid
                sql = "SELECT asset_id, open, close FROM candles_month WHERE world = ? AND time = ? ORDER BY volume DESC LIMIT 8;";
                break;
            case 3:
                //biggest
                sql = "SELECT asset_id, open, close FROM candles_month WHERE world = ? AND time = ? ORDER BY close DESC LIMIT 8;";
                break;
            default:
                return Component.text("error").color(Configuration.COLORERROR);

        }

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setLong(2, (time / intervals[2]) * intervals[2]);

            Component component = Component.text("");
            try (ResultSet rs = stmt.executeQuery()) {

                for(int i = 0; i < 9; i++){
                    if(i != 0){
                        component = component.appendNewline();
                    }
                    if (rs.next()) {

                        String asset = rs.getString("asset_id");
                        double open = rs.getDouble("open");
                        double close = rs.getDouble("close");

                        double change = close-open;

                        component = component.append(
                                Component.text((i+1)+"- ")).append(Component.text("["+asset+"]", Configuration.COLORHIGHLIGHT).clickEvent(ClickEvent.runCommand("/asset "+ asset)).hoverEvent(HoverEvent.showText(Component.text("/asset "+ asset, Configuration.COLORHIGHLIGHT)))).append(Component.text("   $" + close + "   ")).append(Component.text("$"+change+"  "+ Math.ceil(change/open*10000)/100 + "% month").color(change<0? Configuration.COLORBEARISH:Configuration.COLORBULLISH));

                    }else{
                        component = component.append(Component.text((i+1)+"- -----   $--.--   $--.--  --.--% -----"));
                    }
                }

            }

            return component;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Component.text("error").color(Configuration.COLORERROR);
    }

    public static void assetDeleteAllCandles(String asset) {
        String sql = "DELETE FROM candles_day WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "DELETE FROM candles_week WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "DELETE FROM candles_month WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
