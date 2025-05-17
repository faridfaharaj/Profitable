package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.guis.GraphsMenu;
import com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding.BuySellGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AssetButton extends GuiElement {

    Asset asset;
    Candle lastestDay;
    boolean loaded = false;

    public AssetButton(ChestGUI gui, AssetButtonData assetButtonData, int slot) {
        super(gui, new ItemStack(Material.PAPER), Component.text("Loading...", Configuration.COLOREMPTY), null, slot);

        Profitable.getfolialib().getScheduler().runAsync(task -> {

            this.asset = assetButtonData.getAsset();
            this.lastestDay = assetButtonData.getlastCandle();
            double price = lastestDay.getClose(),
                    change = lastestDay.getClose()-lastestDay.getOpen(),
                    volume = lastestDay.getVolume(), Open = lastestDay.getOpen();
            String symbol = asset.getAssetType() == 1? Configuration.MAINCURRENCYASSET.getCode() + "/" + asset.getCode():asset.getCode(),
                    priceStr = "$"+ price,
                    dayChange =  change+" "+ Math.ceil(change/lastestDay.getOpen()*10000)/100 + "% today";

            if(asset.getAssetType() == 2){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()));
            }if(asset.getAssetType() == 3){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()+"_SPAWN_EGG"));
            }if(asset.getAssetType() == 1) {
                this.display = new ItemStack(Material.EMERALD);
            }

            List<Component> keyDataPoints = new ArrayList<>();
            keyDataPoints.add(Component.text(NamingUtil.nameType(asset.getAssetType())));
            keyDataPoints.add(Component.space());
            keyDataPoints.add(Component.text("Volume: " + volume));
            keyDataPoints.add(Component.text("Open: " + Open));
            keyDataPoints.add(Component.text("Day's Range: " + lastestDay.getLow() + " to " + lastestDay.getHigh()));

            keyDataPoints.add(Component.empty());
            keyDataPoints.add(GuiElement.clickAction(ClickType.LEFT, "Trade asset"));
            keyDataPoints.add(GuiElement.clickAction(ClickType.RIGHT, "Get graphs"));

            setDisplayName(Component.text("").append(Component.text(symbol, asset.getColor()).decorate(TextDecoration.BOLD).hoverEvent(HoverEvent.showText(MessagingUtil.assetSummary(asset)))).append( Component.space().appendSpace().appendSpace().append(Component.text(priceStr)).appendSpace().appendSpace().appendSpace()).append(Component.text(dayChange,(change<0?Configuration.COLORBEARISH:Configuration.COLORBULLISH))));
            setLore(keyDataPoints);

            loaded = true;

            this.show(gui);

        });

    }

    public void trade(Player player){
        if(loaded){
            player.getInventory().close();
            Profitable.getfolialib().getScheduler().runAsync(task -> {
                new BuySellGui(asset, lastestDay, Orders.getBidAsk(asset.getCode(), true), Orders.getBidAsk(asset.getCode(), false)).openGui(player);
            });
        }
    }

    public void graphs(Player player){
        if(loaded){
            player.getInventory().close();
            new GraphsMenu(asset.getCode()).openGui(player);
        }
    }
}
