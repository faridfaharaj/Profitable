package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OrderButton extends GuiElement{

    Order order;

    public OrderButton(ChestGUI gui, Order order, int slot) {
        super(gui, new ItemStack(Material.PAPER), order.isSideBuy()?Component.text("Buy order", Configuration.COLORBULLISH):Component.text("Sell order", Configuration.COLORBEARISH),
                List.of(
                        Component.text("Order Type: " + order.getType()),
                        Component.empty(),
                        Component.text("Asset: " + order.getAsset()),
                        Component.empty(),
                        Component.text("Units: " + order.getUnits()),
                        Component.text("Price: " + order.getPrice())
                ), slot);

        this.order = order;


    }

    public void cancel(Player player){
        player.getInventory().close();
        Profitable.getfolialib().getScheduler().runAsync(task -> {
            Orders.cancelOrder(order.getUuid());
        });
    }

}
