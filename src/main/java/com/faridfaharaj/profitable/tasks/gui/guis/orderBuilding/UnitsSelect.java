package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.QuantitySelectGui;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class UnitsSelect extends QuantitySelectGui {

    Order order;
    List<Order> bidOrders;
    List<Order> askOrders;

    AssetCache[][] assetCache;
    AssetCache assetData;
    public UnitsSelect(AssetCache[][] assetCache, AssetCache assetData, Order order, List<Order> bidOrders, List<Order> askOrders) {
        super("How much are you " + (order.isSideBuy()?"buying.":"selling."), assetData.getAsset().getAssetType() != 2, assetData.getAsset().getAssetType() == 2 || assetData.getAsset().getAssetType() == 3, 1);
        this.assetCache = assetCache;
        this.assetData = assetData;

        this.order = order;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;
    }

    @Override
    protected void onAmountUpdate(double newAmount) {
        getSubmitButton().setDisplayName(Component.text("Transacting amount: ").append(Component.text(newAmount, NamedTextColor.YELLOW)));
        List<Component> lore = List.of(
                Component.empty(),
                Component.text("Total value: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, order.getType() == Order.OrderType.MARKET?  assetData.getlastCandle().getClose()*newAmount: order.getPrice()*newAmount)),
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
        new ConfirmOrder(assetCache, assetData, new Order(null, null, assetData.getAsset().getCode(), order.isSideBuy(), order.getPrice(), amount, order.getType()), bidOrders, askOrders).openGui(player);

    }

    @Override
    protected void onReturn(Player player) {

        if(order.getType() == Order.OrderType.MARKET){
            this.getInventory().close();
            new OrderTypeGui(assetCache, assetData, order, bidOrders, askOrders).openGui(player);
        }else {
            this.getInventory().close();
            new PriceSelect(assetCache, assetData, order, bidOrders, askOrders).openGui(player);
        }

    }
}
