package com.cappuccino.profitable.data.tables;

import com.cappuccino.profitable.Configuration;
import com.cappuccino.profitable.data.DataBase;
import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.data.holderClasses.Order;
import com.cappuccino.profitable.util.NamingUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Orders {

    public static void insertOrder(String uuid, String owner, String asset, boolean sideBuy, double price, double units) {
        String sql = "INSERT INTO orders (order_uuid, owner, asset_id, sideBuy, price, units) VALUES (?, ?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, owner);
            stmt.setString(3, asset);
            stmt.setBoolean(4, sideBuy);
            stmt.setDouble(5, price);
            stmt.setDouble(6, units);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOrderUnits(String uuid, double newUnits) {
        String sql = "UPDATE orders SET units = ? WHERE order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setDouble(1, newUnits);
            stmt.setString(2, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static List<Order> getBestOrders(String asset, String account , boolean sideBuy, double price, double units) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE asset_id = ? AND sideBuy = ? AND price " + (sideBuy ? "<=" : ">=") + " ? AND owner != ? ORDER BY price " + (sideBuy ? "ASC" : "DESC") + ";";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);
            stmt.setBoolean(2, !sideBuy);
            stmt.setDouble(3, price);
            stmt.setString(4, account);

            try (ResultSet rs = stmt.executeQuery()) {
                double accumulatedUnits = 0;
                while (rs.next()) {
                    double iteratedUnits = rs.getDouble("units");

                    Order order = new Order(
                            rs.getString("order_uuid"),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            iteratedUnits
                    );
                    orders.add(order);

                    accumulatedUnits += iteratedUnits;
                    if(accumulatedUnits >= units){
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static double getBid(String asset) {
        Double bid = -1d;

        String sqlBid = "SELECT price FROM orders WHERE asset_id = ? AND sideBuy = true ORDER BY price DESC LIMIT 1;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sqlBid)) {
            stmt.setString(1, asset);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    bid = rs.getDouble("price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bid;
    }

    public static double getAsk(String asset) {

        Double ask = -1d;

        String sqlAsk = "SELECT price FROM orders WHERE asset_id = ? AND sideBuy = false ORDER BY price ASC LIMIT 1;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sqlAsk)) {
            stmt.setString(1, asset);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ask = rs.getDouble("price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ask;
    }

    public static List<Order> getAccountOrders(String owner) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE owner = ? ORDER BY asset_id;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, owner);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Order order = new Order(
                            rs.getString("order_uuid"),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            rs.getDouble("units")
                    );

                    orders.add(order);

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<Order> getAssetOrders(String asset) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, asset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Order order = new Order(
                            rs.getString("order_uuid"),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            rs.getDouble("units")
                    );

                    orders.add(order);

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static Order getOrder(String uuid) {
        String sql = "SELECT * FROM orders WHERE order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    return new Order(
                            rs.getString("order_uuid"),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            rs.getDouble("units")
                    );

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }



    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getString("order_uuid"),
                        rs.getString("owner"),
                        rs.getString("asset_id"),
                        rs.getBoolean("sideBuy"),
                        rs.getDouble("price"),
                        rs.getDouble("units")
                );
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static void deleteOrders(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM orders WHERE order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            for (String uuid : uuids) {
                stmt.setString(1, uuid);
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrder(String uuid) {
        String sql = "DELETE FROM orders WHERE order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getCurrentConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllOrders() {
        String sql = "DELETE FROM orders;";

        try (Statement stmt = DataBase.getCurrentConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean cancelOrder(String orderid, Player player){
        Order order = Orders.getOrder(orderid);
        String account = Accounts.getAccount(player);

        if(order == null || !Objects.equals(account, order.getOwner())){
            return false;
        }

        boolean sideBuy = order.isSideBuy();

        Asset asset = sideBuy? Assets.getAssetData(Configuration.MAINCURRENCYASSET.getCode()) : Assets.getAssetData(order.getAsset());

        double ammountToSendBack = sideBuy? order.getPrice() * order.getUnits()  : order.getUnits();

        Asset.distributeAsset(account, asset.getCode(), asset.getAssetType(), ammountToSendBack);

        player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1 , 1);

        Orders.deleteOrder(order.getUuid());

        player.sendMessage(NamingUtil.profitablePrefix() + "Canceled: "+ order.toStringSimplified());
        player.sendMessage(NamingUtil.profitablePrefix() + "Sent "  + asset.getColor() + ammountToSendBack + " " + asset.getCode() + ChatColor.RESET + " back to you");

        return true;
    }

    public static boolean cancelOrder(String orderid){
        Order order = Orders.getOrder(orderid);

        if(order == null){
            return false;
        }

        boolean sideBuy = order.isSideBuy();

        Asset asset = sideBuy? Assets.getAssetData(Configuration.MAINCURRENCYASSET.getCode()) : Assets.getAssetData(order.getAsset());

        double ammountToSendBack = sideBuy? order.getPrice() * order.getUnits()  : order.getUnits();

        Asset.distributeAsset(order.getOwner(), asset.getCode(), asset.getAssetType(), ammountToSendBack);

        Orders.deleteOrder(order.getUuid());

        return true;
    }

}
