package com.faridfaharaj.profitable.tasks.gui.elements;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiElement {

    protected final int slot;
    protected ItemStack display;

    public GuiElement(ChestGUI gui, ItemStack stack, Component text, List<Component> lore, int slot) {
        display = stack;
        setDisplayName(text);
        setLore(lore);

        this.slot = slot;
        show(gui);
    }

    public static Component clickAction(ClickType click, String action){

        String clickString;

        if(click == null){
            return Component.text("Click to "+ action, NamedTextColor.YELLOW);
        }else {
            clickString = click.toString().replace("_", " ").toLowerCase();
            if(click.isMouseClick()){
                clickString = clickString + " click";
            }
        }
        return Component.text(clickString + " to " + action, NamedTextColor.YELLOW);

    }

    public void setDisplayName(Component text){

        ItemMeta metaAccountButton = display.getItemMeta();
        if(!Profitable.getfolialib().isSpigot()){
            metaAccountButton.displayName(text);
        }else {
            metaAccountButton.setDisplayName(LegacyComponentSerializer.legacySection().serialize(text));
        }
        display.setItemMeta(metaAccountButton);

    }

    public void setLore(List<Component> lore){

        if(lore != null){
            ItemMeta metaAccountButton = display.getItemMeta();

            if(!Profitable.getfolialib().isSpigot()){
                metaAccountButton.lore(lore);
            }else {
                List<String> loreString = new ArrayList<>();
                for(Component component : lore){
                    loreString.add(LegacyComponentSerializer.legacySection().serialize(component));
                }
                metaAccountButton.setLore(loreString);
            }

            display.setItemMeta(metaAccountButton);
        }

    }

    public void show(ChestGUI gui){
        gui.getInventory().setItem(slot, display);
    }

    public void hide(ChestGUI gui){
        gui.getInventory().setItem(slot, new ItemStack(Material.AIR));
    }

    public int getSlot(){
        return slot;
    }

}
