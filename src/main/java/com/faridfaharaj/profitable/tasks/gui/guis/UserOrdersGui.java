package com.faridfaharaj.profitable.tasks.gui.guis;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.tasks.gui.elements.GuiElement;
import com.faridfaharaj.profitable.tasks.gui.elements.ReturnButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetButton;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.OrderButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class UserOrdersGui extends ChestGUI {

    List<Order> orders;

    List<OrderButton> orderButtons = new ArrayList<>();

    int page = 0;
    int pages = 0;

    GuiElement pageButton = null;
    GuiElement returnButton;

    AssetCache[][] assetCache;
    public UserOrdersGui(Player player, AssetCache[][] assetCache) {
        super(6, "Your active orders");
        fillSlots(0, 0, 8,0, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 0, 0,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(8, 0, 8,5, Material.BLACK_STAINED_GLASS_PANE);
        fillSlots(0, 4, 8,5, Material.BLACK_STAINED_GLASS_PANE);

        this.assetCache = assetCache;
        returnButton = new ReturnButton(this, vectorSlotPosition(4, 5));

        Profitable.getfolialib().getScheduler().runAsync(task -> {
            orders = Orders.getAccountOrders(Accounts.getAccount(player));

            updatePage();

            if(pages > 0){
                pageButton = new GuiElement(this, new ItemStack(Material.PAPER), Component.text("Page " + page + " / " + pages), List.of(
                        Component.empty(),
                        GuiElement.clickAction(ClickType.LEFT, "next page"),
                        GuiElement.clickAction(ClickType.RIGHT, "previous page")
                ), vectorSlotPosition(7,5));
            }

        });


    }

    public void updatePage(){
        orderButtons.clear();
        pages = orders.size()/21;
        for(int i = 0; i < 21; i++){

            int index = i+(page*21);
            int slot = (i+1)+9+((i/7)*2);
            if(index >= orders.size()){
                getInventory().clear(slot);
            }else {
                orderButtons.add(new OrderButton(this, orders.get(index), slot, "cancel this order"));
            }

        }
    }

    @Override
    public void slotInteracted(Player player, int slot, ClickType click) {

        for(OrderButton button : orderButtons){
            if(button.getSlot() == slot){
                button.cancel(player);
                button.hide(this);
                int indexOfButton = orderButtons.indexOf(button);
                orders.remove(indexOfButton);
                updatePage();
                if(pageButton != null){
                    pageButton.setDisplayName(Component.text("Page " + page + " / " + pages));
                    pageButton.show(this);
                }
                break;
            }
        }

        if(returnButton.getSlot() == slot){
            player.getInventory().close();
            new AssetExplorer(player, 2, assetCache).openGui(player);
        }

        if(pageButton != null){
            if(slot == pageButton.getSlot()){
                if(click.isLeftClick()){
                    page+=1;
                }if(click.isRightClick()){
                    page-=1;
                }
                page = Math.clamp(page, 0, pages);
                updatePage();
                pageButton.setDisplayName(Component.text("Page " + page + " / " + pages));
                pageButton.show(this);
            }
        }

    }
}
