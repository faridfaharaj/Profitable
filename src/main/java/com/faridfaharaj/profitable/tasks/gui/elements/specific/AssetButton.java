package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.guis.GraphsMenu;
import com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding.BuySellGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AssetButton extends GuiElement {

    int[] index;
    boolean loaded;

    public AssetButton(ChestGUI gui, AssetCache assetData, int[] index, int slot) {
        super(gui, new ItemStack(Material.PAPER), Component.text("Loading...", Configuration.COLOREMPTY), null, slot);

        this.index = index;

        double price = assetData.getlastCandle().getClose(),
                change = assetData.getlastCandle().getClose()-assetData.getlastCandle().getOpen(),
                volume = assetData.getlastCandle().getVolume(), open = assetData.getlastCandle().getOpen();

        String symbol = assetData.getAsset().getAssetType() == Asset.AssetType.CURRENCY? Configuration.MAINCURRENCYASSET.getCode() + "/" + assetData.getAsset().getCode():assetData.getAsset().getCode();

        this.display = assetData.getAsset().getStack();

        if(assetData.getAsset().getAssetType() == Asset.AssetType.CURRENCY) {
            ItemMeta meta = this.display.getItemMeta();
            meta.setEnchantmentGlintOverride(true);
            this.display.setItemMeta(meta);
        }

        setDisplayName(
                Profitable.getLang().get("gui.asset-explorer.buttons.asset.name",
                        Map.entry("%asset%", "<color:"+assetData.getAsset().getColor().asHexString() + ">" + symbol + "</color>"),
                        Map.entry("%price%", MessagingUtil.formatNumber(price)),
                        Map.entry("%price_change%", "<color:"+ (change<0?Configuration.COLORBEARISH:Configuration.COLORBULLISH).asHexString() + ">"+ MessagingUtil.formatNumber(change) + "</color>"),
                        Map.entry("%percentage_change%", "<color:"+ (change<0?Configuration.COLORBEARISH:Configuration.COLORBULLISH).asHexString() + ">"+ MessagingUtil.formatNumber(Math.ceil(change/assetData.getlastCandle().getOpen()*10000)/100) + "%</color>")

                )
        );

        setLore(Profitable.getLang().langToLore("gui.asset-explorer.buttons.asset.lore",

                Map.entry("%asset_category%", NamingUtil.nameType(assetData.getAsset().getAssetType())),
                Map.entry("%asset_name%", assetData.getAsset().getName()),
                Map.entry("%volume%", MessagingUtil.formatVolume(volume)),
                Map.entry("%open_price%", String.valueOf(open)),
                Map.entry("%range_low%", String.valueOf(assetData.getlastCandle().getLow())),
                Map.entry("%range_high%", String.valueOf(assetData.getlastCandle().getHigh()))

        ));

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
