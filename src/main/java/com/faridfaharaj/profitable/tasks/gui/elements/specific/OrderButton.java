package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class OrderButton extends GuiElement{

    Order order;

    public OrderButton(ChestGUI gui, Order order, int slot) {
        super(gui, order.isSideBuy()?new ItemStack(Material.PAPER):new ItemStack(Material.MAP), order.isSideBuy()?Component.text("Buy order", Configuration.COLORBULLISH):Component.text("Sell order", Configuration.COLORBEARISH),
                List.of(
                        Component.text("Type: " + order.getType()),
                        Component.empty(),
                        Component.text("Asset: " + order.getAsset()),
                        Component.empty(),
                        Component.text("Units: " + order.getUnits()),
                        Component.text("Price: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, order.getPrice())),
                        Component.empty(),
                        Component.text("Total value: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, order.getPrice()*order.getUnits())),
                        Component.empty(),
                        GuiElement.clickAction(null, "cancel this order")
                ), slot);

        this.order = order;


    }

    public void cancel(Player player){
        Profitable.getfolialib().getScheduler().runAsync(task -> {
            Orders.cancelOrder(order.getUuid(), player);
        });
    }

}
