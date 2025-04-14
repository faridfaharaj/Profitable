package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TopCommand  implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }
        if(sender instanceof Player player){

            if(!sender.hasPermission("profitable.asset.tops")){
                MessagingUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length == 0 || args[0].equals("HOT")){

                MessagingUtil.sendCustomMessage(player,

                        Component.text("=========== [ HOT Assets ] ===========", TextColor.color(0xFF7C1E)).appendNewline().append(Candles.getHotAssets(player.getWorld().getFullTime(), 0))

                );

            }else if(args[0].equals("LIQUID")){

                MessagingUtil.sendCustomMessage(player,

                        Component.text("========== [ Liquid Assets ] ==========", TextColor.color(0x2BCCFF)).appendNewline().append(Candles.getHotAssets(player.getWorld().getFullTime(),2))

                );

            }else if(args[0].equals("GROW")){

                MessagingUtil.sendCustomMessage(player,

                        Component.text("========= [ Growing Assets ] =========", TextColor.color(0xFF7A)).appendNewline().append(Candles.getHotAssets(player.getWorld().getFullTime(),1))

                );

            }else if(args[0].equals("BIG")){

                MessagingUtil.sendCustomMessage(player,

                        Component.text("========= [ Biggest Assets ] =========", TextColor.color(0xFFCA00)).appendNewline().append(Candles.getHotAssets(player.getWorld().getFullTime(),3))

                );

            }else{
                MessagingUtil.sendError(sender, "Invalid Subcommand");
                return true;
            }

            return true;

        }else{

            MessagingUtil.sendGenericCantConsole(sender);

        }


        return false;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            if(args.length == 1){
                return List.of("GROW","HOT", "BIG","LIQUID");
            }

            return Collections.emptyList();

        }

    }

}
