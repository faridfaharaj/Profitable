package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class OrderTypeGui extends ChestGUI {

    Order order;
    List<Order> bidOrders;
    List<Order> askOrders;

    GuiElement[] buttons = new GuiElement[4];

    boolean allowMarket;

    AssetCache[][] assetCache;
    AssetCache assetData;
    public OrderTypeGui(AssetCache[][] assetCache, AssetCache assetData, Order order, List<Order> bidOrders, List<Order> askOrders) {
        super(3, "Pick an order type.");
        this.assetCache = assetCache;
        this.assetData = assetData;

        this.order = order;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));

        String sideString = order.isSideBuy()?"Buy":"Sell";
        ItemStack orderStack = new ItemStack(Material.PAPER);

        if(order.isSideBuy()){
            allowMarket = !askOrders.isEmpty();
        }else {
            allowMarket = !bidOrders.isEmpty();
        }

        buttons[1] = new GuiElement(this, orderStack, Component.text("Market Order"),
                List.of(
                        Component.text(assetData.getAsset().getCode()),
                        Component.empty(),
                        Component.text(sideString).append(Component.text(" immediately", NamedTextColor.YELLOW)),
                        Component.text("at the best"),
                        Component.text("price available"),
                        Component.empty(),
                        allowMarket?GuiElement.clickAction(null, "select Market"):Component.text("No " + (order.isSideBuy()? "sellers!":"buyers!"),Configuration.COLORERROR)
                ), vectorSlotPosition(2, 1));

        buttons[2] = new GuiElement(this, orderStack, Component.text("Limit Order"),
                List.of(
                        Component.text(assetData.getAsset().getCode()),
                        Component.empty(),
                        Component.text("Choose a ").append(Component.text("price", NamedTextColor.YELLOW)),
                        Component.text("and " + sideString + " once a"),
                        Component.text("match is found"),
                        Component.empty(),
                        GuiElement.clickAction(null, "select Limit")
                ), vectorSlotPosition(4, 1));

        buttons[3] = new GuiElement(this, orderStack, Component.text("Stop-Limit order"),
                List.of(
                        Component.text(assetData.getAsset().getCode()),
                        Component.empty(),
                        Component.text(sideString).append(Component.text(" Limit Order", NamedTextColor.YELLOW)),
                        Component.text("once price hits"),
                        Component.text("your trigger"),
                        Component.empty(),
                        GuiElement.clickAction(null, "select Stop-Limit")
                ), vectorSlotPosition(6, 1));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    this.getInventory().close();
                    new BuySellGui(assetCache, assetData, bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[1]){
                    if(allowMarket){
                        this.getInventory().close();
                        new UnitsSelect(assetCache, assetData, new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.isSideBuy()?Double.MAX_VALUE:Double.MIN_VALUE, order.getUnits(), Order.OrderType.MARKET), bidOrders, askOrders).openGui(player);
                    }
                }

                if(button == buttons[2]){
                    this.getInventory().close();
                    new PriceSelect(assetCache, assetData, new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), Order.OrderType.LIMIT), bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[3]){
                    this.getInventory().close();
                    new PriceSelect(assetCache, assetData, new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), Order.OrderType.STOP_LIMIT), bidOrders, askOrders).openGui(player);
                }

            }
        }

    }
}
