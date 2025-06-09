package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
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
import java.util.Map;

public final class AssetExplorer extends ChestGUI {

    GuiElement categoryButton;
    GuiElement pageButton;

    GuiElement walletButton;
    GuiElement ordersButton;
    GuiElement deliveryButton;

    AssetCache[][] assetCache = new AssetCache[5][];
    Asset.AssetType assetType;

    List<AssetButton> assetButtons = new ArrayList<>();

    int page = 0;
    int pages = 0;

    public AssetExplorer(Player player, Asset.AssetType assetType, AssetCache[][] previousCache) {
        super(6, Profitable.getLang().get("gui.asset-explorer.title"));

        this.assetType = assetType;

        fillSlots(0, 0, 8,0, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 0, 0,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(8, 0, 8,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 4, 8,5, Material.BLACK_STAINED_GLASS_PANE);

        String types = "<white>♦ </white><color:" + (assetType == Asset.AssetType.CURRENCY? NamedTextColor.WHITE.asHexString():NamedTextColor.GRAY.asHexString()) + ">" + Profitable.getLang().getString("assets.categories.forex") + "</color>%&new_line&%" +
                "<green>♦ </green><color:" + (assetType == Asset.AssetType.COMMODITY_ITEM? NamedTextColor.GREEN.asHexString():NamedTextColor.GRAY.asHexString()) + ">" + Profitable.getLang().getString("assets.categories.commodity-item") + "</color>%&new_line&%" +
                "<green>♦ </green><color:" + (assetType == Asset.AssetType.COMMODITY_ENTITY? NamedTextColor.GREEN.asHexString():NamedTextColor.GRAY.asHexString()) + ">" + Profitable.getLang().getString("assets.categories.commodity-entity") + "</color>";

        categoryButton = new GuiElement(this, new ItemStack(Material.ENDER_EYE), Profitable.getLang().get("gui.asset-explorer.buttons.category-selector.name"),
                Profitable.getLang().langToLore("gui.asset-explorer.buttons.category-selector.lore",

                        Map.entry("%category_list%", types)

                ), vectorSlotPosition(6, 5));

        pageButton = new GuiElement(this, new ItemStack(Material.PAPER), Profitable.getLang().get("gui.generic.buttons.page-selector.name",
                Map.entry("%page%",String.valueOf(page)),
                Map.entry("%pages%",String.valueOf(pages))
        ), Profitable.getLang().langToLore("gui.generic.buttons.page-selector.lore"), vectorSlotPosition(7,5));

        walletButton = new GuiElement(this, new ItemStack(Material.CHEST), Profitable.getLang().get("gui.asset-explorer.buttons.wallet.name"),
                Profitable.getLang().langToLore("gui.asset-explorer.buttons.wallet.lore")
                , vectorSlotPosition(2, 5));

        ordersButton = new GuiElement(this, new ItemStack(Material.BOOK), Profitable.getLang().get("gui.asset-explorer.buttons.orders.name"),
                Profitable.getLang().langToLore("gui.asset-explorer.buttons.orders.lore")
                , vectorSlotPosition(1, 5));

        deliveryButton = new GuiElement(this, new ItemStack(Material.CARROT_ON_A_STICK), Profitable.getLang().get("gui.asset-explorer.buttons.delivery.name"),
                Profitable.getLang().langToLore("gui.asset-explorer.buttons.delivery.lore")
                , vectorSlotPosition(3, 5));

        long time = player.getWorld().getFullTime();
        updateAssets(assetType, previousCache, time);


    }

    private void updateAssets(Asset.AssetType assetType, AssetCache[][] previousCache, long time) {
        Profitable.getfolialib().getScheduler().runAsync(task -> {
            page = 0;
            if(previousCache == null){
                assetCache[assetType.getValue()] = Candles.getAssetsNPrice(assetType.getValue(), time).toArray(new AssetCache[0]);
            }else {
                assetCache = previousCache;
                if(previousCache[assetType.getValue()] == null){
                    assetCache[assetType.getValue()] = Candles.getAssetsNPrice(assetType.getValue(), time).toArray(new AssetCache[0]);
                }
            }

            pages = assetCache[assetType.getValue()].length/21;

            updatePage();

            if(pages > 0){
                pageButton.setDisplayName(Profitable.getLang().get("gui.generic.buttons.page-selector.name",
                        Map.entry("%page%",String.valueOf(page)),
                        Map.entry("%pages%",String.valueOf(pages))
                ));
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
            if(index >= assetCache[assetType.getValue()].length){
                getInventory().clear(slot);
            }else {
                assetButtons.add(new AssetButton(this, assetCache[assetType.getValue()][index], new int[]{assetType.getValue(), index}, slot));
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
            assetType = Asset.AssetType.fromValue(assetType.getValue() + 1);
            if(assetType.getValue() >= 4){
                assetType = Asset.AssetType.fromValue(1);
            }

            String types = "<white>♦ </white><color:" + (assetType == Asset.AssetType.CURRENCY? NamedTextColor.WHITE.asHexString():NamedTextColor.GRAY.asHexString()) + ">" + Profitable.getLang().getString("assets.categories.forex") + "</color>%&new_line&%" +
                    "<green>♦ </green><color:" + (assetType == Asset.AssetType.COMMODITY_ITEM? NamedTextColor.GREEN.asHexString():NamedTextColor.GRAY.asHexString()) + ">" + Profitable.getLang().getString("assets.categories.commodity-item") + "</color>%&new_line&%" +
                    "<green>♦ </green><color:" + (assetType == Asset.AssetType.COMMODITY_ENTITY? NamedTextColor.GREEN.asHexString():NamedTextColor.GRAY.asHexString()) + ">" + Profitable.getLang().getString("assets.categories.commodity-entity") + "</color>";
            categoryButton.setLore(Profitable.getLang().langToLore("gui.asset-explorer.buttons.category-selector.lore",
                            Map.entry("%category_list%", types)
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
                pageButton.setDisplayName(Profitable.getLang().get("gui.generic.buttons.page-selector.name",
                        Map.entry("%page%",String.valueOf(page)),
                        Map.entry("%pages%",String.valueOf(pages))
                ));
                pageButton.show(this);
            }
        }

    }
}
