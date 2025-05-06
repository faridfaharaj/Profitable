package com.faridfaharaj.profitable.exchange.Books;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Exchange {

    public static void sendNewOrder(Player player, Order order){

        //validate
        if(Objects.equals(order.getAsset(), Configuration.MAINCURRENCYASSET.getCode())){
            MessagingUtil.sendError(player, "Cannot trade " + order.getAsset() + " using " + order.getAsset());
            return;
        }

        Asset tradedAsset = Assets.getAssetData(order.getAsset());
        if(tradedAsset == null){
            MessagingUtil.sendError(player, "That asset cannot be traded here");
            return;
        }

        if(Configuration.PHYSICALDELIVERY){
            String assetTypeName = NamingUtil.nameType(tradedAsset.getAssetType());

            if(tradedAsset.getAssetType() == 2){
                if(!player.hasPermission("profitable.market.trade.asset.item")){
                    MessagingUtil.sendError(player, "You don't have permission to trade items");
                    return;
                }
                if(Accounts.getItemDelivery(order.getOwner()) == null){
                    MessagingUtil.sendWarning(player, "You must set a location for delivery");
                    MessagingUtil.sendButton(player, "[/delivery set item]", "/delivery set item");
                    TemporalItems.sendDeliveryStick(player, true);
                    return;
                }


            }else if(tradedAsset.getAssetType() == 3){
                if(!player.hasPermission("profitable.market.trade.asset.entity")){
                    MessagingUtil.sendError(player, "You don't have permission to trade entities");
                    return;
                }
                if(Accounts.getEntityDelivery(order.getOwner()) == null){
                    MessagingUtil.sendWarning(player, "You must set a location for delivery");
                    MessagingUtil.sendButton(player, "[/delivery set entity]", "/delivery set entity");
                    TemporalItems.sendDeliveryStick(player, false);
                    return;
                }


            }else if(!player.hasPermission("profitable.market.trade.asset."+ assetTypeName.toLowerCase())){
                MessagingUtil.sendError(player, "You don't have permission to trade assets from type: " + assetTypeName);
                return;
            }

        }

        if(tradedAsset.getAssetType() == 2 || tradedAsset.getAssetType() == 3) order.setUnits((int) order.getUnits());

        if(order.getUnits() <= 0){
            MessagingUtil.sendError(player, "Units must be at least 1");
            return;
        }

        if(order.getPrice() <= 0){
            MessagingUtil.sendError(player, "Invalid price");
            return;
        }

        Asset collateralAsset = order.isSideBuy()? Configuration.MAINCURRENCYASSET: tradedAsset;

        Candle lastday = Candles.getLastDay(tradedAsset.getCode(), player.getWorld().getFullTime());
        //stopOrders
        if(order.getType() == Order.OrderType.STOP_LIMIT){

            if(order.isSideBuy()?lastday.getClose() >= order.getPrice(): lastday.getClose() <= order.getPrice()){
                MessagingUtil.sendError(player, (order.isSideBuy()? "Stop price must be higher than market's when buying": "Stop price must be lower than market's when selling"));
                return;
            }

            addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset, collateralAsset);
            return;
        }else if(order.getType() == Order.OrderType.STOP_MARKET){

            if(order.isSideBuy()?order.getPrice() <= lastday.getClose() : lastday.getClose() <= order.getPrice()){
                MessagingUtil.sendError(player, (order.isSideBuy()? "Stop price must be higher than market's when buying": "Stop price must be lower than market's when selling"));
                return;
            }

            addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset, collateralAsset);
            return;
        }

        //lookup
        List<Order> orders = Orders.getBestOrders(order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits());
        //no matches
        if(orders.isEmpty()){

            //Market
            if(order.getType() == Order.OrderType.MARKET){
                MessagingUtil.sendWarning(player, "No orders available, add a price to place a limit order");
                return;
            }

            //Limit
            addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset, collateralAsset);
            return;
        }

        // Transacting
        Order partialOrder = null;
        double moneyTransacted = 0;
        double unitsMissing = order.getUnits();
        for(Order iteratedOrder : orders){

            // Prevent self transact
            if(Objects.equals(iteratedOrder.getOwner(), order.getOwner())){
                MessagingUtil.sendError(player, "You can't transact with yourself!, cancel current order first");
                return;
            }

            double transactingUnits = Math.min(unitsMissing, iteratedOrder.getUnits());

            // transact
            // Mark for deletion
            if(iteratedOrder.getUnits() > unitsMissing){
                Order clone = new Order(iteratedOrder);
                clone.setUnits(iteratedOrder.getUnits() - transactingUnits);
                partialOrder = clone;
            }

            iteratedOrder.setUnits(transactingUnits);


            // Tracking
            unitsMissing -= transactingUnits;
            moneyTransacted += iteratedOrder.getPrice()*transactingUnits;

        }


        double unitsTransacted = order.getUnits()-unitsMissing;
        double finalMoneyTransacted = moneyTransacted;
        boolean fixedTakerFee = !Configuration.ASSETFEES[tradedAsset.getAssetType()][0].endsWith("%");
        double fee = fixedTakerFee? Configuration.parseFee(Configuration.ASSETFEES[tradedAsset.getAssetType()][0], 23): Configuration.parseFee(Configuration.ASSETFEES[tradedAsset.getAssetType()][0], moneyTransacted);

        Order finalPartialOrder = partialOrder;
        Asset.chargeAndRun(player, "Cannot fulfill your order", collateralAsset, order.isSideBuy()?moneyTransacted+fee:unitsTransacted, () -> {
            wrapTransaction(player, order, fee, tradedAsset, finalMoneyTransacted, unitsTransacted);
            Candles.updateDay(tradedAsset.getCode(), player.getWorld(), orders.getLast().getPrice(), unitsTransacted);
            Orders.updateStopLimit(lastday.getClose(), orders.getLast().getPrice());
            List<Order> ordersToDelete = new ArrayList<>(orders);
            if(finalPartialOrder != null){

                Orders.updateOrderUnits(finalPartialOrder.getUuid(), finalPartialOrder.getUnits());
                ordersToDelete.removeLast();

            }
            for(Order iteratedOrder:orders){
                if(iteratedOrder.isSideBuy()){

                    Asset.distributeAsset(iteratedOrder.getOwner(), tradedAsset, iteratedOrder.getUnits());
                    sendTransactionNotice(iteratedOrder.getOwner(), true, tradedAsset, iteratedOrder.getUnits(), (iteratedOrder.getUnits()*iteratedOrder.getPrice()), 0);

                }else {

                    double makerFee = Configuration.parseFee(Configuration.ASSETFEES[tradedAsset.getAssetType()][1],iteratedOrder.getUnits()*iteratedOrder.getPrice());
                    Asset.distributeAsset(iteratedOrder.getOwner(), Configuration.MAINCURRENCYASSET, iteratedOrder.getUnits()*iteratedOrder.getPrice()-makerFee);
                    sendTransactionNotice(iteratedOrder.getOwner(), false, tradedAsset, iteratedOrder.getUnits(), (iteratedOrder.getUnits()*iteratedOrder.getPrice()), makerFee);

                }
            }
            Orders.deleteOrders(ordersToDelete);
        });

        // Partial fill
        if(order.getType() == Order.OrderType.MARKET){

            if(unitsTransacted != order.getUnits()){
                MessagingUtil.sendWarning(player, "Partially filled because no more orders are available");
            }

        }else{
            if(unitsTransacted != order.getUnits()){
                order.setUnits(order.getUnits()-unitsTransacted);
                addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset, collateralAsset);
            }

        }

    }

    private static void wrapTransaction(Player player, Order order, double fee, Asset tradedAsset, double moneyTransacted, double unitsTransacted) {
        System.out.println(fee + " " + fee);
        if(order.isSideBuy()){
            Asset.distributeAsset(order.getOwner(), tradedAsset, unitsTransacted);
            sendTransactionNotice(player, true, tradedAsset, unitsTransacted, moneyTransacted, fee);
        }else{
            Asset.distributeAsset(order.getOwner(), Configuration.MAINCURRENCYASSET, moneyTransacted - fee);
            sendTransactionNotice(player, false, tradedAsset, unitsTransacted, moneyTransacted, fee);
        }
    }

    public static void sendTransactionNotice(Player player, boolean sideBuy, Asset tradedAsset, double units, double money, double fee){
        Location location = player.getLocation();
        MessagingUtil.sendCustomMessage(player, MessagingUtil.profitablePrefix()
                        .append(sideBuy?Component.text("Bought ", Configuration.COLORBULLISH):Component.text("Sold ", Configuration.COLORBEARISH))
                        .append(Component.text(units + " " + tradedAsset.getCode(), tradedAsset.getColor()))
                        .append(Component.text(" for "))
                        .append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, money))

                );

        if(sideBuy){
            MessagingUtil.sendChargeNotice(player, money, fee, Configuration.MAINCURRENCYASSET);
        }else{
            MessagingUtil.sendPaymentNotice(player, money, fee, Configuration.MAINCURRENCYASSET);
        }

        player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
        player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
        player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
    }

    public static void sendTransactionNotice(String account, boolean sideBuy, Asset tradedAsset, double units, double money, double fee){
        UUID playerid;
        try{
            playerid = UUID.fromString(account);
        }catch (Exception e){
            return;
        }

        Player player = Profitable.getInstance().getServer().getPlayer(playerid);
        if(player != null){
            Location location = player.getLocation();
            MessagingUtil.sendCustomMessage(player, MessagingUtil.profitablePrefix()
                    .append(sideBuy?Component.text("Bought ", Configuration.COLORBULLISH):Component.text("Sold ", Configuration.COLORBEARISH))
                    .append(Component.text(units + " " + tradedAsset.getCode(), tradedAsset.getColor()))
                    .append(Component.text(" for "))
                    .append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, money))

            );

            if(sideBuy){
                MessagingUtil.sendChargeNotice(player, money, fee, Configuration.MAINCURRENCYASSET);
            }else{
                MessagingUtil.sendPaymentNotice(player, money, fee, Configuration.MAINCURRENCYASSET);
            }

            player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
            player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
            player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
        }
    }

    public static void addToBook(Player player, Order order, Asset currency, Asset tradedasset, Asset collateralAsset){

        // Collateral
        double cost = order.getPrice()*order.getUnits();
        double makerFee = Configuration.parseFee(Configuration.ASSETFEES[tradedasset.getAssetType()][1], cost);

        Asset.chargeAndRun(player, "Order couldn't be added", collateralAsset, order.isSideBuy()? cost+makerFee: order.getUnits(), () -> {
            // Insert
            Orders.insertOrder(UUID.randomUUID(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), order.getType());

            // Feedback
            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1 , 1);
            });
            MessagingUtil.sendCustomMessage(player, MessagingUtil.profitablePrefix()
                    .append(Component.text("New " + order.getType().toString().replace("_","-").toLowerCase() + " order ")).append(order.isSideBuy()?Component.text("Buy ", Configuration.COLORBULLISH):Component.text("Sell ", Configuration.COLORBEARISH))
                    .append(MessagingUtil.assetAmmount(tradedasset, order.getUnits()))
                    .append(Component.text(" at "))
                    .append(MessagingUtil.assetAmmount(currency, order.getPrice()))
                    .append(Component.text(" each"))
            );

            if(order.isSideBuy()){

                MessagingUtil.sendChargeNotice(player, cost, makerFee, collateralAsset);

            }else{
                MessagingUtil.sendChargeNotice(player, order.getUnits(), 0, collateralAsset);

                if(makerFee >= cost){
                    MessagingUtil.sendWarning(player, "This order is worth less than its fee ($" + makerFee + ") there will be no profit!");
                }
            }
        });
    }

}
