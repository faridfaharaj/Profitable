package com.cappuccino.profitable.exchange.Books;

import com.cappuccino.profitable.Configuration;
import com.cappuccino.profitable.Profitable;
import com.cappuccino.profitable.data.tables.Accounts;
import com.cappuccino.profitable.data.tables.Assets;
import com.cappuccino.profitable.data.tables.Candles;
import com.cappuccino.profitable.data.tables.Orders;
import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.data.holderClasses.Order;
import com.cappuccino.profitable.tasks.TemporalItems;
import com.cappuccino.profitable.util.NamingUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Exchange {

    public static void sendNewOrder(Player player, String asset, boolean sideBuy, double price, double units){

        //validate
        if(Objects.equals(asset, Configuration.MAINCURRENCYASSET.getCode())){
            player.sendMessage(ChatColor.RED+"Cannot trade " + asset + " using " + asset);
            return;
        }

        Asset tradedAsset = Assets.getAssetData(asset);
        if(tradedAsset == null){
            player.sendMessage(ChatColor.RED + "That asset cannot be traded here");
            return;
        }

        String assetTypeName = NamingUtil.nameType(tradedAsset.getAssetType());
        String account;
        if(tradedAsset.getAssetType() == 1 && !player.hasPermission("profitable.market.trade.asset."+ assetTypeName.toLowerCase())){
            player.sendMessage(ChatColor.RED + "You dont have permission to trade assets from type: " + assetTypeName);
            return;
        }else if(tradedAsset.getAssetType() == 2){
            if(!player.hasPermission("profitable.market.trade.asset.item")){
                player.sendMessage(ChatColor.RED + "You dont have permission to trade assets from type: Item Commodity");
                return;
            }
            account = Accounts.getAccount(player);
            if(Accounts.isItemDeliveryNull(account)){
                player.sendMessage(NamingUtil.profitablePrefix() + ChatColor.YELLOW + "You must set a location for delivery");
                player.sendMessage(NamingUtil.profitablePrefix() + ChatColor.YELLOW + "/account delivery set item");
                TemporalItems.sendDeliveryStick(player, true);
                return;
            }


        }else if(tradedAsset.getAssetType() == 3){
            if(!player.hasPermission("profitable.market.trade.asset.entity")){
                player.sendMessage(ChatColor.RED + "You dont have permission to trade assets from type: Entity Commodity");
                return;
            }
            account = Accounts.getAccount(player);
            if(Accounts.isEntityDeliveryNull(account)){
                player.sendMessage(NamingUtil.profitablePrefix() + ChatColor.YELLOW + "You must set a location for delivery");
                player.sendMessage(NamingUtil.profitablePrefix() + ChatColor.YELLOW + "/account delivery set entity");
                TemporalItems.sendDeliveryStick(player, false);
                return;
            }


        }else{
            account = Accounts.getAccount(player);
        }

        double correctedUnits = units;
        if(tradedAsset.getAssetType() == 2 || tradedAsset.getAssetType() == 3) correctedUnits = (int) correctedUnits;

        if(correctedUnits == 0){
            player.sendMessage(ChatColor.RED + "Cannot trade 0 " + asset);
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
                player.sendMessage(NamingUtil.profitablePrefix()+ChatColor.YELLOW + "Liquidity shortage, Place limit order instead");
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

            player.sendMessage(NamingUtil.profitablePrefix()+ChatColor.YELLOW + "Order was partially filled due to liquidity shortage");

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

    public static void sendTransactionNotice(Player player, boolean sideBuy, ChatColor tradedColor, String tradedCode, double units, double profit){
        player.sendMessage(NamingUtil.profitablePrefix() + "Successfully " + (sideBuy? ChatColor.GREEN + "Bought ": ChatColor.RED + "Sold ") + tradedColor + units + " " + tradedCode + ChatColor.RESET + " for " + Configuration.MAINCURRENCYASSET.getColor() + profit + " " + Configuration.MAINCURRENCYASSET.getCode());
    }

    public static void sendOrderNotice(Player player, boolean sideBuy, ChatColor tradedColor, String tradedCode, ChatColor currencyColor, String currencyCode, double units, double price){
        if(sideBuy){
            player.sendMessage(NamingUtil.profitablePrefix() + "New order: " + ChatColor.GREEN + "Buy " + tradedColor + units + " " + tradedCode + ChatColor.RESET + " at" + currencyColor + " " + (price) + " " + currencyCode);
        }else{
            player.sendMessage(NamingUtil.profitablePrefix() + "New order: " + ChatColor.RED + "Sell " + tradedColor + units + " " + tradedCode + ChatColor.RESET + " at" + currencyColor + " " + (price) + " " + currencyCode);
        }
        player.sendMessage(NamingUtil.profitablePrefix() + ChatColor.YELLOW + "Use '/account orders' to manage");
    }

    public static void addToBook(String asset, String owner, boolean sideBuy, double price, double units){
        Orders.insertOrder(UUID.randomUUID().toString(), owner, asset, sideBuy, price, units);
    }

    public static void transact(String buyerAccount, String sellerAccount, String asset, ChatColor assetColor, int assetType, boolean sidebuy,double price, double units, World world){

        Asset.distributeAsset(sellerAccount, Configuration.MAINCURRENCYASSET.getCode(), 1, price*units);

        Asset.distributeAsset(buyerAccount, asset, assetType, units);

        if(sidebuy){
            sendMessageDefaultAcc(sellerAccount, NamingUtil.profitablePrefix() + "Successfully " + ChatColor.RED + "sold " + assetColor + units + " " + asset + ChatColor.RESET + " for" + Configuration.MAINCURRENCYASSET.getColor() + " " + (units*price) + " " + Configuration.MAINCURRENCYASSET.getCode());
        }else {
            sendMessageDefaultAcc(buyerAccount, NamingUtil.profitablePrefix() + "Successfully " + ChatColor.GREEN + "bought " + assetColor + units + " " + asset + ChatColor.RESET + " for" + Configuration.MAINCURRENCYASSET.getColor() + " " + (units*price) + " " + Configuration.MAINCURRENCYASSET.getCode());
        }

        Candles.updateDay(asset, world, price, units);
    }

    public static void sendMessageDefaultAcc(String account, String notice){
        UUID playerid;
        try{
            playerid = UUID.fromString(account);
        }catch (Exception e){
            return;
        }

        Player player = Profitable.getInstance().getServer().getPlayer(playerid);
        if(player != null){
            Location location = player.getLocation();
            player.sendMessage(notice);
            player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
            player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
            player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
        }
    }

}
