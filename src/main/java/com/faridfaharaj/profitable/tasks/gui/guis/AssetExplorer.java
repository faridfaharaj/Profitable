package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AssetExplorer extends ChestGUI {

    GuiElement categoryButton;
    GuiElement pageButton;

    GuiElement walletButton;
    GuiElement ordersButton;
    GuiElement deliveryButton;

    AssetCache[][] assetCache = new AssetCache[5][];
    int assetType;

    List<AssetButton> assetButtons = new ArrayList<>();

    int page = 0;
    int pages = 0;

    public AssetExplorer(Player player, int assetType, AssetCache[][] previousCache) {
        super(6, "Asset explorer");

        this.assetType = assetType;

        fillSlots(0, 0, 8,0, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 0, 0,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(8, 0, 8,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 4, 8,5, Material.BLACK_STAINED_GLASS_PANE);

        categoryButton = new GuiElement(this, new ItemStack(Material.ENDER_EYE), Component.text("Category"),
                List.of(
                        Component.text("Assets", Configuration.GUICOLORSUBTITLE),
                        Component.empty(),
                        Component.text("♦ ", NamedTextColor.WHITE).append(Component.text("Forex", assetType == 1? NamedTextColor.WHITE:NamedTextColor.GRAY)),
                        Component.text("♦ ", NamedTextColor.GREEN).append(Component.text("Commodity (Item)", assetType == 2? NamedTextColor.GREEN:NamedTextColor.GRAY)),
                        Component.text("♦ ", NamedTextColor.GREEN).append(Component.text("Commodity (Entity)", assetType == 3? NamedTextColor.GREEN:NamedTextColor.GRAY)),
                        Component.empty(),
                        GuiElement.clickAction(null, "cycle")

                ), vectorSlotPosition(6, 5));

        pageButton = new GuiElement(this, new ItemStack(Material.PAPER), Component.text("Page " + page + " / " + pages), List.of(
                Component.empty(),
                GuiElement.clickAction(ClickType.LEFT, "next page"),
                GuiElement.clickAction(ClickType.RIGHT, "previous page")
        ), vectorSlotPosition(7,5));

        walletButton = new GuiElement(this, new ItemStack(Material.CHEST), Component.text("Manage owned assets"),
                List.of(
                        Component.text("Asset portfolio", Configuration.GUICOLORSUBTITLE),
                        Component.empty(),
                        GuiElement.clickAction(null, "view portfolio")

                ), vectorSlotPosition(2, 5));

        ordersButton = new GuiElement(this, new ItemStack(Material.BOOK), Component.text("Manage orders"),
                List.of(
                        Component.text("Orders", Configuration.GUICOLORSUBTITLE),
                        Component.empty(),
                        GuiElement.clickAction(null, "view active orders")

                ), vectorSlotPosition(1, 5));

        deliveryButton = new GuiElement(this, new ItemStack(Material.CARROT_ON_A_STICK), Component.text("Set delivery location"),
                List.of(
                        Component.text("Delivery", Configuration.GUICOLORSUBTITLE),
                        Component.empty(),
                        GuiElement.clickAction(null, "set item delivery"),
                        GuiElement.clickAction(null, "set entity delivery")

                ), vectorSlotPosition(3, 5));

        long time = player.getWorld().getFullTime();
        updateAssets(assetType, previousCache, time);


    }

    private void updateAssets(int assetType, AssetCache[][] previousCache, long time) {
        Profitable.getfolialib().getScheduler().runAsync(task -> {
            page = 0;
            if(previousCache == null){
                assetCache[assetType] = Candles.getAssetsNPrice(assetType, time).toArray(new AssetCache[0]);
            }else {
                assetCache = previousCache;
                if(previousCache[assetType] == null){
                    assetCache[assetType] = Candles.getAssetsNPrice(assetType, time).toArray(new AssetCache[0]);
                }
            }

            pages = assetCache[assetType].length/21;

            updatePage();

            if(pages > 0){
                pageButton.setDisplayName(Component.text("Page " + page + " / " + pages));
                pageButton.show(this);
            }else {
                fillSlot(pageButton.getSlot(), new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            }

        });
    }

    public void updatePage(){
        assetButtons.clear();
        for(int i = 0; i < 21; i++){

            int index = i+(page*21);
            int slot = (i+1)+9+((i/7)*2);
            if(index >= assetCache[assetType].length){
                getInventory().clear(slot);
            }else {
                assetButtons.add(new AssetButton(this, assetCache[assetType][index], new int[]{assetType, index}, slot));
            }

        }
    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(AssetButton button : assetButtons){
            if(button.getSlot() == slot){
                if(click.isLeftClick()){
                    button.trade(player, assetCache);
                }
                if(click.isRightClick()){
                    button.graphs(player, assetCache);
                }
            }
        }

        if(walletButton.getSlot() == slot){
            player.closeInventory();
            new HoldingsMenu(player, assetCache).openGui(player);
        }

        if(ordersButton.getSlot() == slot){
            player.closeInventory();
            new UserOrdersGui(player, assetCache).openGui(player);
        }

        if(deliveryButton.getSlot() == slot){
            if(click.isLeftClick()){
                player.closeInventory();
                TemporalItems.sendDeliveryStick(player, true);
            }
            if(click.isRightClick()){
                player.closeInventory();
                TemporalItems.sendDeliveryStick(player, false);
            }
        }

        if(categoryButton.getSlot() == slot){
            assetType += 1;
            if(assetType >= 4){
                assetType = 1;
            }
            categoryButton.setLore(List.of(
                    Component.text("Assets", Configuration.GUICOLORSUBTITLE),
                    Component.empty(),
                    Component.text("♦ ", NamedTextColor.WHITE).append(Component.text("Forex", assetType == 1? NamedTextColor.WHITE: Configuration.GUICOLORTEXT)),
                    Component.text("♦ ", NamedTextColor.GREEN).append(Component.text("Commodity (Item)", assetType == 2? NamedTextColor.GREEN:Configuration.GUICOLORTEXT)),
                    Component.text("♦ ", NamedTextColor.GREEN).append(Component.text("Commodity (Entity)", assetType == 3? NamedTextColor.GREEN:Configuration.GUICOLORTEXT)),
                    Component.empty(),
                    GuiElement.clickAction(null, "cycle")

            ));
            categoryButton.show(this);
            updateAssets(assetType, assetCache, player.getWorld().getFullTime());
        }

        if(pages > 0){
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
