package com.faridfaharaj.profitable.tasks.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiButton {

    private final int slot;
    private final ItemStack display;

    GuiButton(ChestGUI gui, Material material, String text, int slot){
        display = new ItemStack(material);
        ItemMeta metaAccountButton = display.getItemMeta();
        metaAccountButton.setDisplayName(text);
        display.setItemMeta(metaAccountButton);

        this.slot = slot;
        gui.getInventory().setItem(slot, display);
    }

    int getSlot(){
        return slot;
    }


}
