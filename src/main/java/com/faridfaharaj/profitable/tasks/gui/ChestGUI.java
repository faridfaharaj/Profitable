package com.faridfaharaj.profitable.tasks.gui;

import com.faridfaharaj.profitable.Profitable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public abstract class ChestGUI implements InventoryHolder {

    private final Inventory inventory;

    public ChestGUI(int height, String title){
        inventory = Bukkit.createInventory(this, 9*height, title);
    }

    public void openGui(Player player){
        Profitable.getfolialib().getScheduler().runNextTick(task -> {
            player.openInventory(inventory);
        });
    };

    protected static int vectorSlotPosition(int x, int y){
        return x + (y * 9);
    }

    protected void setSlot(int x, int y, ItemStack item){
        inventory.setItem(vectorSlotPosition(x, y), item);
    }

    protected void fillSlots(int x1, int y1, int x2, int y2, Material item){
        ItemStack itemStack = new ItemStack(item);
        ItemMeta metaAccountButton = itemStack.getItemMeta();
        metaAccountButton.setDisplayName(" ");
        itemStack.setItemMeta(metaAccountButton);
        for(int i = vectorSlotPosition(x1, y1); i <= vectorSlotPosition(x2, y2); i++){
            inventory.setItem(i, itemStack);
        }

    }

    public abstract void slotInteracted(Player player, int slot, ClickType click);

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
