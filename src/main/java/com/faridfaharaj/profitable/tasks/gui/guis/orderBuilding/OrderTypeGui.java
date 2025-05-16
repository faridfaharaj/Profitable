package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class OrderTypeGui extends ChestGUI {

    Order order;
    Asset asset;
    Candle lastDay;
    List<Order> bidOrders;
    List<Order> askOrders;

    GuiElement[] buttons = new GuiElement[4];

    boolean allowMarket;

    public OrderTypeGui(Order order, Asset asset, Candle lastDay, List<Order> bidOrders, List<Order> askOrders) {
        super(3, "Pick an order type.");

        this.order = order;
        this.asset = asset;
        this.lastDay = lastDay;
        System.out.println(lastDay);
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
                        Component.text(asset.getCode()),
                        Component.empty(),
                        Component.text(sideString).append(Component.text(" an asset ")).append(Component.text("Immediately", NamedTextColor.YELLOW)),
                        Component.text("at the best price"),
                        Component.empty(),
                        allowMarket?GuiElement.clickAction(null, "select Market"):Component.text("No " + (order.isSideBuy()? "sellers!":"buyers!"),Configuration.COLORERROR)
                ), vectorSlotPosition(2, 1));

        buttons[2] = new GuiElement(this, orderStack, Component.text("Limit Order"),
                List.of(
                        Component.text(asset.getCode()),
                        Component.empty(),
                        Component.text("Choose a ").append(Component.text("price", NamedTextColor.YELLOW)),
                        Component.text("and " + sideString + " once a"),
                        Component.text("match is found"),
                        Component.empty(),
                        GuiElement.clickAction(null, "select Limit")
                ), vectorSlotPosition(4, 1));

        buttons[3] = new GuiElement(this, orderStack, Component.text("Stop-Limit order"),
                List.of(
                        Component.text(asset.getCode()),
                        Component.empty(),
                        Component.text(sideString).append(Component.text(" as limit order")),
                        Component.text("once ").append(Component.text("market reaches", NamedTextColor.YELLOW)),
                        Component.text("your price", NamedTextColor.YELLOW),
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
                    new BuySellGui(asset, lastDay, bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[1]){
                    if(allowMarket){
                        this.getInventory().close();
                        new UnitsSelect(new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.isSideBuy()?Double.MAX_VALUE:Double.MIN_VALUE, order.getUnits(), Order.OrderType.MARKET), asset,lastDay, bidOrders, askOrders).openGui(player);
                    }
                }

                if(button == buttons[2]){
                    this.getInventory().close();
                    new PriceSelect(new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), Order.OrderType.LIMIT), asset,lastDay, bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[3]){
                    this.getInventory().close();
                    new PriceSelect(new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), Order.OrderType.STOP_LIMIT), asset,lastDay, bidOrders, askOrders).openGui(player);
                }

            }
        }

    }
}
