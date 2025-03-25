package com.cappuccino.profitable.data.tables;


import com.cappuccino.profitable.data.DataBase;
import com.cappuccino.profitable.data.holderClasses.Candle;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Candles {

    //this could be way more performant, needs fixing  <------------

    static String[] tables = {"day", "week", "month"};
    static long[] intervals = {24000, 168000, 720000};

    public static void updateDay(String asset, World world, double price, double volume){

        for(int i = 0; i<3; i++){

            String sql = "INSERT INTO candles_"+ tables[i] +" (time, open, close, high, low, volume, asset_id) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(time, asset_id) DO UPDATE SET" +
                    "    high = MAX(high, excluded.high)," +
                    "    low = MIN(low, excluded.low)," +
                    "    close = excluded.close, " +
                    "    volume = volume + excluded.volume;";

            try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
                stmt.setLong(1, (world.getFullTime() / intervals[i]) * intervals[i]);
                stmt.setDouble(2,price);
                stmt.setDouble(3,price);
                stmt.setDouble(4,price);
                stmt.setDouble(5,price);
                stmt.setDouble(6,volume);
                stmt.setString(7,asset);

                stmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


    }

    public static Candle getLastDay(String asset, long time) {
        String sql = "SELECT time, open, close, high, low, volume FROM candles_day WHERE asset_id = ? AND time = ? " +
                "UNION ALL " +
                "SELECT time, close AS open, close, close AS high, close AS low, 0 AS volume FROM candles_day WHERE asset_id = ? ORDER BY time DESC LIMIT 1;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.setLong(2, (time / intervals[0]) * intervals[0]);
            stmt.setString(3, asset);

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

    public static List<Candle> getInterval(String asset, long time, int interval) {

        String sql = "SELECT * FROM candles_" + tables[interval] + " WHERE asset_id = ? AND time > ? ORDER BY time ASC;";

        List<Candle> candles = new ArrayList<>();

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.setLong(2, time);

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
                        System.out.println(missingDays);
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


    public static List<String> getHotAssets(long time) {
        String sql = "SELECT asset_id, open, close FROM candles_month WHERE time = ? ORDER BY ABS((close - open) / open) DESC LIMIT 8;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setLong(1, (time / intervals[2]) * intervals[2]);

            List<String> tickerList = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    String asset = rs.getString("asset_id");
                    double open = rs.getDouble("open");
                    double close = rs.getDouble("close");

                    double change = close-open;


                    tickerList.add(asset + "   $" + close + "   " + (change<0? ChatColor.RED:ChatColor.GREEN) + ("$"+change+"  "+ Math.ceil(change/open*10000)/100 + "% month"));
                }
            }

            return tickerList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static List<String> getPerformingAssets(long time) {
        String sql = "SELECT asset_id, open, close FROM candles_month WHERE time = ? ORDER BY ((close - open) / open) DESC LIMIT 8;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setLong(1, (time / intervals[2]) * intervals[2]);

            List<String> tickerList = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    String asset = rs.getString("asset_id");
                    double open = rs.getDouble("open");
                    double close = rs.getDouble("close");

                    double change = close-open;


                    tickerList.add(asset + "   $" + close + "   " + (change<0? ChatColor.RED:ChatColor.GREEN) + ("$"+change+"  "+ Math.ceil(change/open*10000)/100 + "% month"));
                }
            }

            return tickerList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static List<String> getLiquidAssets(long time) {
        String sql = "SELECT asset_id, open, close FROM candles_month WHERE time = ? ORDER BY volume DESC LIMIT 8;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setLong(1, (time / intervals[2]) * intervals[2]);

            List<String> tickerList = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    String asset = rs.getString("asset_id");
                    double open = rs.getDouble("open");
                    double close = rs.getDouble("close");

                    double change = close-open;


                    tickerList.add(asset + "   $" + close + "   " + (change<0? ChatColor.RED:ChatColor.GREEN) + ("$"+change+"  "+ Math.ceil(change/open*10000)/100 + "% month"));
                }
            }

            return tickerList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static List<String> getExpensiveAssets(long time) {
        String sql = "SELECT asset_id, open, close FROM candles_month WHERE time = ? ORDER BY close DESC LIMIT 8;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setLong(1, (time / intervals[2]) * intervals[2]);

            List<String> tickerList = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    String asset = rs.getString("asset_id");
                    double open = rs.getDouble("open");
                    double close = rs.getDouble("close");

                    double change = close-open;


                    tickerList.add(asset + "   $" + close + "   " + (change<0? ChatColor.RED:ChatColor.GREEN) + ("$"+change+"  "+ Math.ceil(change/open*10000)/100 + "% month"));
                }
            }

            return tickerList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static void assetDeleteAllCandles(String asset) {
        String sql = "DELETE FROM candles_day WHERE asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "DELETE FROM candles_week WHERE asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "DELETE FROM candles_month WHERE asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
