package com.faridfaharaj.profitable.tasks.gui;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.util.NamingUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class AssetInspector extends ChestGUI{

    GuiButton returnButton;

    AssetInspector(String assetID, World world) {
        super(6, assetID);

        returnButton = new GuiButton(this, Material.BARRIER, "Back", vectorSlotPosition(4,5));

        Asset asset = Assets.getAssetData(assetID);
        Candle lastestDay = Candles.getLastDay(asset.getCode(), world.getFullTime());


        String symbol = asset.getAssetType() == 1? Configuration.MAINCURRENCYASSET.getCode() + "/" + asset.getCode():asset.getCode();
        String footer = NamingUtil.nameType(asset.getAssetType())+ " market";
        double price = lastestDay.getClose(),
                change = lastestDay.getClose()-lastestDay.getOpen(),
                bid = Orders.getBid(asset.getCode()),
                ask = Orders.getAsk(asset.getCode());

        String priceStr = "$"+ price;
        String dayChange =  change+" "+ Math.ceil(change/lastestDay.getOpen()*10000)/100 + "% today";

        boolean bullish = change < 0;

        fillSlots(0,0,3,0, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);
        fillSlots(0,5,3,5, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);

        fillSlots(5,0,8,0, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);
        fillSlots(5,5,8,5, bullish?Material.LIME_STAINED_GLASS_PANE:Material.RED_STAINED_GLASS_PANE);



        // KEY DATA POINTS

        StringBuilder keyDataPoints = new StringBuilder("Key Data Points");

        double volume = lastestDay.getVolume(), Open = lastestDay.getOpen();

        keyDataPoints.append("\n\n\nVolume:\n").append(volume);

        keyDataPoints.append("\n\nOpen:\n$").append(Open);

        keyDataPoints.append("\n\nDay's Range:\n$").append(lastestDay.getLow() + " to $" + lastestDay.getHigh());
    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

    }
}
