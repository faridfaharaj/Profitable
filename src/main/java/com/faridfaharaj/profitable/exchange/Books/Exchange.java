package com.faridfaharaj.profitable.exchange.Books;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
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

    public static void sendNewOrder(Player player, String asset, boolean sideBuy, double price, double units){

        //validate
        if(Objects.equals(asset, Configuration.MAINCURRENCYASSET.getCode())){
            TextUtil.sendError(player, "Cannot trade " + asset + " using " + asset);
            return;
        }

        Asset tradedAsset = Assets.getAssetData(asset);
        if(tradedAsset == null){
            TextUtil.sendError(player, "That asset cannot be traded here");
            return;
        }

        String assetTypeName = TextUtil.nameType(tradedAsset.getAssetType());
        String account;
        if(tradedAsset.getAssetType() == 2){
            if(!player.hasPermission("profitable.market.trade.asset.item")){
                TextUtil.sendError(player, "You don't have permission to trade items");
                return;
            }
            account = Accounts.getAccount(player);
            if(Accounts.isItemDeliveryNull(account)){
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
            account = Accounts.getAccount(player);
            if(Accounts.isEntityDeliveryNull(account)){
                TextUtil.sendWarning(player, "You must set a location for delivery");
                TextUtil.sendButton(player, "[/delivery set entity]", "/delivery set entity");
                TemporalItems.sendDeliveryStick(player, false);
                return;
            }


        }else if(!player.hasPermission("profitable.market.trade.asset."+ assetTypeName.toLowerCase())){
            TextUtil.sendError(player, "You don't have permission to trade assets from type: " + assetTypeName);
            return;
        }else{
            account = Accounts.getAccount(player);
        }

        double correctedUnits = units;
        if(tradedAsset.getAssetType() == 2 || tradedAsset.getAssetType() == 3) correctedUnits = (int) correctedUnits;

        if(correctedUnits == 0){
            TextUtil.sendError(player, "Cannot trade 0 " + asset);
            return;
        }

        //collateral
        Asset collateralAsset;
        if(sideBuy){
            collateralAsset = Configuration.MAINCURRENCYASSET;
        }else{
            collateralAsset = tradedAsset;
        }

        //transaction
        List<Order> orders = Orders.getBestOrders(asset, account, sideBuy, price, correctedUnits);
        //no matches
        if(orders.isEmpty()){

            //Market
            if(price == Double.MAX_VALUE || price == Double.MIN_VALUE){
                TextUtil.sendWarning(player, "No orders available, Place limit order instead");
                return;
            }

            //Limit
            if(!Asset.retrieveAsset(player, "Order couldn't be added", collateralAsset.getCode(), collateralAsset.getAssetType(),sideBuy?price*correctedUnits:correctedUnits)){
                return;
            }

            sendOrderNotice(player, sideBuy, tradedAsset.getColor(), asset, Configuration.MAINCURRENCYASSET.getColor(), Configuration.MAINCURRENCYASSET.getCode(), correctedUnits, price);
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1 , 1);
            addToBook(asset, account, sideBuy, price, correctedUnits);
            return;
        }

        List<String> ordersToDelete = new ArrayList<>();
        double moneyTransacted = 0;
        double unitsMissing = correctedUnits;
        for(Order order : orders){
            double iteratedPrice = order.getPrice();
            double iteratedUnits = order.getUnits();

            double transactingUnits;
            if(unitsMissing < iteratedUnits){

                transactingUnits = unitsMissing;
                Orders.updateOrderUnits(order.getUuid(), iteratedUnits-unitsMissing);

            }else {

                transactingUnits = iteratedUnits;
                ordersToDelete.add(order.getUuid());

            }

            if(!Asset.retrieveAsset(player, "Order was partially filled", collateralAsset.getCode(), collateralAsset.getAssetType(), sideBuy?transactingUnits*iteratedPrice:transactingUnits)){
                if(correctedUnits != unitsMissing){
                    sendTransactionNotice(player, sideBuy, tradedAsset.getColor(), asset, (correctedUnits-unitsMissing),moneyTransacted);

                    if(!ordersToDelete.isEmpty()){
                        Orders.deleteOrders(ordersToDelete);
                    }
                }
                return;
            }
            unitsMissing -= iteratedUnits;

            moneyTransacted += iteratedPrice*transactingUnits;
            transact(sideBuy? account:order.getOwner(), !sideBuy? account:order.getOwner(), asset, tradedAsset.getColor(),tradedAsset.getAssetType(), sideBuy, iteratedPrice, transactingUnits, player.getWorld());

        }

        if(unitsMissing > 0){
            sendTransactionNotice(player, sideBuy, tradedAsset.getColor(), asset, (correctedUnits-unitsMissing), moneyTransacted);

            TextUtil.sendWarning(player, "Partially filled because no more orders are available");

            if(price != Double.MAX_VALUE && price != Double.MIN_VALUE){

                if(!Asset.retrieveAsset(player, "Order couldn't be added", collateralAsset.getCode(), collateralAsset.getAssetType() ,sideBuy?price*unitsMissing:unitsMissing)){
                    return;
                }
                addToBook(asset, account, sideBuy, price, unitsMissing);
                player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1 , 1);
                sendOrderNotice(player, sideBuy, tradedAsset.getColor(), asset, Configuration.MAINCURRENCYASSET.getColor(), Configuration.MAINCURRENCYASSET.getCode(), unitsMissing, price);

            }

        }else {
            sendTransactionNotice(player, sideBuy, tradedAsset.getColor(), asset, correctedUnits, moneyTransacted);
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

    public static void sendOrderNotice(Player player, boolean sideBuy, TextColor tradedColor, String tradedCode, TextColor currencyColor, String currencyCode, double units, double price){
        TextUtil.sendCustomMessage(player, TextUtil.profitablePrefix()
                .append(Component.text("New order ")).append(sideBuy?Component.text("Buy ", Configuration.COLORBULLISH):Component.text("Sell ", Configuration.COLORBEARISH))
                .append(Component.text(units + " " + tradedCode,tradedColor))
                .append(Component.text(" at "))
                .append(Component.text(price + " " + currencyCode, currencyColor))
        );
    }

    public static void addToBook(String asset, String owner, boolean sideBuy, double price, double units){
        Orders.insertOrder(UUID.randomUUID().toString(), owner, asset, sideBuy, price, units);
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
