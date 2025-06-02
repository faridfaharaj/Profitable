package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Component assetsComponent = Candles.getHotAssets(player.getWorld().getFullTime(), 0);
                    MessagingUtil.sendMiniMessage(player,

                            MiniMessage.miniMessage().serialize(Component.text("=========== [ HOT Assets ] ===========", TextColor.color(0xFF7C1E)).appendNewline().append(assetsComponent))

                    );
                });

            }else if(args[0].equals("LIQUID")){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Component assetsComponent = Candles.getHotAssets(player.getWorld().getFullTime(), 2);
                    MessagingUtil.sendMiniMessage(player,

                            MiniMessage.miniMessage().serialize(Component.text("========== [ Liquid Assets ] ==========", TextColor.color(0x2BCCFF)).appendNewline().append(assetsComponent))

                    );
                });

            }else if(args[0].equals("GROW")){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Component assetsComponent = Candles.getHotAssets(player.getWorld().getFullTime(), 1);
                    MessagingUtil.sendMiniMessage(player,

                            MiniMessage.miniMessage().serialize(Component.text("========= [ Growing Assets ] =========", TextColor.color(0xFF7A)).appendNewline().append(assetsComponent))

                    );
                });

            }else if(args[0].equals("BIG")){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Component assetsComponent = Candles.getHotAssets(player.getWorld().getFullTime(), 3);
                    MessagingUtil.sendMiniMessage(player,

                            MiniMessage.miniMessage().serialize(Component.text("========= [ Biggest Assets ] =========", TextColor.color(0xFFCA00)).appendNewline().append(assetsComponent))

                    );
                });

            }else{
                MessagingUtil.sendGenericInvalidSubCom(sender, args[0]);
                return true;
            }

            return true;

        }else{

            MessagingUtil.sendGenericCantConsole(sender);

        }


        return true;
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
