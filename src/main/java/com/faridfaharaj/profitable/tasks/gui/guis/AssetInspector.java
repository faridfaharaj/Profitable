package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.AssetButton;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class AssetInspector extends ChestGUI {

    AssetButton tradeButton;

    public AssetInspector(String assetID, World world) {
        super(6, assetID);


        tradeButton = new AssetButton(this, world.getFullTime(),assetID,vectorSlotPosition(2,1));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        if(slot == tradeButton.getSlot()){
            if(click.isLeftClick()){
                tradeButton.trade(player);
            }
            if(click.isRightClick()){
                tradeButton.graphs(player);
            }
        }

    }
}
