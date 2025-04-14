package com.faridfaharaj.profitable.tasks;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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
            MessagingUtil.sendError(player, "This asset isn't traded here");
            return;
        }

        /*if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            TextUtil.sendError(player, "you must have your main hand free");
            return;
        }*/




        String symbol = asset.getAssetType() == 1? Configuration.MAINCURRENCYASSET.getCode() + "/" + asset.getCode():asset.getCode();
        String footer = NamingUtil.nameType(asset.getAssetType())+ " market";
        Candle lastestDay = Candles.getLastDay(assetid, player.getWorld().getFullTime());
        double price = lastestDay.getClose(),
                change = lastestDay.getClose()-lastestDay.getOpen(),
                bid = Orders.getBid(assetid),
                ask = Orders.getAsk(assetid);

        String priceStr = "$"+ price;
        String dayChange =  change+" "+ Math.ceil(change/lastestDay.getOpen()*10000)/100 + "% today";




        // KEY DATA POINTS

        StringBuilder keyDataPoints = new StringBuilder("Key Data Points");

        double volume = lastestDay.getVolume(), Open = lastestDay.getOpen();

        keyDataPoints.append("\n\n\nVolume:\n").append(volume);

        keyDataPoints.append("\n\nOpen:\n$").append(Open);

        keyDataPoints.append("\n\nDay's Range:\n$").append(lastestDay.getLow() + " to $" + lastestDay.getHigh());



        //summary
        MessagingUtil.sendCustomMessage( player,
                MessagingUtil.profitableTopSeparator("Overview", "----------------").appendNewline()
                        .appendNewline()
                        .append(Component.text("     ")).append(Component.text(symbol, asset.getColor()).decorate(TextDecoration.BOLD).hoverEvent(HoverEvent.showText(Component.text("").append(Component.text(asset.getName(), asset.getColor())).appendNewline().append(Component.text(NamingUtil.nameType(asset.getAssetType()), NamedTextColor.GRAY)))))

                        .append( Component.text( "  ").append(Component.text(priceStr).hoverEvent(HoverEvent.showText(Component.text("Last price traded")))).append( Component.text( "  ")))
                        .append(Component.text(dayChange,(change<0?Configuration.COLORBEARISH:Configuration.COLORBULLISH)).hoverEvent(HoverEvent.showText(Component.text("How much the price changed today, both in difference and percentage")))).appendNewline()

                        .appendNewline()
                        .append(Component.text("Bid: ",Configuration.COLORBULLISH).hoverEvent(HoverEvent.showText(Component.text("Highest price at which you can sell an asset")))
                                .append((bid<0?
                                        Component.text("No orders").color(Configuration.COLOREMPTY) :
                                        Component.text("$"+bid).color(Configuration.COLORBULLISH))).appendNewline())
                        .append(Component.text("Ask: ",Configuration.COLORBEARISH).hoverEvent(HoverEvent.showText(Component.text("Lowest price at which you can buy an asset")))
                                .append((ask<0?
                                        Component.text("No orders").color(Configuration.COLOREMPTY) :
                                        Component.text("$"+ask).color(Configuration.COLORBEARISH)))).appendNewline()
                        .appendNewline()
                        .append(Component.text("[View graphs]",Configuration.COLORINFO).clickEvent(ClickEvent.runCommand("/asset " + asset.getCode() + " graph")).hoverEvent(HoverEvent.showText(Component.text("/asset " + asset.getCode() + " graph", Configuration.COLORINFO)))).appendNewline()
                        .append(MessagingUtil.profitableBottomSeparator())
        );

        Component bookTitle = Component.text("Encyclopedia of cats");
        Component bookAuthor = Component.text("kashike");
        Collection<Component> bookPages = new ArrayList<>();
        bookPages.add(Component.text("asdf"));

        Book myBook = Book.book(bookTitle, bookAuthor, bookPages);
        Audience audience = Profitable.getBukkitAudiences().player(player);
        audience.openBook(myBook);


    }

    public static void sendClaimingTag(Player player){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            MessagingUtil.sendError(player, "you must have your main hand free");
            return;
        }

        MessagingUtil.sendWarning(player,"Claiming fees are " + Profitable.getInstance().getConfig().getDouble("exchange.commodities.fees.entity-claiming-fees") + " " + Configuration.MAINCURRENCYASSET.getCode() + " per entity");

        TemporalItems.addTemp(player, TemporalItem.CLAIMINGTAG);

        ItemStack nameTag = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = nameTag.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§dClaiming Tag");
            meta.addEnchant(Enchantment.EFFICIENCY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            nameTag.setItemMeta(meta);
        }

        player.getInventory().setItemInMainHand(nameTag);

    }

    public static void sendDeliveryStick(Player player, boolean items){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            MessagingUtil.sendError(player, "you must have your main hand free");
            return;
        }

        TemporalItems.addTemp(player, items?TemporalItem.ITEMDELIVERYSTICK:TemporalItem.ENTITYDELIVERYSTICK);

        ItemStack nameTag = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = nameTag.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§ddelivery marker");
            meta.addEnchant(Enchantment.BLAST_PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            nameTag.setItemMeta(meta);
        }

        player.getInventory().setItemInMainHand(nameTag);

    }

    public static void sendGraphMap(Player player, String assetid, long time, String interval){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            MessagingUtil.sendError(player, "you must have your main hand free");
            return;
        }

        Asset asset = Assets.getAssetData(assetid);

        if(asset == null){
            MessagingUtil.sendError(player, "This asset isn't traded here");
            return;
        }

        TemporalItems.addTemp(player, TemporalItem.GRAPHMAP);

        ItemStack graphMap = MapGraphRenderer.createGraphMap(player, assetid, time, interval);
        ItemMeta meta = graphMap.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§dGraph " + assetid);
            meta.addEnchant(Enchantment.BLAST_PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            graphMap.setItemMeta(meta);
        }

        player.getInventory().setItemInMainHand(graphMap);
    }

}
