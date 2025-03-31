package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.exchange.Books.Exchange;
import com.faridfaharaj.profitable.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class TransactCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = null;
        if(sender instanceof Player got){
            player = got;
        }

        if(player != null){

            boolean sideBuy;
            if(command.getName().equals("buy")){
                sideBuy = true;
            }else if(command.getName().equals("sell")){
                sideBuy = false;
            }else{
                return false;
            }

            double price;

            if(args.length <= 1){
                TextUtil.sendError(sender, "/" + command.getName() + " <Asset> <Order Type> <args>..");
                return true;
            }

            if(Objects.equals(args[1], "market")){

                if(args.length < 3){
                    TextUtil.sendError(sender, "/" + command.getName() + " <Asset> market <Units>");
                    return true;
                }

                price = sideBuy?Double.MAX_VALUE:Double.MIN_VALUE;

            }else if(Objects.equals(args[1], "limit")){

                if(args.length < 4){
                    TextUtil.sendError(sender, "/" + command.getName() + " <Asset> limit <Units> <Price>");
                    return true;
                }

                try{
                    price = Double.parseDouble(args[3]);
                }catch (Exception e){
                    TextUtil.sendError(sender, "Invalid Price");
                    return true;
                }

            }else{
                TextUtil.sendError(sender, args[1] + " is not a valid type of order");
                return true;
            }

            Double units;
            try{
                units = Double.parseDouble(args[2]);
            }catch (Exception e){
                TextUtil.sendError(sender, "Invalid Units");
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
                suggestions = Arrays.asList("limit", "market");
            }

            if(args.length == 3){
                suggestions = Collections.singletonList("[<Units>]");
            }

            if(args.length == 4 && !Objects.equals(args[1], "market")){
                suggestions = Collections.singletonList("[<Price each>]");
            }

            return suggestions;

        }

    }
}
