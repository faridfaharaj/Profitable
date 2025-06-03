package com.faridfaharaj.profitable.tasks.gui.guis.orderBuilding;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class OrderTypeGui extends ChestGUI {

    Order order;
    List<Order> bidOrders;
    List<Order> askOrders;

    GuiElement[] buttons = new GuiElement[4];

    boolean allowMarket;

    AssetCache[][] assetCache;
    AssetCache assetData;
    public OrderTypeGui(AssetCache[][] assetCache, AssetCache assetData, Order order, List<Order> bidOrders, List<Order> askOrders) {
        super(3, Profitable.getLang().get("gui.order-building.type-select.title"));
        this.assetCache = assetCache;
        this.assetData = assetData;

        this.order = order;
        this.bidOrders = bidOrders;
        this.askOrders = askOrders;

        fillAll(Material.BLACK_STAINED_GLASS_PANE);
        buttons[0] = new ReturnButton(this, vectorSlotPosition(0, 2));

        ItemStack orderStack = new ItemStack(Material.PAPER);

        if(order.isSideBuy()){
            allowMarket = !askOrders.isEmpty();
        }else {
            allowMarket = !bidOrders.isEmpty();
        }

        buttons[1] = new GuiElement(this, orderStack, Profitable.getLang().get("gui.order-building.type-select.buttons.market.name"),
                Profitable.getLang().langToLore(allowMarket?"gui.order-building.type-select.buttons.market.lore":"gui.order-building.type-select.buttons.market.lore-no-orders",
                        Map.entry("%asset%", order.getAsset())
                ), vectorSlotPosition(2, 1));

        buttons[2] = new GuiElement(this, orderStack, Profitable.getLang().get("gui.order-building.type-select.buttons.limit.name"),
                Profitable.getLang().langToLore("gui.order-building.type-select.buttons.limit.lore",
                        Map.entry("%asset%", order.getAsset())
                ), vectorSlotPosition(4, 1));

        buttons[3] = new GuiElement(this, orderStack, Profitable.getLang().get("gui.order-building.type-select.buttons.stop-limit.name"),
                Profitable.getLang().langToLore("gui.order-building.type-select.buttons.stop-limit.lore",
                        Map.entry("%asset%", order.getAsset())
                ), vectorSlotPosition(6, 1));

    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(GuiElement button :buttons){
            if(button.getSlot() == slot){

                if(button == buttons[0]){
                    player.closeInventory();
                    new BuySellGui(assetCache, assetData, bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[1]){
                    if(allowMarket){
                        player.closeInventory();
                        new UnitsSelect(assetCache, assetData, new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.isSideBuy()?Double.MAX_VALUE:Double.MIN_VALUE, order.getUnits(), Order.OrderType.MARKET), bidOrders, askOrders).openGui(player);
                    }
                }

                if(button == buttons[2]){
                    player.closeInventory();
                    new PriceSelect(assetCache, assetData, new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), Order.OrderType.LIMIT), bidOrders, askOrders).openGui(player);
                }

                if(button == buttons[3]){
                    player.closeInventory();
                    new PriceSelect(assetCache, assetData, new Order(order.getUuid(), order.getOwner(), order.getAsset(), order.isSideBuy(), order.getPrice(), order.getUnits(), Order.OrderType.STOP_LIMIT), bidOrders, askOrders).openGui(player);
                }

            }
        }

    }
}
