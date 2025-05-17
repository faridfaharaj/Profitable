package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetButtonData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AssetExplorer extends ChestGUI {

    GuiElement categoryButton;
    GuiElement pageButton = null;

    List<AssetButtonData> assets = new ArrayList<>();
    List<AssetButton> assetButtons = new ArrayList<>();

    int page = 0;
    int pages = 0;

    public AssetExplorer(Player player) {
        super(6, "Asset explorer");
        fillSlots(0, 0, 8,0, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 0, 0,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(8, 0, 8,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 4, 8,5, Material.BLACK_STAINED_GLASS_PANE);

        categoryButton = new GuiElement(this, new ItemStack(Material.ENDER_EYE), Component.text("Category"),
                List.of(



                ), vectorSlotPosition(6, 5));

        long time = player.getWorld().getFullTime();
        Profitable.getfolialib().getScheduler().runAsync(task -> {
            assets = Candles.getAssetFancyTypeWithCandles(1, time);

            pages = assets.size()/21+1;

            for(int i = 0; i < 21; i++){

                int index = i+(page*21);
                if(index >= assets.size()){
                    break;
                }

                int slot = (i+1)+9+((i/7)*2);

                assetButtons.add(new AssetButton(this, assets.get(index), slot));

            }

            updatePage();

            if(pages > 1){
                pageButton = new GuiElement(this, new ItemStack(Material.PAPER), Component.text("Page " + page + " / " + pages), null, vectorSlotPosition(7,5));
            }

        });


    }

    public void updatePage(){
        for(int i = 0; i < 21; i++){

            int index = i+(page*21);
            if(index >= assets.size()){
                break;
            }

            int slot = (i+1)+9+((i/7)*2);

            assetButtons.add(new AssetButton(this, assets.get(index), slot));

        }
    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(AssetButton button : assetButtons){
            if(button.getSlot() == slot){
                if(click.isLeftClick()){
                    button.trade(player);
                }
                if(click.isRightClick()){
                    button.graphs(player);
                }
            }
        }

        if(pageButton != null){
            if(slot == pageButton.getSlot()){
                page+=1;
                updatePage();
            }
        }

    }
}
