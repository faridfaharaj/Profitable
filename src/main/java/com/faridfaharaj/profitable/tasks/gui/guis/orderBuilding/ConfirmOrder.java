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
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.OrderButton;
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
    List<Order> bidOrders;
    List<Order> askOrders;

    AssetCache[][] assetCache;
    AssetCache assetData;
    public ConfirmOrder(AssetCache[][] assetCache, AssetCache assetData, Order order, List<Order> bidOrders, List<Order> askOrders) {
        super(3, "Confirm?");
        this.assetCache = assetCache;
        this.assetData = assetData;

        this.order = order;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));

        buttons[1] = new OrderButton(this, order, vectorSlotPosition(4, 1), "confirm this order");

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    player.closeInventory();
                    new UnitsSelect(assetCache, assetData, order, bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[1]){
                    player.closeInventory();
                    MessagingUtil.sendPlain(player, "Processing Order...");
                    Profitable.getfolialib().getScheduler().runAsync(task -> {
                        Exchange.sendNewOrder(player, new Order(UUID.randomUUID(), Accounts.getAccount(player), assetData.getAsset().getCode(), order.isSideBuy(), order.getPrice(), order.getUnits(), order.getType()));
                    });
                }

            }
        }

    }
}
