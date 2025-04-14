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
import com.faridfaharaj.profitable.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
            TextUtil.sendError(player, "Cannot trade " + order.getAsset() + " using " + order.getAsset());
            return;
        }

        Asset tradedAsset = Assets.getAssetData(order.getAsset());
        if(tradedAsset == null){
            TextUtil.sendError(player, "That asset cannot be traded here");
            return;
        }

        String assetTypeName = TextUtil.nameType(tradedAsset.getAssetType());
        if(tradedAsset.getAssetType() == 2){
            if(!player.hasPermission("profitable.market.trade.asset.item")){
                TextUtil.sendError(player, "You don't have permission to trade items");
                return;
            }
            if(Accounts.getItemDelivery(order.getOwner()) == null){
                TextUtil.sendWarning(player, "You must set a location for delivery");
                TextUtil.sendButton(player, "[/delivery set item]", "/delivery set item");
                TemporalItems.sendDeliveryStick(player, true);
                return;
            }


        }else if(tradedAsset.getAssetType() == 3){
            if(!player.hasPermission("profitable.market.trade.asset.entity")){
                TextUtil.sendError(player, "You don't have permission to trade entities");
                return;
            }
            if(Accounts.getEntityDelivery(order.getOwner()) == null){
                TextUtil.sendWarning(player, "You must set a location for delivery");
                TextUtil.sendButton(player, "[/delivery set entity]", "/delivery set entity");
                TemporalItems.sendDeliveryStick(player, false);
                return;
            }


        }else if(!player.hasPermission("profitable.market.trade.asset."+ assetTypeName.toLowerCase())){
            TextUtil.sendError(player, "You don't have permission to trade assets from type: " + assetTypeName);
            return;
        }

        if(tradedAsset.getAssetType() == 2 || tradedAsset.getAssetType() == 3) order.setUnits((int) order.getUnits());

        if(order.getUnits() <= 0){
            TextUtil.sendError(player, "Units must be at least 1");
            return;
        }

        if(order.getPrice() < 0){
            TextUtil.sendError(player, "Invalid price");
            return;
        }

        //collateral
        Asset collateralAsset = order.isSideBuy()? Configuration.MAINCURRENCYASSET: tradedAsset;

        //stopOrders
        if(order.getType() == Order.OrderType.STOP_LIMIT){
            Candle lastday = Candles.getLastDay(tradedAsset.getCode(), player.getWorld().getFullTime());

            if(order.isSideBuy()?lastday.getClose() >= order.getPrice(): lastday.getClose() <= order.getPrice()){
                TextUtil.sendError(player, (order.isSideBuy()? "Stop price must be higher than market's when buying": "Stop price must be lower than market's when selling"));
                return;
            }

            if(!Asset.retrieveAsset(player, "Order couldn't be added", collateralAsset.getCode(), collateralAsset.getAssetType(), order.isSideBuy()?order.getPrice()*order.getUnits() : order.getUnits())){
                return;
            }

            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1 , 1);
            addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset);
            return;
        }
        if(order.getType() == Order.OrderType.STOP_MARKET){
            Candle lastday = Candles.getLastDay(tradedAsset.getCode(), player.getWorld().getFullTime());

            if(order.isSideBuy()?order.getPrice() <= lastday.getClose() : lastday.getClose() <= order.getPrice()){
                TextUtil.sendError(player, (order.isSideBuy()? "Stop price must be higher than market's when buying": "Stop price must be lower than market's when selling"));
                return;
            }

            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1 , 1);
            addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset);
            return;
        }

        //lookup
        List<Order> orders = Orders.getBestOrders(order.getAsset(), order.getOwner(), order.isSideBuy(), order.getPrice(), order.getUnits());
        //no matches
        if(orders.isEmpty()){

            //Market
            if(order.getType() == Order.OrderType.MARKET){
                TextUtil.sendWarning(player, "No orders available, add a price to place limit order");
                return;
            }

            //Limit
            if(!Asset.retrieveAsset(player, "Order couldn't be added", collateralAsset.getCode(), collateralAsset.getAssetType(),order.isSideBuy()?order.getPrice()*order.getUnits():order.getUnits())){
                return;
            }

            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1 , 1);
            addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset);
            return;
        }

        List<UUID> ordersToDelete = new ArrayList<>();
        double moneyTransacted = 0;
        double unitsMissing = order.getUnits();
        for(Order iteratedOrder : orders){
            double iteratedPrice = iteratedOrder.getPrice();
            double iteratedUnits = iteratedOrder.getUnits();

            double transactingUnits;
            if(unitsMissing < iteratedUnits){

                transactingUnits = unitsMissing;
                Orders.updateOrderUnits(iteratedOrder.getUuid(), iteratedUnits-unitsMissing);

            }else {

                transactingUnits = iteratedUnits;
                ordersToDelete.add(iteratedOrder.getUuid());

            }

            if(!Asset.retrieveAsset(player, "Order was partially filled", collateralAsset.getCode(), collateralAsset.getAssetType(), order.isSideBuy()?transactingUnits*iteratedPrice:transactingUnits)){
                if(order.getUnits() != unitsMissing){
                    sendTransactionNotice(player, iteratedOrder.isSideBuy(), tradedAsset.getColor(), iteratedOrder.getAsset(), (order.getUnits()-unitsMissing),moneyTransacted);

                    if(!ordersToDelete.isEmpty()){
                        Orders.deleteOrders(ordersToDelete);
                    }
                }
                return;
            }
            unitsMissing -= iteratedUnits;

            moneyTransacted += iteratedPrice*transactingUnits;
            transact(iteratedOrder.isSideBuy()? order.getOwner(): iteratedOrder.getOwner(), !iteratedOrder.isSideBuy()? order.getOwner(): iteratedOrder.getOwner(), tradedAsset.getCode(), tradedAsset.getColor(), tradedAsset.getAssetType(), order.isSideBuy(), iteratedPrice, transactingUnits, player.getWorld());

        }

        if(unitsMissing > 0){
            sendTransactionNotice(player, order.isSideBuy(), tradedAsset.getColor(), order.getAsset(), (order.getUnits()-unitsMissing), moneyTransacted);

            TextUtil.sendWarning(player, "Partially filled because no more orders are available");

            if(order.getPrice() != Double.MAX_VALUE && order.getPrice() != Double.MIN_VALUE){

                if(!Asset.retrieveAsset(player, "Order couldn't be added", collateralAsset.getCode(), collateralAsset.getAssetType() , order.isSideBuy()?order.getPrice()*unitsMissing:unitsMissing)){
                    return;
                }
                order.setUnits(unitsMissing);
                addToBook(player, order, Configuration.MAINCURRENCYASSET, tradedAsset);
                player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1 , 1);

            }

        }else {
            sendTransactionNotice(player, order.isSideBuy(), tradedAsset.getColor(), order.getAsset(), order.getUnits(), moneyTransacted);
        }

        Orders.deleteOrders(ordersToDelete);

    }

    public static void sendTransactionNotice(Player player, boolean sideBuy, TextColor tradedColor, String tradedCode, double units, double money){
        TextUtil.sendCustomMessage(player, TextUtil.profitablePrefix()
                        .append(Component.text("Successfully ")).append(sideBuy?Component.text("bought ", Configuration.COLORBULLISH):Component.text("sold ", Configuration.COLORBEARISH))
                        .append(Component.text(units + " " + tradedCode, tradedColor))
                        .append(Component.text(" using "))
                        .append(Component.text(money + " " + Configuration.MAINCURRENCYASSET.getCode(), Configuration.MAINCURRENCYASSET.getColor()))

                );
    }

    public static void addToBook(Player player, Order order, Asset currency, Asset tradedasset){
        Orders.insertOrder(UUID.randomUUID(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), order.getType());

        TextUtil.sendCustomMessage(player, TextUtil.profitablePrefix()
                .append(Component.text("New " + order.getType().toString().replace("_","-").toLowerCase() + " order ")).append(order.isSideBuy()?Component.text("Buy ", Configuration.COLORBULLISH):Component.text("Sell ", Configuration.COLORBEARISH))
                .append(Component.text(order.getUnits() + " " + tradedasset.getCode(), tradedasset.getColor()))
                .append(Component.text(" at "))
                .append(Component.text(order.getPrice() + " " + currency.getCode(), currency.getColor()))
        );
    }

    public static void transact(String buyerAccount, String sellerAccount, String asset, TextColor assetColor, int assetType, boolean sidebuy,double price, double units, World world){

        Asset.distributeAsset(sellerAccount, Configuration.MAINCURRENCYASSET.getCode(), 1, price*units);

        Asset.distributeAsset(buyerAccount, asset, assetType, units);

        if(sidebuy){

            sendMessageDefaultAcc(sellerAccount, TextUtil.profitablePrefix()
                    .append(Component.text("Successfully ")).append(Component.text("sold ", Configuration.COLORBEARISH))
                    .append(Component.text(units + " " + asset,assetColor))
                    .append(Component.text(" for "))
                    .append(Component.text((units*price) + " " + Configuration.MAINCURRENCYASSET.getCode(), Configuration.MAINCURRENCYASSET.getColor()))
            );

        }else {
            sendMessageDefaultAcc(buyerAccount, TextUtil.profitablePrefix()
                    .append(Component.text("Successfully ")).append(Component.text("bought ", Configuration.COLORBULLISH))
                    .append(Component.text(units + " " + asset,assetColor))
                    .append(Component.text(" for "))
                    .append(Component.text((units*price) + " " + Configuration.MAINCURRENCYASSET.getCode(), Configuration.MAINCURRENCYASSET.getColor()))
            );
        }

        Candles.updateDay(asset, world, price, units);
        Orders.updateStopLimit(Candles.getLastDay(asset, world.getFullTime()).getClose(),price);
    }

    public static void sendMessageDefaultAcc(String account, Component notice){
        UUID playerid;
        try{
            playerid = UUID.fromString(account);
        }catch (Exception e){
            return;
        }

        Player player = Profitable.getInstance().getServer().getPlayer(playerid);
        if(player != null){
            Location location = player.getLocation();
            TextUtil.sendCustomMessage(player, notice);
            player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
            player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
            player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
        }
    }

}
