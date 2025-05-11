package com.faridfaharaj.profitable.tasks.gui;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiElement {

    private final int slot;
    private final ItemStack display;

    public GuiElement(ChestGUI gui, Material material, Component text, int slot){
        display = new ItemStack(material);
        ItemMeta metaAccountButton = display.getItemMeta();
        if(Profitable.isPaper()){
            metaAccountButton.displayName(text);
        }else {
            metaAccountButton.setDisplayName(LegacyComponentSerializer.legacySection().serialize(text));
        }
        display.setItemMeta(metaAccountButton);

        this.slot = slot;
        gui.getInventory().setItem(slot, display);
    }

    public GuiElement(ChestGUI gui, Material material, Component text, List<Component> lore, int slot){
        display = new ItemStack(material);
        ItemMeta metaAccountButton = display.getItemMeta();
        if(Profitable.isPaper()){
            metaAccountButton.displayName(text);
            metaAccountButton.lore(lore);
        }else {
            metaAccountButton.setDisplayName(LegacyComponentSerializer.legacySection().serialize(text));
            List<String> loreString = new ArrayList<>();
            for(Component component : lore){
                loreString.add(LegacyComponentSerializer.legacySection().serialize(component));
            }
            metaAccountButton.setLore(loreString);
        }
        display.setItemMeta(metaAccountButton);

        this.slot = slot;
        gui.getInventory().setItem(slot, display);
    }

    public int getSlot(){
        return slot;
    }

}
