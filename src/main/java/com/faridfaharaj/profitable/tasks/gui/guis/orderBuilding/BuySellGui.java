package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
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

import java.util.ArrayList;
import java.util.List;

import static com.faridfaharaj.profitable.util.NamingUtil.formatVolume;

public final class BuySellGui extends ChestGUI {

    List<Order> bidOrders;
    List<Order> askOrders;

    GuiElement[] buttons = new GuiElement[3];

    AssetCache[][] assetCache;
    AssetCache assetData;
    public BuySellGui(AssetCache[][] assetCache, AssetCache assetData, List<Order> bidOrders, List<Order> askOrders) {
        super(3, "Pick a side.");

        this.assetCache = assetCache;
        this.assetData = assetData;

        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));



        List<Component> sellLore = new ArrayList<>();
        sellLore.add(Component.text(this.assetData.getAsset().getCode(), Configuration.GUICOLORSUBTITLE));
        sellLore.add(Component.empty());
        if(!askOrders.isEmpty()){
            sellLore.add(Component.text("Ask: ", Configuration.GUICOLORTEXT).append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, askOrders.getFirst().getPrice())));
            sellLore.add(Component.empty());
            sellLore.add(Component.text("Lowest prices: ", Configuration.GUICOLORTEXT));
            for(int i = 0; i < 7; i++){
                if(i < askOrders.size()){
                    Order iteratedAsk = askOrders.get(i);
                    sellLore.add(Component.text(" - ", Configuration.GUICOLORTEXT).append(Component.text("[ "+formatVolume(iteratedAsk.getUnits())+" ] ",Configuration.COLORBEARISH)).append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, iteratedAsk.getPrice())));
                }else {
                    break;
                }
                if(i == 6){
                    sellLore.add(Component.text(" - ...", Configuration.GUICOLORTEXT));
                }
            }
        }else {
            sellLore.add(Component.text("No one is selling this asset yet!", Configuration.GUICOLORTEXT));
        }
        sellLore.add(Component.empty());
        sellLore.add(GuiElement.clickAction(null, "select sell"));

        List<Component> buyLore = new ArrayList<>();
        buyLore.add(Component.text(this.assetData.getAsset().getCode(),Configuration.GUICOLORSUBTITLE));
        buyLore.add(Component.empty());
        if(!bidOrders.isEmpty()){
            buyLore.add(Component.text("Bid: ", Configuration.GUICOLORTEXT).append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET,bidOrders.getFirst().getPrice())));
            buyLore.add(Component.empty());
            buyLore.add(Component.text("Highest prices: ", Configuration.GUICOLORTEXT));
            for(int i = 0; i < 7; i++){
                if(i < bidOrders.size()){
                    Order iteratedAsk = bidOrders.get(i);
                    buyLore.add(Component.text(" - ", Configuration.GUICOLORTEXT).append(Component.text("[ "+formatVolume(iteratedAsk.getUnits())+" ] ",Configuration.COLORBULLISH)).append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, iteratedAsk.getPrice())));
                }else {
                    break;
                }
                if(i == 6){
                    sellLore.add(Component.text(" - ...", Configuration.GUICOLORTEXT));
                }
            }
        }else {
            buyLore.add(Component.text("No one is buying this asset yet!", Configuration.GUICOLORTEXT));
        }
        buyLore.add(Component.empty());
        buyLore.add(GuiElement.clickAction(null, "select buy"));

        buttons[1] = new GuiElement(this, new ItemStack(Material.RED_DYE), Component.text("Sell Order", Configuration.COLORBEARISH),
                sellLore, vectorSlotPosition(5, 1));
        buttons[2] = new GuiElement(this, new ItemStack(Material.LIME_DYE), Component.text("Buy Order", Configuration.COLORBULLISH),
                buyLore, vectorSlotPosition(3, 1));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    this.getInventory().close();
                    new AssetExplorer(player, assetData.getAsset().getAssetType(), assetCache).openGui(player);
                }

                if(button == buttons[1]){
                    this.getInventory().close();
                    new OrderTypeGui(assetCache, assetData, new Order(null, null, assetData.getAsset().getCode(), false, 0, 0, null), bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[2]){
                    this.getInventory().close();
                    new OrderTypeGui(assetCache, assetData, new Order(null, null, assetData.getAsset().getCode(), true, 0, 0, null), bidOrders, askOrders).openGui(player);
                }

            }
        }

    }
}
