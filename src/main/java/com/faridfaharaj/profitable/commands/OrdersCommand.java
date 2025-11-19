package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.tasks.gui.guis.UserOrdersGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersCommand  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(sender instanceof Player player){

            new UserOrdersGui(player, null).openGui(player);

        }else{
            MessagingUtil.sendGenericCantConsole(sender);
        }
        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            return null;

        }

    }

}
