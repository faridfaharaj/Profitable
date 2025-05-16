package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.guis.QuantitySelectGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class UnitsSelect extends QuantitySelectGui {

    Order order;
    Asset asset;
    Candle lastDay;
    List<Order> bidOrders;
    List<Order> askOrders;

    public UnitsSelect(Order order, Asset asset, Candle lastDay, List<Order> bidOrders, List<Order> askOrders) {
        super("How much are you " + (order.isSideBuy()?"buying.":"selling."),asset.getAssetType() != 2, asset.getAssetType() == 2 || asset.getAssetType() == 3, 1);
        this.order = order;
        this.asset = asset;
        this.lastDay = lastDay;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;
    }

    @Override
    protected void onAmountUpdate(double newAmount) {
        getSubmitButton().setDisplayName(Component.text("Transacting amount: ").append(Component.text(newAmount, NamedTextColor.YELLOW)));
        List<Component> lore = List.of(
                Component.empty(),
                Component.text("Total value: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, order.getType() == Order.OrderType.MARKET? lastDay.getClose()*newAmount: order.getPrice()*newAmount)),
                Component.empty(),
                GuiElement.clickAction(null, "Proceed with this amount")
        );
        getSubmitButton().setLore(lore);
        getSubmitButton().show(this);
    }

    @Override
    protected GuiElement submitButton(int slot) {

        ItemStack display = new ItemStack(Material.PAPER);

        Component name = Component.text("Transacting amount: ").append(Component.text(1.0, NamedTextColor.YELLOW));
        List<Component> lore = List.of(
                Component.empty(),
                GuiElement.clickAction(null, "Proceed with this amount")
        );

        return new GuiElement(this, display, name, lore, slot);
    }

    @Override
    protected void onSubmitAmount(Player player, double amount) {

        this.getInventory().close();
        new ConfirmOrder(new Order(null, null, asset.getCode(), order.isSideBuy(), order.getPrice(), amount, order.getType()), asset,lastDay, bidOrders, askOrders).openGui(player);

    }

    @Override
    protected void onReturn(Player player) {

        if(order.getType() == Order.OrderType.MARKET){
            this.getInventory().close();
            new OrderTypeGui(order, asset, lastDay, bidOrders, askOrders).openGui(player);
        }else {
            this.getInventory().close();
            new PriceSelect(order, asset, lastDay, bidOrders, askOrders).openGui(player);
        }

    }
}
