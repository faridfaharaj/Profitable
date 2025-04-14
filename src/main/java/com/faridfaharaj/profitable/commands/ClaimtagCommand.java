package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.util.TextUtil;
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

        if(!sender.hasPermission("profitable.account.claim")){
            TextUtil.sendGenericMissingPerm(sender);
            return true;
        }

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }

        Player player = null;
        if(sender instanceof Player got){
            player = got;
        }
        if(player != null){

            TemporalItems.sendClaimingTag(player);

            return true;

        }else{
            TextUtil.sendGenericCantConsole(sender);
        }

        return false;


    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            return Collections.emptyList();

        }

    }

}
