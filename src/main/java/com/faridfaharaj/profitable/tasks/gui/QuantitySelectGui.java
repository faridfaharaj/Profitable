package com.faridfaharaj.profitable.tasks.gui;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public abstract class QuantitySelectGui extends ChestGUI {

    final GuiElement[] buttons = new GuiElement[10];

    protected double amount;
    final boolean enfoceInt;

    public QuantitySelectGui(Component text, boolean decimal, boolean enforceInteger, double defaultAmount) {
        super(5, text);

        this.amount = Math.max(defaultAmount, 1);

        fillAll(Material.BLACK_STAINED_GLASS_PANE);

        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 4));

        int big = decimal?50:64;
        int mid = decimal?10:16;

        this.enfoceInt = enforceInteger;

        // -64/-50
        buttons[1] = new GuiElement(this, new ItemStack(Material.RED_CANDLE, big), Profitable.getLang().get("gui.number-selector.buttons.subtract.name",
                Map.entry("%number%", String.valueOf(big))
                ), null, vectorSlotPosition(1, 2));
        // -16/-10
        buttons[2] = new GuiElement(this, new ItemStack(Material.RED_CANDLE, mid), Profitable.getLang().get("gui.number-selector.buttons.subtract.name",
                Map.entry("%number%", String.valueOf(mid))
        ), null, vectorSlotPosition(2, 2));
        // -1/-1
        buttons[3] = new GuiElement(this, new ItemStack(Material.RED_CANDLE, 1), Profitable.getLang().get("gui.number-selector.buttons.subtract.name",
                Map.entry("%number%", String.valueOf(1))
        ), null, vectorSlotPosition(3, 2));

        // middle
        buttons[4] = submitButton(vectorSlotPosition(4, 2));

        // 1/1
        buttons[5] = new GuiElement(this, new ItemStack(Material.LIME_CANDLE, 1), Profitable.getLang().get("gui.number-selector.buttons.add.name",
                Map.entry("%number%", String.valueOf(1))
        ),
                null, vectorSlotPosition(5, 2));
        // 16/10
        buttons[6] = new GuiElement(this, new ItemStack(Material.LIME_CANDLE, mid), Profitable.getLang().get("gui.number-selector.buttons.add.name",
                Map.entry("%number%", String.valueOf(mid))
        ), null, vectorSlotPosition(6, 2));
        // 64/50
        buttons[7] = new GuiElement(this, new ItemStack(Material.LIME_CANDLE, big), Profitable.getLang().get("gui.number-selector.buttons.add.name",
                Map.entry("%number%", String.valueOf(big))
        ), null, vectorSlotPosition(7, 2));

        // add 0
        buttons[8] = new GuiElement(this, new ItemStack(Material.SHEARS), Profitable.getLang().get("gui.number-selector.buttons.divide-ten.name"), null, vectorSlotPosition(3, 4));

        // remove 0
        buttons[9] = new GuiElement(this, new ItemStack(Material.SUGAR), Profitable.getLang().get("gui.number-selector.buttons.times-ten.name"), null, vectorSlotPosition(5, 4));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {
        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    player.closeInventory();
                    onReturn(player);
                    return;
                }

                if(button == buttons[4]){
                    onSubmitAmount(player, amount);
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

                if (button == buttons[8]) {
                    amount /= 10;
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.ENTITY_SHEEP_SHEAR, 1,1);
                    });
                }else if (button == buttons[9]) {
                    amount *= 10;
                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        player.playSound(player, Sound.BLOCK_BREWING_STAND_BREW, 1,1);
                    });
                }

                if(enfoceInt){
                    amount = Math.max(1, (int)amount);
                }else {
                    amount = Math.max(0.001, amount);
                }

                onAmountUpdate(amount);
            }
        }
    }

    protected abstract void onAmountUpdate(double newAmount);

    protected GuiElement getSubmitButton(){
        return buttons[4];
    }

    protected abstract GuiElement submitButton(int slot);

    protected abstract void onSubmitAmount(Player player, double amount);

    protected abstract void onReturn(Player player);
}
