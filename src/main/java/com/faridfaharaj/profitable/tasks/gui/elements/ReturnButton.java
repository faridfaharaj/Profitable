package com.faridfaharaj.profitable.tasks.gui.elements;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ReturnButton extends GuiElement{

    public ReturnButton(ChestGUI gui, int slot) {
        super(gui, new ItemStack(Material.ARROW), Profitable.getLang().get("gui.generic.buttons.return.name"), Profitable.getLang().langToLore("gui.generic.buttons.return.lore"), slot);
    }

}
