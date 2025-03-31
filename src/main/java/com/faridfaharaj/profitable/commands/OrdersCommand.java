package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OrdersCommand  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(sender instanceof Player player){

            if(args.length > 0 && args[0].equals("cancel")){

                if(!sender.hasPermission("profitable.account.manage.orders.cancel")){
                    TextUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length == 1){

                    TextUtil.sendError(sender, "/orders cancel <Order ID>");
                    return true;

                }

                if(!Orders.cancelOrder(args[1], player)){
                    TextUtil.sendError(sender, "Couldn't cancel that order");
                }
                return true;
            }

            if(!sender.hasPermission("profitable.account.info.orders")){
                TextUtil.sendGenericMissingPerm(sender);
                return true;
            }

            int page;
            try {
                page = args.length == 0? 0 : Integer.parseInt(args[0]);
            }catch (Exception e){
                TextUtil.sendError(sender, "Invalid page number");
                return true;
            }

            List<Order> orders = Orders.getAccountOrders(Accounts.getAccount(player));
            if(orders.isEmpty()){
                TextUtil.sendEmptyNotice(player, "No active orders on this account");
            }else {
                int totalPages = (orders.size()-1)/2;
                Component component = TextUtil.profitableTopSeparator();
                for(int i = page*2; i<Math.min(page*2+2,orders.size()); i++){
                    Order order = orders.get(i);
                    String cmnd = "/orders cancel " + order.getUuid();
                    component = component.appendNewline().append(order.toComponent()).appendNewline()
                            .append(Component.text("[Click to cancel]",Configuration.COLORINFO)
                                    .clickEvent(ClickEvent.runCommand(cmnd))
                                    .hoverEvent(HoverEvent.showText(Component.text(cmnd,Configuration.COLORINFO))))
                    ;
                }
                component = component.appendNewline().append(TextUtil.profitablePaginator(page, totalPages, "/orders"));
                TextUtil.sendCustomMessage(sender, component);

            }
        }else{
            TextUtil.sendGenericCantConsole(sender);
        }
        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();
            if(args.length == 1){
                suggestions = List.of("[<Page>]","cancel");

            }

            if(args.length == 2){
                suggestions = List.of("[<Order ID>]");
            }

            return suggestions;

        }

    }

}
