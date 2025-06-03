package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.commands.WalletCommand;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.tasks.gui.QuantitySelectGui;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class DepositWithdrawalGui extends QuantitySelectGui {

    boolean depositing;
    Asset asset;

    AssetCache[][] assetCache;
    public DepositWithdrawalGui(Asset asset, boolean depositing, AssetCache[][] assetCache) {
        super(Profitable.getLang().get(depositing?"gui.deposit-withdrawal.title-deposit":"gui.deposit-withdrawal.title-withdrawal"), asset.getAssetType() != 2 && asset.getAssetType() != 3, asset.getAssetType() == 2 || asset.getAssetType() == 3, 1);
        this.assetCache = assetCache;
        this.depositing = depositing;
        this.asset = asset;
        onAmountUpdate(amount);
    }

    @Override
    protected void onAmountUpdate(double newAmount) {

        getSubmitButton().setDisplayName(Profitable.getLang().get(depositing?"gui.deposit-withdrawal.buttons.submit-deposit.name":"gui.deposit-withdrawal.buttons.submit-withdrawal.name", Map.entry("%amount%", String.valueOf(newAmount)), Map.entry("%asset%", asset.getCode())));
        List<Component> lore = Profitable.getLang().langToLore(depositing?"gui.deposit-withdrawal.buttons.submit-deposit.lore":"gui.deposit-withdrawal.buttons.submit-withdrawal.lore", Map.entry("%asset%", asset.getCode()));
        getSubmitButton().setLore(lore);
        getSubmitButton().show(this);

    }

    @Override
    protected GuiElement submitButton(int slot) {

        ItemStack display = new ItemStack(Material.PAPER);

        Component name = Profitable.getLang().get(depositing?"gui.deposit-withdrawal.buttons.submit-deposit.name":"gui.deposit-withdrawal.buttons.submit-withdrawal.name", Map.entry("%amount%", String.valueOf(amount)));
        List<Component> lore = Profitable.getLang().langToLore(depositing?"gui.deposit-withdrawal.buttons.submit-deposit.lore":"gui.deposit-withdrawal.buttons.submit-withdrawal.lore");

        return new GuiElement(this, display, name, lore, slot);
    }

    @Override
    protected void onSubmitAmount(Player player, double amount) {

        player.closeInventory();
        if(depositing){

            WalletCommand.depositAsset(asset, amount, player);

        }else {

            WalletCommand.withdrawAsset(asset, amount, player);

        }


    }

    @Override
    protected void onReturn(Player player) {
        new HoldingsMenu(player, assetCache).openGui(player);
    }
}
