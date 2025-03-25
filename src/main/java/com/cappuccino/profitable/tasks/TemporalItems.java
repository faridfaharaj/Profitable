package com.cappuccino.profitable.tasks;

import com.cappuccino.profitable.Configuration;
import com.cappuccino.profitable.Profitable;
import com.cappuccino.profitable.data.tables.Assets;
import com.cappuccino.profitable.data.tables.Candles;
import com.cappuccino.profitable.data.tables.Orders;
import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.data.holderClasses.Candle;
import com.cappuccino.profitable.util.NamingUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TemporalItems {

    public enum TemporalItem{

        INFOBOOK,
        CLAIMINGTAG,
        ITEMDELIVERYSTICK,
        ENTITYDELIVERYSTICK,
        GRAPHMAP;

    }

    public static HashMap<UUID, TemporalItem> holdingTemp = new HashMap();

    public static void removeTempItem(Player player){
        if(holdingTemp.containsKey(player.getUniqueId())){
            player.getInventory().setItemInMainHand(null);
            holdingTemp.remove(player.getUniqueId());
            player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1 , 1);
        }

    }

    public static void removeTemp(Player player){
        holdingTemp.remove(player.getUniqueId());
        player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1 , 1);

    }

    public static void addTemp(Player player, TemporalItem Item){
        removeTempItem(player);
        player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1 , 1);
        holdingTemp.put(player.getUniqueId(), Item);
    }



    //ITEMS-------------------------------------

    public static void sendInfoBook(Player player, String assetid){
        Asset asset = Assets.getAssetData(assetid);

        if(asset == null){
            player.sendMessage(ChatColor.RED + "This asset isn't traded here");
            return;
        }

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            player.sendMessage(ChatColor.RED + "you must have your main hand free");
            return;
        }

        TemporalItems.addTemp(player, TemporalItem.INFOBOOK);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setTitle(ChatColor.YELLOW+assetid);
        meta.setAuthor(player.getWorld().getName().replace("_", "'s ") + " Mercantile Exchange");


        List<String> pages = new ArrayList<>();

        StringBuilder centeredSymbol = new StringBuilder();

        String assetSymbol = asset.getAssetType() == 1? Configuration.MAINCURRENCYASSET.getCode() + "/" +assetid:assetid;
        centeredSymbol.append(" ".repeat(Math.max(0, 13 - assetSymbol.length() / 2)));
        centeredSymbol.append(assetSymbol);


        //FRONTPAGE
        StringBuilder frontPage = new StringBuilder("\n\n\n\n");

        String assetName = asset.getName();
        frontPage.append(" ".repeat(12 - assetName.length() / 2));
        frontPage.append(assetName);

        frontPage.append("\n        --------\n");

        frontPage.append(centeredSymbol);

        frontPage.append("\n\n\n\n\n\n\n");

        String footer = NamingUtil.nameType(asset.getAssetType())+ " market";
        frontPage.append(" ".repeat(12 - footer.length() / 2));
        frontPage.append(footer);

        pages.add(frontPage.toString());


        //TICKER
        Candle lastestDay = Candles.getLastDay(assetid, player.getWorld().getFullTime());

        double price = lastestDay.getClose(),
                change = lastestDay.getClose()-lastestDay.getOpen(),
                bid = Orders.getBid(assetid),
                ask = Orders.getAsk(assetid);

        StringBuilder ticker = new StringBuilder();

        String bidask = "Bid " + (bid<0? "NA" : "$"+bid) + " | " + (ask<0? "NA" : "$"+ask) + " Ask";
        ticker.append(" ".repeat(Math.max(0, 13 - bidask.length() / 2)));
        ticker.append(bidask.replace("Bid",ChatColor.GREEN+"Bid").replace("|",ChatColor.RESET+"|"+ChatColor.RED) + ChatColor.RESET);
        ticker.append("\n\n\n\n\n");

        ticker.append(centeredSymbol).append("\n");

        String priceStr = "$"+ price;
        for (int i = 0; i < 12-priceStr.length()/2; i++) {
            ticker.append(" ");
        }
        ticker.append(priceStr).append("\n");

        String dayChange =  change+" "+ Math.ceil(change/lastestDay.getOpen()*10000)/100 + "% today";
        ticker.append(" ".repeat(Math.max(0, 11 - dayChange.length() / 2)));
        ticker.append((change<0?ChatColor.RED:ChatColor.GREEN)+dayChange+ChatColor.RESET).append("\n\n\n\n\n");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("        HH:mm z \n      dd/MM/yyyy");

        ticker.append(now.format(formatter));

        pages.add(ticker.toString());




        // KEY DATA POINTS

        StringBuilder keyDataPoints = new StringBuilder("Key Data Points");

        double volume = lastestDay.getVolume(), Open = lastestDay.getOpen();

        keyDataPoints.append("\n\n\nVolume:\n").append(volume);

        keyDataPoints.append("\n\nOpen:\n$").append(Open);

        keyDataPoints.append("\n\nDay's Range:\n$").append(lastestDay.getLow() + " to $" + lastestDay.getHigh());

        pages.add(keyDataPoints.toString());

        if(asset.getAssetType() > 5){

            StringBuilder aboutPage = new StringBuilder("About\n\n");

            aboutPage.append(asset.getStringData().getFirst());

            pages.add(aboutPage.toString());

        }




        player.sendMessage(NamingUtil.profitableTopSeparator());

        player.sendMessage("Summary:");

        player.sendMessage("      "+asset.getColor() + assetSymbol + "  " + ChatColor.RESET + priceStr + "  " + (change<0?ChatColor.RED:ChatColor.GREEN) + dayChange);

        player.sendMessage("");

        player.sendMessage(ChatColor.GREEN + "Bid: " + (bid<0? ChatColor.GRAY+"No orders" : "$"+bid));
        player.sendMessage(ChatColor.RED  + "Ask: " +  (bid<0? ChatColor.GRAY+"No orders" : "$"+ask));


        player.sendMessage("");
        player.sendMessage("Get graphs:");
        TextComponent textComponent = new TextComponent(ChatColor.YELLOW + "[/asset get " + asset.getCode() + " graph]");
        textComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                "/assets peek " + asset.getCode() + " graph"));
        player.spigot().sendMessage(textComponent);

        player.sendMessage(NamingUtil.profitableBottomSeparator());


        //send info book
        meta.setPages(pages);
        book.setItemMeta(meta);
        player.getInventory().setItemInMainHand(book);

    }

    public static void sendClaimingTag(Player player){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            player.sendMessage(ChatColor.RED + "You must have your main hand free");
            return;
        }

        player.sendMessage(NamingUtil.profitablePrefix() + ChatColor.YELLOW + "Claiming fees are " + Profitable.getInstance().getConfig().getDouble("exchange.commodities.claiming-fees") + " " + Configuration.MAINCURRENCYASSET.getCode() + " per entity");

        TemporalItems.addTemp(player, TemporalItem.CLAIMINGTAG);

        ItemStack nameTag = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = nameTag.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Claiming Tag");
            meta.addEnchant(Enchantment.EFFICIENCY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            nameTag.setItemMeta(meta);
        }

        player.getInventory().setItemInMainHand(nameTag);

    }

    public static void sendDeliveryStick(Player player, boolean items){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            player.sendMessage(ChatColor.RED + "You must have your main hand free");
            return;
        }

        TemporalItems.addTemp(player, items?TemporalItem.ITEMDELIVERYSTICK:TemporalItem.ENTITYDELIVERYSTICK);

        ItemStack nameTag = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = nameTag.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "delivery marker");
            meta.addEnchant(Enchantment.BLAST_PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            nameTag.setItemMeta(meta);
        }

        player.getInventory().setItemInMainHand(nameTag);

    }

    public static void sendGraphMap(Player player, String assetid, long time, String interval){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            player.sendMessage(ChatColor.RED + "You must have your main hand free");
            return;
        }

        Asset asset = Assets.getAssetData(assetid);

        if(asset == null){
            player.sendMessage(ChatColor.RED + "This asset isn't traded here");
            return;
        }

        TemporalItems.addTemp(player, TemporalItem.GRAPHMAP);

        ItemStack graphMap = MapGraphRenderer.createGraphMap(player, assetid, time, interval);
        ItemMeta meta = graphMap.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Graph " + assetid);
            meta.addEnchant(Enchantment.BLAST_PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            graphMap.setItemMeta(meta);
        }

        player.getInventory().setItemInMainHand(graphMap);
    }

}
