package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.QuantitySelectGui;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class PriceSelect  extends QuantitySelectGui {

    Order order;
    List<Order> bidOrders;
    List<Order> askOrders;

    AssetCache[][] assetCache;
    AssetCache assetData;
    public PriceSelect(AssetCache[][] assetCache, AssetCache assetData, Order order, List<Order> bidOrders, List<Order> askOrders) {
        super(Profitable.getLang().get("gui.order-building.price-select.title"), true, false,
                Math.max(order.isSideBuy()? (bidOrders.isEmpty()? assetData.getlastCandle().getClose() : bidOrders.getFirst().getPrice()) :(askOrders.isEmpty()? assetData.getlastCandle().getClose() : askOrders.getFirst().getPrice()), 0.001)
        );
        this.assetCache = assetCache;
        this.assetData = assetData;

        this.order = order;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        List<Component> lore;
        lore = Profitable.getLang().langToLore("gui.order-building.price-select.buttons.submit.lore",
                Map.entry("%asset%", assetData.getAsset().getCode()),
                Map.entry("%price_asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, assetData.getlastCandle().getClose()))
        );
        getSubmitButton().setLore(lore);
        getSubmitButton().show(this);
    }

    @Override
    protected void onAmountUpdate(double newPrice) {
        getSubmitButton().setDisplayName(Profitable.getLang().get("gui.order-building.price-select.buttons.submit.name", Map.entry("%amount%", String.valueOf(this.amount)), Map.entry("%asset%", Configuration.MAINCURRENCYASSET.getCode())));
        getSubmitButton().show(this);
    }

    @Override
    protected GuiElement submitButton(int slot) {

        ItemStack display = new ItemStack(Material.MAP);

        Component name = Component.text("Your price: ", Configuration.GUICOLORTITLE).append(Component.text(this.amount + " " + Configuration.MAINCURRENCYASSET.getCode(),Configuration.GUICOLORTITLEHIGHLIGHT));

        name = Profitable.getLang().get("gui.order-building.price-select.buttons.submit.name", Map.entry("%amount%", String.valueOf(this.amount)), Map.entry("%asset%", Configuration.MAINCURRENCYASSET.getCode()));

        return new GuiElement(this, display, name, null, slot);
    }

    @Override
    protected void onSubmitAmount(Player player, double amount) {
        player.closeInventory();
        new UnitsSelect(assetCache, assetData, new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), amount, order.getUnits(), order.getType()), bidOrders, askOrders).openGui(player);
    }

    @Override
    protected void onReturn(Player player) {
        player.closeInventory();
        new OrderTypeGui(assetCache, assetData, order, bidOrders, askOrders).openGui(player);
    }
}
