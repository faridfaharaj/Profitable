package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.exchange.Books.Exchange;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.MessagingUtil;
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

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
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

            Order.OrderType orderType;
            double price;

            if (args.length > 2) {
                orderType = Order.OrderType.LIMIT;

                try{
                    price = Double.parseDouble(args[2]);
                }catch (Exception e){
                    MessagingUtil.sendError(sender, "Invalid Price");
                    return true;
                }

            }else {
                orderType = Order.OrderType.MARKET;
                price = sideBuy?Double.MAX_VALUE:Double.MIN_VALUE;
            }

            if(args.length > 3){
                if(args[3].equals("stop-limit")){
                    orderType = Order.OrderType.STOP_LIMIT;
                }else if(args[3].equals("stop-market")){
                    orderType = Order.OrderType.STOP_MARKET;
                } else if (args[3].equals("limit")) {

                }else if (args[3].equals("market")) {

                    MessagingUtil.sendWarning(sender, "Ignoring price for market order");
                    price = sideBuy?Double.MAX_VALUE:Double.MIN_VALUE;
                    orderType = Order.OrderType.MARKET;

                } else{
                    MessagingUtil.sendError(sender,"Invalid Order Type");
                    return true;
                }
            }

            double units;
            if(args.length <= 1){

                units = 1d;

            }else{
                try{
                    units = Double.parseDouble(args[1]);
                }catch (Exception e){
                    MessagingUtil.sendError(sender, "Invalid Units");
                    return true;
                }
            }


            Exchange.sendNewOrder(player, new Order(UUID.randomUUID(), Accounts.getAccount(player), args[0], sideBuy, price, units, orderType));
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
                if(VaultHook.isConnected()){
                    options.add(VaultHook.getAsset().getCode());
                }

                StringUtil.copyPartialMatches(args[0], options, suggestions);
            }

            if(args.length == 2){
                suggestions = Collections.singletonList("[<Units>]");
            }

            if(args.length == 3){
                suggestions = Collections.singletonList("[<Price each>]");
            }

            if(args.length == 4){
                suggestions = List.of("stop-limit","limit","market");
            }

            return suggestions;

        }

    }
}
