package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Orders;
import com.faridfaharaj.profitable.tasks.gui.guis.UserOrdersGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
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
import java.util.UUID;

public class OrdersCommand  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }
        if(sender instanceof Player player){

            new UserOrdersGui(player).openGui(player);

        }else{
            MessagingUtil.sendGenericCantConsole(sender);
        }
        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();
            if(args.length == 1){
                suggestions = List.of("[<Page>]");

            }

            return suggestions;

        }

    }

}
