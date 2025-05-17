package com.faridfaharaj.profitable.tasks.gui.elements.assetSpecific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AssetHolderButton extends GuiElement {

    Asset asset;
    Candle lastestDay;
    boolean loaded = false;

    public AssetHolderButton(ChestGUI gui, long fullTime, String assetId, double amount, int slot) {
        super(gui, new ItemStack(Material.PAPER), Component.text("Loading...", Configuration.COLOREMPTY), null, slot);

        Profitable.getfolialib().getScheduler().runAsync(task -> {

            this.asset = Assets.getAssetData(assetId);
            this.lastestDay = Candles.getLastDay(asset.getCode(), fullTime);

            if(asset.getAssetType() == 2){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()));
            }if(asset.getAssetType() == 3){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()+"_SPAWN_EGG"));
            }if(asset.getAssetType() == 1) {
                this.display = new ItemStack(Material.EMERALD);
            }

            setDisplayName(Component.text(asset.getCode(),asset.getColor()));

            List<Component> lore = new ArrayList<>();

            lore.add(Component.text(NamingUtil.nameType(asset.getAssetType())));
            lore.add(Component.empty());
            lore.add(Component.text("Owned: ").append(MessagingUtil.assetAmmount(asset, amount)));
            lore.add(Component.empty());
            lore.add(Component.text("Market price: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, lastestDay.getClose())));
            lore.add(Component.text("Total value: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, amount*lastestDay.getClose())));
            lore.add(Component.empty());
            lore.add(GuiElement.clickAction(null, "Withdraw"));

            setLore(lore);

            loaded = true;

            this.show(gui);

        });
    }
}
