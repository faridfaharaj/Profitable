package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.guis.DepositWithdrawalGui;
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

public final class AssetHolderButton extends GuiElement {

    Asset asset;
    Candle lastestDay;
    boolean loaded = false;

    public AssetHolderButton(ChestGUI gui, AssetCache assetButtonData, int slot) {
        super(gui, new ItemStack(Material.PAPER), Component.text("Loading...", Configuration.COLOREMPTY), null, slot);

        Profitable.getfolialib().getScheduler().runAsync(task -> {

            this.asset = assetButtonData.getAsset();
            this.lastestDay = assetButtonData.getlastCandle();

            if(asset.getAssetType() == 2){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()));
            }if(asset.getAssetType() == 3){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()+"_SPAWN_EGG"));
            }if(asset.getAssetType() == 1) {
                this.display = new ItemStack(Material.EMERALD);
                ItemMeta meta = this.display.getItemMeta();
                meta.setEnchantmentGlintOverride(true);
                this.display.setItemMeta(meta);
            }

            setDisplayName(Component.text(asset.getCode(),asset.getColor()));

            List<Component> lore = new ArrayList<>();

            lore.add(Component.text(NamingUtil.nameType(asset.getAssetType())));
            lore.add(Component.empty());
            lore.add(Component.text("Owned: ").append(MessagingUtil.assetAmmount(asset, lastestDay.getVolume())));
            lore.add(Component.empty());
            lore.add(Component.text("Market price: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, lastestDay.getClose())));
            lore.add(Component.text("Total value: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, lastestDay.getVolume()*lastestDay.getClose())));
            lore.add(Component.empty());
            lore.add(GuiElement.clickAction(ClickType.LEFT, "withdraw"));
            lore.add(GuiElement.clickAction(ClickType.RIGHT, "deposit"));

            setLore(lore);

            loaded = true;

            this.show(gui);

        });
    }

    public void manage(Player player, boolean depositing, AssetCache[][] assetCache){
        new DepositWithdrawalGui(asset, depositing, assetCache).openGui(player);
    }
}
