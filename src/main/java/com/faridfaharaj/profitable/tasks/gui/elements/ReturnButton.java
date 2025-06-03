package com.faridfaharaj.profitable.tasks.gui.elements;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ReturnButton extends GuiElement{

    public ReturnButton(ChestGUI gui, int slot) {
        super(gui, new ItemStack(Material.ARROW), Profitable.getLang().get("gui.generic.buttons.return.name"), Profitable.getLang().langToLore("gui.generic.buttons.return.lore"), slot);
    }

}
