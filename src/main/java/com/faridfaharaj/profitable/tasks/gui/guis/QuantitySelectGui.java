package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class QuantitySelectGui extends ChestGUI {

    final GuiElement[] buttons = new GuiElement[8];

    int amount = 1;

    public QuantitySelectGui() {
        super(5, "Units");

        fillAll(Material.BLACK_STAINED_GLASS_PANE);

        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 4));


        // -64/-50
        buttons[1] = new GuiElement(this, new ItemStack(Material.GRAY_DYE, 64), Component.text("Remove 64"),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Remove")
                ), vectorSlotPosition(1, 2));
        // -16/-10
        buttons[2] = new GuiElement(this, new ItemStack(Material.GRAY_DYE, 16), Component.text("Remove 16"),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Remove")
                ), vectorSlotPosition(2, 2));
        // -1/-1
        buttons[3] = new GuiElement(this, new ItemStack(Material.GRAY_DYE, 1), Component.text("Remove 1"),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Remove")
                ), vectorSlotPosition(3, 2));

        // middle
        buttons[4] = new GuiElement(this, new ItemStack(Material.PAPER, 1), Component.text(amount),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Select this amount")
                ), vectorSlotPosition(4, 2));

        // 1/1
        buttons[5] = new GuiElement(this, new ItemStack(Material.GRAY_DYE, 1), Component.text("Add 1"),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Add")
                ), vectorSlotPosition(5, 2));
        // 16/10
        buttons[6] = new GuiElement(this, new ItemStack(Material.GRAY_DYE, 16), Component.text("Add 16"),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Add")
                ), vectorSlotPosition(6, 2));
        // 64/50
        buttons[7] = new GuiElement(this, new ItemStack(Material.GRAY_DYE, 64), Component.text("Add 64"),
                List.of(
                        GuiElement.clickAction(ClickType.LEFT, "Add")
                ), vectorSlotPosition(7, 2));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {
        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    this.getInventory().close();
                    new OrderTypeGui().openGui(player);
                    return;
                }

                if(button == buttons[4]){
                    this.getInventory().close();
                    new ConfirmOrder().openGui(player);
                    return;
                }

                if (button == buttons[1]) {
                    amount -= getInventory().getItem(buttons[1].getSlot()).getAmount();
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1,1);
                    });
                } else if (button == buttons[2]) {
                    amount -= getInventory().getItem(buttons[2].getSlot()).getAmount();
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1,1);
                    });
                } else if (button == buttons[3]) {
                    amount -= getInventory().getItem(buttons[3].getSlot()).getAmount();
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1,1);
                    });
                } else if (button == buttons[5]) {
                    amount += getInventory().getItem(buttons[5].getSlot()).getAmount();
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1,1);
                    });
                } else if (button == buttons[6]) {
                    amount += getInventory().getItem(buttons[6].getSlot()).getAmount();
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1,1);
                    });
                } else if (button == buttons[7]) {
                    amount += getInventory().getItem(buttons[7].getSlot()).getAmount();
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1,1);
                    });
                }

                buttons[4].setDisplayName(Component.text(amount + " EMERALD"));
                buttons[4].show(this);

            }
        }
    }
}
