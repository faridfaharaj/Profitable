package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class GraphsMenu extends ChestGUI {

    String assetid;

    final GuiElement returnButton;

    final GuiElement graph1MButton;
    final GuiElement graph3MButton;
    final GuiElement graph6MButton;
    final GuiElement graph1YButton;
    final GuiElement graph2YButton;

    AssetCache[][] cache;

    public GraphsMenu(String assetID, AssetCache[][] cache) {
        super(3, "Graphs for " + assetID);

        this.assetid = assetID;
        this.cache = cache;

        List<Component> buttonInstructions = new ArrayList<>();
        buttonInstructions.add(Component.text(assetID, Configuration.GUICOLORSUBTITLE));
        buttonInstructions.add(Component.empty());
        buttonInstructions.add(GuiElement.clickAction(null, "view graph"));

        fillAll(Material.BLACK_STAINED_GLASS_PANE);

        returnButton = new ReturnButton(this, vectorSlotPosition(0,2));

        ItemStack mapStack = new ItemStack(Material.FILLED_MAP);

        graph1MButton = new GuiElement(this, mapStack, Component.text("1 Month", Configuration.GUICOLORTITLE), buttonInstructions, vectorSlotPosition(2,1));
        graph3MButton = new GuiElement(this, mapStack, Component.text("3 Months", Configuration.GUICOLORTITLE), buttonInstructions, vectorSlotPosition(3,1));
        graph6MButton = new GuiElement(this, mapStack, Component.text("6 Months", Configuration.GUICOLORTITLE), buttonInstructions, vectorSlotPosition(4,1));
        graph1YButton = new GuiElement(this, mapStack, Component.text("1 Year", Configuration.GUICOLORTITLE), buttonInstructions, vectorSlotPosition(5,1));
        graph2YButton = new GuiElement(this, mapStack, Component.text("2 Years", Configuration.GUICOLORTITLE), buttonInstructions, vectorSlotPosition(6,1));
    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        if(slot == returnButton.getSlot()){
            this.getInventory().close();
            new AssetExplorer(player, 2, cache).openGui(player);
        }else if(slot == graph1MButton.getSlot()){
            this.getInventory().close();
            MessagingUtil.sendSuccsess(player, "Preparing Graph...");
            Profitable.getfolialib().getScheduler().runAsync(task -> {
                TemporalItems.sendGraphMap(player, assetid, 720000, "1M");
            });
        } else if (slot == graph3MButton.getSlot()) {
            this.getInventory().close();
            MessagingUtil.sendSuccsess(player, "Preparing Graph...");
            Profitable.getfolialib().getScheduler().runAsync(task -> {
                TemporalItems.sendGraphMap(player, assetid, 2160000, "3M");
            });
        } else if (slot == graph6MButton.getSlot()) {
            this.getInventory().close();
            MessagingUtil.sendSuccsess(player, "Preparing Graph...");
            Profitable.getfolialib().getScheduler().runAsync(task -> {
                TemporalItems.sendGraphMap(player, assetid, 4320000, "6M");
            });
        } else if (slot == graph1YButton.getSlot()) {
            this.getInventory().close();
            MessagingUtil.sendSuccsess(player, "Preparing Graph...");
            Profitable.getfolialib().getScheduler().runAsync(task -> {
                TemporalItems.sendGraphMap(player, assetid, 8760000, "1Y");
            });
        } else if (slot == graph2YButton.getSlot()) {
            this.getInventory().close();
            MessagingUtil.sendSuccsess(player, "Preparing Graph...");
            Profitable.getfolialib().getScheduler().runAsync(task -> {
                TemporalItems.sendGraphMap(player, assetid, 17520000, "2Y");
            });
        }

    }
}
