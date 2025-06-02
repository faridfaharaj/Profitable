package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
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

        if(sender instanceof Player player){

            if(Configuration.MULTIWORLD){
                DataBase.universalUpdateWorld(sender);
            }


            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {

                String asset;
                if(args.length == 0 || args[0].equals("hand")){
                    asset = player.getInventory().getItemInMainHand().getType().name();
                }else {
                    asset = args[0].toUpperCase();
                }

                boolean sideBuy;
                if(command.getName().equals("buy")){
                    sideBuy = true;
                }else if(command.getName().equals("sell")){
                    sideBuy = false;
                }else{
                    return;
                }

                Order.OrderType orderType;
                double price;

                if (args.length > 2) {
                    orderType = Order.OrderType.LIMIT;

                    try{
                        price = Double.parseDouble(args[2]);
                    }catch (Exception e){
                        MessagingUtil.sendGenericInvalidAmount(sender, args[2]);
                        return;
                    }

                }else {
                    orderType = Order.OrderType.MARKET;
                    price = sideBuy?Double.MAX_VALUE:Double.MIN_VALUE;
                }

                if(args.length > 3){
                    switch (args[3]) {
                        case "stop-limit" -> orderType = Order.OrderType.STOP_LIMIT;
                        case "stop-market" -> orderType = Order.OrderType.STOP_MARKET;
                        case "limit" -> {
                        }
                        case "market" -> {

                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("exchange.warning.market-ignores-price"));
                            price = sideBuy ? Double.MAX_VALUE : Double.MIN_VALUE;
                            orderType = Order.OrderType.MARKET;
                        }
                        default -> {
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("exchange.error.invalid-order-type"));
                            return;
                        }
                    }
                }

                double units;
                if(args.length <= 1){

                    units = 1d;

                }else{
                    try{
                        units = Double.parseDouble(args[1]);
                    }catch (Exception e){
                        MessagingUtil.sendGenericInvalidAmount(sender, args[1]);
                        return;
                    }
                }

                double finalPrice = price;
                Order.OrderType finalOrderType = orderType;

                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("exchange.loading-order"));

                Profitable.getfolialib().getScheduler().runAsync(async -> {

                    Exchange.sendNewOrder(player, new Order(UUID.randomUUID(), Accounts.getAccount(player), asset, sideBuy, finalPrice, units, finalOrderType));

                });
            });
            return true;

        }else{

            MessagingUtil.sendGenericCantConsole(sender);

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
                options.add("hand");

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
