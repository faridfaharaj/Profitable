package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.tasks.gui.guis.AssetExplorer;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class BuySellGui extends ChestGUI {

    List<Order> bidOrders;
    List<Order> askOrders;

    GuiElement[] buttons = new GuiElement[3];

    AssetCache[][] assetCache;
    AssetCache assetData;
    public BuySellGui(AssetCache[][] assetCache, AssetCache assetData, List<Order> bidOrders, List<Order> askOrders) {
        super(3, Profitable.getLang().get("gui.order-building.buy-sell.title"));

        this.assetCache = assetCache;
        this.assetData = assetData;

        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));



        List<Component> sellLore;
        if(!askOrders.isEmpty()){

            StringBuilder prices = new StringBuilder();

            for(int i = 0; i < 7; i++){
                if(i < askOrders.size()){
                    Order iteratedAsk = askOrders.get(i);
                    prices.append("<gray> - <red>[ ").append(MessagingUtil.formatVolume(iteratedAsk.getUnits())).append(" ]</red> ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, iteratedAsk.getPrice())).append("</gray>%&new_line&%");
                }else {
                    break;
                }
                if(i == 6){
                    prices.append("<gray> - ...</gray>");
                }
            }

            sellLore = Profitable.getLang().langToLore("gui.order-building.buy-sell.buttons.sell.lore",
                    Map.entry("%asset%", this.assetData.getAsset().getCode()),
                    Map.entry("%ask_asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, askOrders.getFirst().getPrice())),
                    Map.entry("%price_list%", prices.toString())
            );
        }else {
            sellLore = Profitable.getLang().langToLore("gui.order-building.buy-sell.buttons.sell.no-orders-lore",
                    Map.entry("%asset%", this.assetData.getAsset().getCode())
            );
        }

        List<Component> buyLore;
        if(!bidOrders.isEmpty()){

            StringBuilder prices = new StringBuilder();

            for(int i = 0; i < 7; i++){
                if(i < bidOrders.size()){
                    Order iteratedAsk = bidOrders.get(i);
                    prices.append("<gray> - <green>[ ").append(MessagingUtil.formatVolume(iteratedAsk.getUnits())).append(" ]</green> ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, iteratedAsk.getPrice())).append("</gray>%&new_line&%");
                }else {
                    break;
                }
                if(i == 6){
                    prices.append("<gray> - ...</gray>");
                }
            }

            buyLore = Profitable.getLang().langToLore("gui.order-building.buy-sell.buttons.buy.lore",
                    Map.entry("%asset%", this.assetData.getAsset().getCode()),
                    Map.entry("%bid_asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, bidOrders.getFirst().getPrice())),
                    Map.entry("%price_list%", prices.toString())
            );
        }else {
            buyLore = Profitable.getLang().langToLore("gui.order-building.buy-sell.buttons.buy.no-orders-lore",
                    Map.entry("%asset%", this.assetData.getAsset().getCode())
            );
        }

        buttons[1] = new GuiElement(this, new ItemStack(Material.RED_DYE), Profitable.getLang().get("orders.sides.sell").color(Configuration.COLORBEARISH),
                sellLore, vectorSlotPosition(5, 1));
        buttons[2] = new GuiElement(this, new ItemStack(Material.LIME_DYE), Profitable.getLang().get("orders.sides.buy").color(Configuration.COLORBULLISH),
                buyLore, vectorSlotPosition(3, 1));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    player.closeInventory();
                    new AssetExplorer(player, assetData.getAsset().getAssetType(), assetCache).openGui(player);
                }

                if(button == buttons[1]){
                    player.closeInventory();
                    new OrderTypeGui(assetCache, assetData, new Order(null, null, assetData.getAsset().getCode(), false, 0, 0, null), bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[2]){
                    player.closeInventory();
                    new OrderTypeGui(assetCache, assetData, new Order(null, null, assetData.getAsset().getCode(), true, 0, 0, null), bidOrders, askOrders).openGui(player);
                }

            }
        }

    }
}
