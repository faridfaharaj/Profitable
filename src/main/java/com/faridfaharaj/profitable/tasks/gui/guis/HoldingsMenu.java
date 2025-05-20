package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetHolderButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class HoldingsMenu extends ChestGUI {

    GuiElement returnButton;
    GuiElement pageButton = null;

    List<AssetCache> assets = new ArrayList<>();
    List<AssetHolderButton> assetButtons = new ArrayList<>();

    int page = 0;
    int pages = 0;

    AssetCache[][] assetCache;
    public HoldingsMenu(Player player, AssetCache[][] assetCache) {
        super(6, "Portfolio");
        fillSlots(0, 0, 8,0, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 0, 0,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(8, 0, 8,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 4, 8,5, Material.BLACK_STAINED_GLASS_PANE);

        this.assetCache= assetCache;
        returnButton = new ReturnButton(this, vectorSlotPosition(4, 5));

        Profitable.getfolialib().getScheduler().runAsync(task -> {
            assets = AccountHoldings.AssetBalancesToAssetData(Accounts.getAccount(player));

            pages = assets.size()/21;


            updatePage();

            if(pages > 0){
                pageButton = new GuiElement(this, new ItemStack(Material.PAPER), Component.text("Page " + page + " / " + pages), List.of(
                        Component.empty(),
                        GuiElement.clickAction(ClickType.LEFT, "next page"),
                        GuiElement.clickAction(ClickType.RIGHT, "previous page")
                ), vectorSlotPosition(7,5));
            }

        });
    }

    void updatePage(){
        assetButtons.clear();
        for(int i = 0; i < 21; i++){

            int index = i+(page*21);
            int slot = (i+1)+9+((i/7)*2);
            if(index >= assets.size()){
                getInventory().clear(slot);
            }else {
                assetButtons.add(new AssetHolderButton(this, assets.get(index), slot));
            }

        }
    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(AssetHolderButton button : assetButtons){
            if(button.getSlot() == slot){
                if(click.isLeftClick()){

                    button.manage(player, false, assetCache);

                }else if(click.isRightClick()){
                    button.manage(player, true, assetCache);
                }
            }
        }

        if(returnButton.getSlot() == slot){
            player.closeInventory();
            new AssetExplorer(player, 2, assetCache).openGui(player);
        }

        if(pageButton != null){
            if(slot == pageButton.getSlot()){
                if(click.isLeftClick()){
                    page+=1;
                }if(click.isRightClick()){
                    page-=1;
                }
                page = Math.clamp(page, 0, pages);
                updatePage();
                pageButton.setDisplayName(Component.text("Page " + page + " / " + pages));
                pageButton.show(this);
            }
        }

    }
}
