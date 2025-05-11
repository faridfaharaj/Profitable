package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.GuiElement;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class MainMenu extends ChestGUI {

    final GuiElement accountButton;
    final GuiElement chestGui;
    final GuiElement MainMenu;

    public MainMenu() {
        super(3, "Exchange's main menu");

        accountButton = new GuiElement(this, Material.PLAYER_HEAD, Component.text("Account"), vectorSlotPosition(2,1));
        chestGui = new GuiElement(this, Material.EMERALD, Component.text("Trade"), vectorSlotPosition(4,1));
        MainMenu = new GuiElement(this, Material.BOOK, Component.text("Info"), vectorSlotPosition(6,1));

    }

    @Override
    public void slotInteracted(Player player , int slot, ClickType click) {

        if(slot == accountButton.getSlot()){


        }else if(slot == chestGui.getSlot()){


        } else if(slot == MainMenu.getSlot()) {


        }

    }
}
