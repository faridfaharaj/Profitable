package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.util.MessagingUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ClaimtagCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if(sender instanceof Player player){

            if(!sender.hasPermission("profitable.account.claim")){
                MessagingUtil.sendGenericMissingPerm(sender);
                return true;
            }


            TemporalItems.sendClaimingTag(player);

        }else {
            MessagingUtil.sendGenericCantConsole(sender);
        }

        return true;


    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            return Collections.emptyList();

        }

    }

}
