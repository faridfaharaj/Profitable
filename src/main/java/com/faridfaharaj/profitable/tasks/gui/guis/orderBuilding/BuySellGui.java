package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.guis.MainMenu;
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

    Asset asset;
    Candle lastDay;
    List<Order> bidOrders;
    List<Order> askOrders;

    GuiElement[] buttons = new GuiElement[3];

    public BuySellGui(Asset asset, Candle lastDay, List<Order> bidOrders, List<Order> askOrders) {
        super(3, "Pick a side.");

        this.asset = asset;
        this.lastDay = lastDay;
        System.out.println(lastDay);
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));



        List<Component> sellLore = new ArrayList<>();
        sellLore.add(Component.text(asset.getCode()));
        sellLore.add(Component.empty());
        if(!askOrders.isEmpty()){
            sellLore.add(Component.text("Ask: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, askOrders.getFirst().getPrice())));
            sellLore.add(Component.empty());
            sellLore.add(Component.text("Best orders: "));
            for(int i = 0; i < 7; i++){
                if(i < askOrders.size()){
                    Order iteratedAsk = askOrders.get(i);
                    sellLore.add(Component.text(" - ").append(Component.text("[ "+formatVolume(iteratedAsk.getUnits())+" ] ",Configuration.COLORBEARISH)).append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, iteratedAsk.getPrice())));
                }else {
                    break;
                }
                if(i == 6){
                    sellLore.add(Component.text(" - ..."));
                }
            }
        }else {
            sellLore.add(Component.text("No one is selling this asset yet!"));
        }
        sellLore.add(Component.empty());
        sellLore.add(GuiElement.clickAction(null, "select sell"));

        List<Component> buyLore = new ArrayList<>();
        buyLore.add(Component.text(asset.getCode()));
        buyLore.add(Component.empty());
        if(!bidOrders.isEmpty()){
            buyLore.add(Component.text("Bid: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET,bidOrders.getFirst().getPrice())));
            buyLore.add(Component.empty());
            buyLore.add(Component.text("Best orders: "));
            for(int i = 0; i < 7; i++){
                if(i < bidOrders.size()){
                    Order iteratedAsk = bidOrders.get(i);
                    buyLore.add(Component.text(" - ").append(Component.text("[ "+formatVolume(iteratedAsk.getUnits())+" ] ",Configuration.COLORBULLISH)).append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, iteratedAsk.getPrice())));
                }else {
                    break;
                }
                if(i == 6){
                    sellLore.add(Component.text(" - ..."));
                }
            }
        }else {
            buyLore.add(Component.text("No one is buying this asset yet!"));
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
                    new MainMenu().openGui(player);
                }

                if(button == buttons[1]){
                    this.getInventory().close();
                    new OrderTypeGui(new Order(null, null, asset.getCode(), false, 0, 0, null), asset,lastDay, bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[2]){
                    this.getInventory().close();
                    new OrderTypeGui(new Order(null, null, asset.getCode(), true, 0, 0, null),asset,lastDay, bidOrders, askOrders).openGui(player);
                }

            }
        }

    }
}
