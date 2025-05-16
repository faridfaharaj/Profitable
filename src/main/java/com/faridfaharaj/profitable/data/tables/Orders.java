package com.faridfaharaj.profitable.data.tables;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Orders {

    public static boolean insertOrder(UUID uuid, String owner, String asset, boolean sideBuy, double price, double units, Order.OrderType orderType) {
        String sql = "INSERT INTO orders (world, order_uuid, owner, asset_id, sideBuy, price, units, order_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setBytes(2, MessagingUtil.UUIDtoBytes(uuid));
            stmt.setString(3, owner);
            stmt.setString(4, asset);
            stmt.setBoolean(5, sideBuy);
            stmt.setDouble(6, price);
            stmt.setDouble(7, units);
            stmt.setDouble(8, orderType.getValue());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static void updateOrderUnits(UUID uuid, double newUnits) {
        String sql = "UPDATE orders SET units = ? WHERE world = ? AND order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, newUnits);
            stmt.setBytes(2, DataBase.getCurrentWorld());
            stmt.setBytes(3, MessagingUtil.UUIDtoBytes(uuid));
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateStopLimit(double old, double actual) {
        String sql = "UPDATE orders SET order_type = " + Order.OrderType.LIMIT.getValue() + " WHERE world = ? AND order_type = " + Order.OrderType.STOP_LIMIT.getValue() + " AND price <= ? AND price >= ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setDouble(2, Math.max(old,actual));
            stmt.setDouble(3, Math.min(old,actual));
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*public static List<Order> getStopMarket(double old, double actual) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * WHERE world = ? AND order_type == 2 AND price <= ? AND price >= ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setDouble(2, Math.max(old,actual));
            stmt.setDouble(3, Math.min(old,actual));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Order order = new Order(
                            TextUtil.UUIDfromBytes(rs.getBytes("order_uuid")),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            rs.getDouble("units"),
                            Order.OrderType.fromValue(rs.getInt("order_type"))
                    );
                    orders.add(order);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/


    public static List<Order> getBestOrders(String asset, boolean sideBuy, double price, double units) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE world = ? AND asset_id = ? AND sideBuy = ? AND price " + (sideBuy ? "<=" : ">=") + " ? AND order_type = " + Order.OrderType.LIMIT.getValue() + " ORDER BY price " + (sideBuy ? "ASC" : "DESC") + " LIMIT " + Math.ceil(units) +";";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            stmt.setBoolean(3, !sideBuy);
            stmt.setDouble(4, price);

            try (ResultSet rs = stmt.executeQuery()) {
                double accumulatedUnits = 0;
                while (rs.next()) {
                    double iteratedUnits = rs.getDouble("units");

                    Order order = new Order(
                            MessagingUtil.UUIDfromBytes(rs.getBytes("order_uuid")),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            iteratedUnits,
                            Order.OrderType.fromValue(rs.getInt("order_type"))
                    );
                    orders.add(order);

                    accumulatedUnits += iteratedUnits;
                    if(accumulatedUnits >= units){
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<Order> getBidAsk(String asset, boolean isBid) {
        List<Order> orders = new ArrayList<>();
        String orderDirection = isBid ? "DESC" : "ASC";

        String sql = "SELECT price, SUM(units) as units FROM orders " +
                "WHERE world = ? AND asset_id = ? AND sideBuy = ? AND order_type = ? " +
                "GROUP BY price " +
                "ORDER BY price " + orderDirection + " " +
                "LIMIT 7;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);
            stmt.setBoolean(3, isBid);
            stmt.setInt(4, Order.OrderType.LIMIT.getValue());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            null,
                            null,
                            null,
                            isBid,
                            rs.getDouble("price"),
                            rs.getDouble("units"),
                            null
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public static List<Order> getAccountOrders(String owner) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE world = ? AND owner = ? ORDER BY asset_id;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, owner);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Order order = new Order(
                            MessagingUtil.UUIDfromBytes(rs.getBytes("order_uuid")),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            rs.getDouble("units"),
                            Order.OrderType.fromValue(rs.getInt("order_type"))
                    );

                    orders.add(order);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<Order> getAssetOrders(String asset) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE world = ? AND asset_id = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setString(2, asset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Order order = new Order(
                            MessagingUtil.UUIDfromBytes(rs.getBytes("order_uuid")),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            rs.getDouble("units"),
                            Order.OrderType.fromValue(rs.getInt("order_type"))
                    );

                    orders.add(order);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static Order getOrder(UUID uuid) {
        String sql = "SELECT * FROM orders WHERE world = ? AND order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setBytes(2, MessagingUtil.UUIDtoBytes(uuid));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    return new Order(
                            MessagingUtil.UUIDfromBytes(rs.getBytes("order_uuid")),
                            rs.getString("owner"),
                            rs.getString("asset_id"),
                            rs.getBoolean("sideBuy"),
                            rs.getDouble("price"),
                            rs.getDouble("units"),
                            Order.OrderType.fromValue(rs.getInt("order_type"))
                    );

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;

    }



    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        MessagingUtil.UUIDfromBytes(rs.getBytes("order_uuid")),
                        rs.getString("owner"),
                        rs.getString("asset_id"),
                        rs.getBoolean("sideBuy"),
                        rs.getDouble("price"),
                        rs.getDouble("units"),
                        Order.OrderType.fromValue(rs.getInt("order_type"))
                );
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return orders;
    }

    public static void deleteOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM orders WHERE world = ? AND order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            for (Order order : orders) {
                stmt.setBytes(1, DataBase.getCurrentWorld());
                stmt.setBytes(2, MessagingUtil.UUIDtoBytes(order.getUuid()));
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteOrder(UUID uuid) {
        String sql = "DELETE FROM orders WHERE world = ? AND order_uuid = ?;";

        try (PreparedStatement stmt = DataBase.getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, DataBase.getCurrentWorld());
            stmt.setBytes(2, MessagingUtil.UUIDtoBytes(uuid));
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static boolean deleteAllOrders() {
        String sql = "DELETE FROM orders;";

        try (Statement stmt = DataBase.getConnection().createStatement()) {
            int rows = stmt.executeUpdate(sql);
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean cancelOrder(UUID orderid, Player player){
        Order order = Orders.getOrder(orderid);
        String account = Accounts.getAccount(player);

        if(order == null || !Objects.equals(account, order.getOwner())){
            return false;
        }

        boolean sideBuy = order.isSideBuy();

        Asset tradedAsset = Assets.getAssetData(order.getAsset());
        Asset asset = sideBuy? Configuration.MAINCURRENCYASSET : tradedAsset;

        double ammountToSendBack = sideBuy?
                order.getPrice() * order.getUnits() + Configuration.parseFee(Configuration.ASSETFEES[tradedAsset.getAssetType()][1], order.getPrice() * order.getUnits())
                :
                order.getUnits();

        Asset.distributeAsset(account, asset, ammountToSendBack);

        player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1 , 1);

        Orders.deleteOrder(order.getUuid());

        MessagingUtil.sendCustomMessage(player, Component.text("Cancelled ").append(order.toStringSimplified()));
        MessagingUtil.sendPaymentNotice(player, ammountToSendBack, 0, asset);

        return true;
    }

    public static boolean cancelOrder(UUID orderid){
        Order order = Orders.getOrder(orderid);

        if(order == null){
            return false;
        }

        boolean sideBuy = order.isSideBuy();

        Asset tradedAsset = Assets.getAssetData(order.getAsset());
        Asset asset = sideBuy? Configuration.MAINCURRENCYASSET : tradedAsset;

        double ammountToSendBack = sideBuy?
                order.getPrice() * order.getUnits() + Configuration.parseFee(Configuration.ASSETFEES[tradedAsset.getAssetType()][1], order.getPrice() * order.getUnits())
                :
                order.getUnits();

        Asset.distributeAsset(order.getOwner(), asset, ammountToSendBack);

        Orders.deleteOrder(order.getUuid());

        return true;
    }

}
