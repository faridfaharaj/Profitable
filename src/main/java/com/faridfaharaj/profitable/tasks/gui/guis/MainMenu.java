package com.faridfaharaj.profitable.tasks.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class MainMenu extends ChestGUI{

    final GuiButton accountButton;
    final GuiButton chestGui;
    final GuiButton MainMenu;

    public MainMenu() {
        super(3, "Exchange's main menu");

        accountButton = new GuiButton(this, Material.PLAYER_HEAD, "Account", vectorSlotPosition(2,1));
        chestGui = new GuiButton(this, Material.EMERALD, "Trade", vectorSlotPosition(4,1));
        MainMenu = new GuiButton(this, Material.BOOK, "Info", vectorSlotPosition(6,1));

    }

    @Override
    public void slotInteracted(Player player , int slot, ClickType click) {

        if(slot == accountButton.getSlot()){

            if(click.isLeftClick()){
                getInventory().close();
            }

        }else if(slot == chestGui.getSlot()){



        } else if(slot == MainMenu.getSlot()) {



        }

    }
}
