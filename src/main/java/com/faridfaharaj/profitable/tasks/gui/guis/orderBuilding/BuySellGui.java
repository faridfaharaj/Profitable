package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.guis.MainMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public final class OrderGui extends ChestGUI {

    GuiElement[] buttons = new GuiElement[3];

    public OrderGui() {
        super(3, "Order builder");

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));

        buttons[1] = new GuiElement(this, Material.RED_DYE, Component.text("Sell Order", Configuration.COLORBEARISH),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Place sell order")
                ), vectorSlotPosition(5, 1));
        buttons[2] = new GuiElement(this, Material.LIME_DYE, Component.text("Buy Order", Configuration.COLORBULLISH),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Place buy order")
                ), vectorSlotPosition(3, 1));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    this.getInventory().close();
                    new MainMenu().openGui(player);
                }

            }
        }

    }
}
