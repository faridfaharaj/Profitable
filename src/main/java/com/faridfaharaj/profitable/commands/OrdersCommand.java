package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.tasks.gui.guis.UserOrdersGui;
import com.faridfaharaj.profitable.util.MessagingUtil;
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

        if(sender instanceof Player player){

            if(Configuration.MULTIWORLD){
                DataBase.universalUpdateWorld(sender);
            }

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
