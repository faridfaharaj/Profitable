package com.faridfaharaj.profitable.tasks.gui.guis;

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

public final class DepositWithdrawalGui extends QuantitySelectGui {

    boolean depositing;
    Asset asset;

    AssetCache[][] assetCache;
    public DepositWithdrawalGui(Asset asset, boolean depositing, AssetCache[][] assetCache) {
        super("Select amount to " + (depositing?"deposit.":"withdraw."), true, asset.getAssetType() == 2 && asset.getAssetType() == 3, 1);
        this.assetCache = assetCache;
        this.depositing = depositing;
        this.asset = asset;
    }

    @Override
    protected void onAmountUpdate(double newAmount) {

        getSubmitButton().setDisplayName(Component.text("Amount: ").append(Component.text(newAmount, NamedTextColor.YELLOW)));
        List<Component> lore = List.of(
                Component.empty(),
                GuiElement.clickAction(null, (depositing?"Deposit":"Withdraw") + " this amount")
        );
        getSubmitButton().setLore(lore);
        getSubmitButton().show(this);

    }

    @Override
    protected GuiElement submitButton(int slot) {

        ItemStack display = new ItemStack(Material.PAPER);

        Component name = Component.text("Amount: ").append(Component.text(1.0, NamedTextColor.YELLOW));
        List<Component> lore = List.of(
                Component.empty(),
                GuiElement.clickAction(null, (depositing?"Deposit":"Withdraw") + " this amount")
        );

        return new GuiElement(this, display, name, lore, slot);
    }

    @Override
    protected void onSubmitAmount(Player player, double amount) {

        this.getInventory().close();
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
