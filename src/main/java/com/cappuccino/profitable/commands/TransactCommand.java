package com.cappuccino.profitable.commands;

import com.cappuccino.profitable.Configuration;
import com.cappuccino.profitable.exchange.Books.Exchange;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransactCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length < 3){
            return false;
        }

        if(sender instanceof Player player){

            boolean sideBuy;
            if(command.getName().equals("buy")){
                sideBuy = true;
            }else if(command.getName().equals("sell")){
                sideBuy = false;
            }else{
                return false;
            }

            double price;

            if(Objects.equals(args[1], "market")){

                price = sideBuy?Double.MAX_VALUE:Double.MIN_VALUE;

            }else if(Objects.equals(args[1], "limit")){

                if(args.length < 4){
                    player.sendMessage(ChatColor.RED + "Missing Price");
                    return true;
                }

                try{
                    price = Double.parseDouble(args[3]);
                }catch (Exception e){
                    player.sendMessage(ChatColor.RED + "Invalid Price");
                    return true;
                }

            }else{
                player.sendMessage(ChatColor.RED+ args[1] + " is not a valid type of order");
                return true;
            }

            Double units;
            try{
                units = Double.parseDouble(args[2]);
            }catch (Exception e){
                player.sendMessage(ChatColor.RED + "Invalid Units");
                return true;
            }

            Exchange.sendNewOrder(player, args[0], sideBuy, price, units);
            return true;

        }

        return false;

    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){
                List<String> options = new ArrayList<>(Configuration.ALLOWEITEMS);
                options.addAll(Configuration.ALLOWENTITIES);
                if(Configuration.VAULTENABLED){
                    options.add("VLT");
                }

                StringUtil.copyPartialMatches(args[0], options, suggestions);
            }

            if(args.length == 2){
                suggestions = List.of("limit", "market");
            }

            if(args.length == 3){
                suggestions = List.of("[<Units>]");
            }

            if(args.length == 4 && !Objects.equals(args[1], "market")){
                suggestions = List.of("[<Price each>]");
            }

            return suggestions;

        }

    }
}
