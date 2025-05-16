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

import java.util.ArrayList;
import java.util.List;

public final class PriceSelect  extends QuantitySelectGui {

    Order order;
    Asset asset;
    Candle lastDay;
    List<Order> bidOrders;
    List<Order> askOrders;

    public PriceSelect(Order order, Asset asset, Candle lastDay, List<Order> bidOrders, List<Order> askOrders) {
        super("Select price per unit.", true, false,
                order.isSideBuy()?
                        (bidOrders.isEmpty()? lastDay.getClose() : bidOrders.getFirst().getPrice())
                        :(askOrders.isEmpty()? lastDay.getClose() : askOrders.getFirst().getPrice())
        );

        this.order = order;
        this.asset = asset;
        this.lastDay = lastDay;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Current asset price: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, lastDay.getClose())));
        if(!bidOrders.isEmpty()){
            lore.add(Component.text("Bid: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, bidOrders.getFirst().getPrice())));
        }
        if(!askOrders.isEmpty()){
            lore.add(Component.text("Ask: ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, askOrders.getFirst().getPrice())));
        }
        lore.add(Component.empty());
        lore.add(GuiElement.clickAction(null, "Proceed with this price"));
        getSubmitButton().setLore(lore);
        getSubmitButton().show(this);
    }

    @Override
    protected void onAmountUpdate(double newPrice) {
        getSubmitButton().setDisplayName(Component.text("Your price: ").append(Component.text(newPrice + " " + Configuration.MAINCURRENCYASSET.getCode(),NamedTextColor.YELLOW)));
        getSubmitButton().show(this);
    }

    @Override
    protected GuiElement submitButton(int slot) {

        ItemStack display = new ItemStack(Material.EMERALD);

        Component name = Component.text("Your price: ").append(Component.text(this.amount + " " + Configuration.MAINCURRENCYASSET.getCode(),NamedTextColor.YELLOW));


        return new GuiElement(this, display, name, null, slot);
    }

    @Override
    protected void onSubmitAmount(Player player, double amount) {
        this.getInventory().close();
        new UnitsSelect(new Order(null, null, asset.getCode(), order.isSideBuy(), amount, 0, order.getType()), asset,lastDay, bidOrders, askOrders).openGui(player);
    }

    @Override
    protected void onReturn(Player player) {
        this.getInventory().close();
        new OrderTypeGui(order, asset,lastDay, bidOrders, askOrders).openGui(player);
    }
}
