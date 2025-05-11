package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.GuiElement;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public final class AssetInspector extends ChestGUI {

    String assetid;

    GuiElement returnButton;
    GuiElement graphsButton;
    GuiElement tradeButton;
    GuiElement infoButton;
    GuiElement tickerElement;

    public AssetInspector(String assetID, World world) {
        super(6, assetID);

        this.assetid = assetID;

        Asset asset = Assets.getAssetData(assetID);

        Candle lastestDay = Candles.getLastDay(asset.getCode(), world.getFullTime());


        String symbol = asset.getAssetType() == 1? Configuration.MAINCURRENCYASSET.getCode() + "/" + asset.getCode():asset.getCode();
        double price = lastestDay.getClose(),
                change = lastestDay.getClose()-lastestDay.getOpen(),
                bid = Orders.getBid(asset.getCode()),
                ask = Orders.getAsk(asset.getCode());

        String priceStr = "$"+ price;
        String dayChange =  change+" "+ Math.ceil(change/lastestDay.getOpen()*10000)/100 + "% today";

        boolean bullish = 0 <= change;

        // KEY DATA POINTS


        double volume = lastestDay.getVolume(), Open = lastestDay.getOpen();


        returnButton = new GuiElement(this, Material.BARRIER, Component.text("Return", NamedTextColor.RED), vectorSlotPosition(4,5));

        graphsButton = new GuiElement(this, Material.FILLED_MAP, Component.text("Graphs", NamedTextColor.YELLOW), vectorSlotPosition(2,3));

        tradeButton = new GuiElement(this, Material.EMERALD, Component.text("Trade", NamedTextColor.YELLOW), vectorSlotPosition(4,2));

        infoButton = new GuiElement(this, Material.BOOK, Component.text("Info", NamedTextColor.YELLOW), vectorSlotPosition(6,3));

        Material assetDisplay = Material.PAPER;
        if(asset.getAssetType() == 2){
            assetDisplay = Material.getMaterial(asset.getCode());
        }if(asset.getAssetType() == 3){
            assetDisplay = Material.getMaterial(asset.getCode()+"_SPAWN_EGG");
        }if(asset.getAssetType() == 1) {
            assetDisplay = Material.EMERALD;
        }

        List<Component> keyDataPoints = new ArrayList<>();

        keyDataPoints.add(Component.text("Bid: ",Configuration.COLORBULLISH).append((bid<0?
                        Component.text("No orders").color(Configuration.COLOREMPTY) :
                        Component.text(bid).color(Configuration.COLORBULLISH))));
        keyDataPoints.add(Component.text("Ask: ",Configuration.COLORBEARISH).append((ask<0?
                Component.text("No orders").color(Configuration.COLOREMPTY) :
                Component.text(ask).color(Configuration.COLORBEARISH))));
        keyDataPoints.add(Component.space());
        keyDataPoints.add(Component.text("Volume: " + volume));
        keyDataPoints.add(Component.text("Open: " + Open));
        keyDataPoints.add(Component.text("Day's Range: " + lastestDay.getLow() + " to " + lastestDay.getHigh()));

        tickerElement = new GuiElement(this, assetDisplay,
                Component.text("").append(Component.text(symbol, asset.getColor()).decorate(TextDecoration.BOLD).hoverEvent(HoverEvent.showText(MessagingUtil.assetSummary(asset))))
                        .append( Component.space().appendSpace().appendSpace().append(Component.text(priceStr))
                                .appendSpace().appendSpace().appendSpace()).append(Component.text(dayChange,(change<0?Configuration.COLORBEARISH:Configuration.COLORBULLISH)))
                , keyDataPoints,vectorSlotPosition(4,0));




        fillSlots(0,0,3,0, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);
        fillSlots(0,5,3,5, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);

        fillSlots(5,0,8,0, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);
        fillSlots(5,5,8,5, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);
    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        if(slot == returnButton.getSlot()){
            this.getInventory().close();
            new MainMenu().openGui(player);
        }else if(slot == graphsButton.getSlot()){
            this.getInventory().close();
            new GraphsMenu(assetid).openGui(player);
        }

    }
}
