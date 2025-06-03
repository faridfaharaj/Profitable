package com.faridfaharaj.profitable.tasks.gui;

import com.faridfaharaj.profitable.Profitable;
import com.tcoded.folialib.FoliaLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    public ChestGUI(int height, Component title){

        if(Profitable.getfolialib().isSpigot()){
            inventory = Bukkit.createInventory(this, 9*height, LegacyComponentSerializer.legacySection().serialize(title));
        }else {
            inventory = Bukkit.createInventory(this, 9*height, title);
        }
    }

    public void openGui(Player player){
        Profitable.getfolialib().getScheduler().runNextTick(task -> {
            player.openInventory(inventory);
        });
    };

    protected static int vectorSlotPosition(int x, int y){
        return x + (y * 9);
    }

    protected void fillSlot(int slot, ItemStack item){
        ItemStack itemStack = new ItemStack(item);
        ItemMeta metaAccountButton = itemStack.getItemMeta();
        metaAccountButton.setDisplayName(" ");
        itemStack.setItemMeta(metaAccountButton);
        inventory.setItem(slot, itemStack);
    }

    protected void fillSlots(int x1, int y1, int x2, int y2, Material item){
        ItemStack itemStack = new ItemStack(item);
        ItemMeta metaAccountButton = itemStack.getItemMeta();
        metaAccountButton.setDisplayName(" ");
        itemStack.setItemMeta(metaAccountButton);

        for(int i = y1; i <= y2; i++){
            for(int j = x1; j <= x2; j++){
                inventory.setItem(vectorSlotPosition(j,i), itemStack);
            }
        }



    }

    protected void fillAll(Material item){

        fillSlots(0,0,8, (inventory.getSize()/9)-1, item);

    }

    public abstract void slotInteracted(Player player, int slot, ClickType click);

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
