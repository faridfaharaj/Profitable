package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.guis.GraphsMenu;
import com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding.BuySellGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class AssetButton extends GuiElement {

    int[] index;
    boolean loaded;

    public AssetButton(ChestGUI gui, AssetCache assetData, int[] index, int slot) {
        super(gui, new ItemStack(Material.PAPER), Component.text("Loading...", Configuration.COLOREMPTY), null, slot);

        this.index = index;
        double price = assetData.getlastCandle().getClose(),
                change = assetData.getlastCandle().getClose()-assetData.getlastCandle().getOpen(),
                volume = assetData.getlastCandle().getVolume(), Open = assetData.getlastCandle().getOpen();
        String symbol = assetData.getAsset().getAssetType() == 1? Configuration.MAINCURRENCYASSET.getCode() + "/" + assetData.getAsset().getCode():assetData.getAsset().getCode(),
                priceStr = "$"+ MessagingUtil.formatNumber(price),
                dayChange =  MessagingUtil.formatNumber(change)+" "+ MessagingUtil.formatNumber(Math.ceil(change/assetData.getlastCandle().getOpen()*10000)/100) + "% today";

        if(assetData.getAsset().getAssetType() == 2){
            this.display = new ItemStack(Material.getMaterial(assetData.getAsset().getCode()));
        }if(assetData.getAsset().getAssetType() == 3){
            this.display = new ItemStack(Material.getMaterial(assetData.getAsset().getCode()+"_SPAWN_EGG"));
        }if(assetData.getAsset().getAssetType() == 1) {
            this.display = new ItemStack(Material.EMERALD);
            ItemMeta meta = this.display.getItemMeta();
            meta.setEnchantmentGlintOverride(true);
            this.display.setItemMeta(meta);
        }

        List<Component> keyDataPoints = new ArrayList<>();
        keyDataPoints.add(Component.text(NamingUtil.nameType(assetData.getAsset().getAssetType()), Configuration.GUICOLORSUBTITLE));
        keyDataPoints.add(Component.empty());
        keyDataPoints.add(Component.text(assetData.getAsset().getName(), Configuration.GUICOLORTEXT));
        keyDataPoints.add(Component.text(" - ", Configuration.GUICOLORSUBTITLE).append(Component.text("Volume: ", Configuration.GUICOLORTEXT)).append(Component.text(volume, Configuration.GUICOLORHIGHLIGHT)));
        keyDataPoints.add(Component.text(" - ", Configuration.GUICOLORSUBTITLE).append(Component.text("Open: ", Configuration.GUICOLORTEXT)).append(Component.text(Open, Configuration.GUICOLORHIGHLIGHT)));
        keyDataPoints.add(Component.text(" - ", Configuration.GUICOLORSUBTITLE).append(Component.text("Day's Range: ", Configuration.GUICOLORTEXT)).append(Component.text(assetData.getlastCandle().getLow() + " to " + assetData.getlastCandle().getHigh(), Configuration.GUICOLORHIGHLIGHT)));

        keyDataPoints.add(Component.empty());
        keyDataPoints.add(GuiElement.clickAction(ClickType.LEFT, "Trade asset"));
        keyDataPoints.add(GuiElement.clickAction(ClickType.RIGHT, "Get graphs"));

        setDisplayName(Component.text("").append(Component.text(symbol, assetData.getAsset().getColor()).hoverEvent(HoverEvent.showText(MessagingUtil.assetSummary(assetData.getAsset())))).append( Component.space().appendSpace().appendSpace().append(Component.text(priceStr)).appendSpace().appendSpace().appendSpace()).append(Component.text(dayChange,(change<0?Configuration.COLORBEARISH:Configuration.COLORBULLISH))));
        setLore(keyDataPoints);

        loaded = true;

        this.show(gui);

    }

    public void trade(Player player, AssetCache[][] cache){
        if(loaded){
            player.closeInventory();
            Profitable.getfolialib().getScheduler().runAsync(task -> {
                new BuySellGui(cache, cache[index[0]][index[1]], Orders.getBidAsk(cache[index[0]][index[1]].getAsset().getCode(), true), Orders.getBidAsk(cache[index[0]][index[1]].getAsset().getCode(), false)).openGui(player);
            });
        }
    }

    public void graphs(Player player, AssetCache[][] cache){
        if(loaded){
            player.closeInventory();
            new GraphsMenu(cache[index[0]][index[1]].getAsset().getCode(), cache).openGui(player);
        }
    }
}
