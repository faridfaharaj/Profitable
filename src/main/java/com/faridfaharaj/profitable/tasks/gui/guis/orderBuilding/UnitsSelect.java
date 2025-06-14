package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
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
import java.util.Map;

public final class UnitsSelect extends QuantitySelectGui {

    Order order;
    List<Order> bidOrders;
    List<Order> askOrders;

    AssetCache[][] assetCache;
    AssetCache assetData;
    public UnitsSelect(AssetCache[][] assetCache, AssetCache assetData, Order order, List<Order> bidOrders, List<Order> askOrders) {
        super(Profitable.getLang().get("gui.order-building.units-select.title"), assetData.getAsset().getAssetType() != 2 && assetData.getAsset().getAssetType() != 3, assetData.getAsset().getAssetType() == 2 || assetData.getAsset().getAssetType() == 3, 1);
        this.assetCache = assetCache;
        this.assetData = assetData;

        this.order = order;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        onAmountUpdate(amount);
    }

    @Override
    protected void onAmountUpdate(double newAmount) {
        getSubmitButton().setDisplayName(Profitable.getLang().get("gui.order-building.units-select.buttons.submit.name", Map.entry("%amount%", String.valueOf(this.amount)), Map.entry("%asset%", order.getAsset())));
        List<Component> lore;
        lore = Profitable.getLang().langToLore("gui.order-building.units-select.buttons.submit.lore",
                Map.entry("%asset%", order.getAsset()),
                Map.entry("%value_asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, order.getType() == Order.OrderType.MARKET?  assetData.getlastCandle().getClose()*newAmount: order.getPrice()*newAmount))
                );
        getSubmitButton().setLore(lore);
        getSubmitButton().show(this);
    }

    @Override
    protected GuiElement submitButton(int slot) {

        ItemStack display = new ItemStack(Material.PAPER);

        Component name = Profitable.getLang().get("gui.order-building.units-select.buttons.submit.name", Map.entry("%amount%", String.valueOf(this.amount)));

        return new GuiElement(this, display, name, null, slot);
    }

    @Override
    protected void onSubmitAmount(Player player, double amount) {

        player.closeInventory();
        new ConfirmOrder(assetCache, assetData, new Order(null, null, assetData.getAsset().getCode(), order.isSideBuy(), order.getPrice(), amount, order.getType()), bidOrders, askOrders).openGui(player);

    }

    @Override
    protected void onReturn(Player player) {

        if(order.getType() == Order.OrderType.MARKET){
            player.closeInventory();
            new OrderTypeGui(assetCache, assetData, order, bidOrders, askOrders).openGui(player);
        }else {
            player.closeInventory();
            new PriceSelect(assetCache, assetData, order, bidOrders, askOrders).openGui(player);
        }

    }
}
