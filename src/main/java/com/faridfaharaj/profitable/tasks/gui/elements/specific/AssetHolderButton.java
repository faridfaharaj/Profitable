package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.guis.DepositWithdrawalGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public final class AssetHolderButton extends GuiElement {

    Asset asset;
    Candle lastestDay;
    boolean loaded = false;

    public AssetHolderButton(ChestGUI gui, AssetCache assetButtonData, int slot) {
        super(gui, new ItemStack(Material.PAPER), Component.text("Loading...", Configuration.COLOREMPTY), null, slot);

        Profitable.getfolialib().getScheduler().runAsync(task -> {

            this.asset = assetButtonData.getAsset();
            this.lastestDay = assetButtonData.getlastCandle();

            if(asset.getAssetType() == Asset.AssetType.COMMODITY_ITEM){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()));
            }if(asset.getAssetType() == Asset.AssetType.COMMODITY_ENTITY){
                this.display = new ItemStack(Material.getMaterial(asset.getCode()+"_SPAWN_EGG"));
            }if(asset.getAssetType() == Asset.AssetType.CURRENCY) {
                this.display = new ItemStack(Material.EMERALD);
                ItemMeta meta = this.display.getItemMeta();
                meta.setEnchantmentGlintOverride(true);
                this.display.setItemMeta(meta);
            }

            setDisplayName(Component.text(asset.getCode(),asset.getColor()));

            List<Component> lore = Profitable.getLang().langToLore("gui.wallet.buttons.asset-holding.lore",
                    Map.entry("%asset_type%", NamingUtil.nameType(asset.getAssetType())),
                    Map.entry("%owned_asset_amount%", MessagingUtil.assetAmmount(asset, lastestDay.getVolume())),
                    Map.entry("%price_asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, lastestDay.getClose())),
                    Map.entry("%value_asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, lastestDay.getVolume()*lastestDay.getClose()))
            );

            setLore(lore);


            loaded = true;

            this.show(gui);

        });
    }

    public void manage(Player player, boolean depositing, AssetCache[][] assetCache){
        new DepositWithdrawalGui(asset, depositing, assetCache).openGui(player);
    }
}
