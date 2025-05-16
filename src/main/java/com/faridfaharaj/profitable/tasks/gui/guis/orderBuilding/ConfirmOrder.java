package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.exchange.Books.Exchange;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public final  class ConfirmOrder extends ChestGUI {

    GuiElement[] buttons = new GuiElement[2];

    Order order;
    Asset asset;
    Candle lastDay;
    List<Order> bidOrders;
    List<Order> askOrders;

    public ConfirmOrder(Order order, Asset asset, Candle lastDay, List<Order> bidOrders, List<Order> askOrders) {
        super(3, "Confirm?");

        this.order = order;
        this.asset = asset;
        this.lastDay = lastDay;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));

        buttons[1] = new GuiElement(this, new ItemStack(Material.FILLED_MAP), Component.text("Order"),
                List.of(
                        Component.text("Type: ").append(Component.text(order.getType().toString())),
                        Component.text("Side: ").append(order.isSideBuy()?Component.text("Buy",Configuration.COLORBULLISH):Component.text("Sell",Configuration.COLORBEARISH)),
                        Component.empty(),
                        Component.text("Asset: ").append(Component.text(asset.getCode(),asset.getColor())),
                        Component.empty(),
                        Component.text("Units: ").append(Component.text(order.getUnits())),
                        Component.text("Price: ").append(order.getType() == Order.OrderType.MARKET?Component.text(order.isSideBuy()?"Lowest":"Highest"):Component.text(order.getPrice())),
                        Component.empty(),
                        GuiElement.clickAction(ClickType.LEFT, "Place Order")
                ), vectorSlotPosition(4, 1));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    this.getInventory().close();
                    new UnitsSelect(order, asset, lastDay, bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[1]){
                    this.getInventory().close();
                    MessagingUtil.sendInfoNotice(player, "Processing Order...");
                    Profitable.getfolialib().getScheduler().runAsync(task -> {
                        Exchange.sendNewOrder(player, new Order(UUID.randomUUID(), Accounts.getAccount(player), asset.getCode(), order.isSideBuy(), order.getPrice(), order.getUnits(), order.getType()));
                    });
                }

            }
        }

    }
}
