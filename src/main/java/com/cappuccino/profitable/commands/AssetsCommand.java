package com.cappuccino.profitable.commands;

import com.cappuccino.profitable.Configuration;
import com.cappuccino.profitable.data.tables.Assets;
import com.cappuccino.profitable.data.tables.Candles;
import com.cappuccino.profitable.tasks.TemporalItems;
import com.cappuccino.profitable.util.NamingUtil;
import net.md_5.bungee.api.chat.TextComponent;
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


public class AssetsCommand implements CommandExecutor {

    public static void sendGraphOptions(Player player, String asset){
        player.sendMessage(NamingUtil.profitableTopSeparator());
        player.sendMessage("Graphs for " + asset + " (In Minecraft time):");

        String[] durations = {"1M", "3M", "6M", "1Y", "2Y"};
        String[] durationsText = {"1 Month", "3 Months", "6 Months", "1 Year", "2 Years"};

        for (String duration : durations) {
            TextComponent textComponent = new TextComponent(ChatColor.YELLOW + "[" + duration + "]");
            textComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                    "/assets peek " + asset + " graph " + duration));
            player.spigot().sendMessage(textComponent);
        }
        player.sendMessage(NamingUtil.profitableBottomSeparator());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0 ||args[0].equals("HOT")){

            if(!sender.hasPermission("profitable.asset.tops")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(sender instanceof Player player){
                player.sendMessage(ChatColor.GOLD +"=========== [ HOT Assets ] ===========");

                List<String> tickers = Candles.getHotAssets(player.getWorld().getFullTime());
                for(int i = 0; i < 9; i++){

                    if(i < tickers.size()){
                        player.sendMessage((i+1) + "-   " + tickers.get(i));
                    }else {
                        player.sendMessage((i+1) + "-   ----   $--.--   $--.--  --.--% -----");
                    }
                }
            }

            return true;
        }

        if(args[0].equals("LIQUID")){

            if(!sender.hasPermission("profitable.asset.tops")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(sender instanceof Player player){
                player.sendMessage(ChatColor.GOLD +"========== [ Liquid Assets ] ==========");

                List<String> tickers = Candles.getLiquidAssets(player.getWorld().getFullTime());
                for(int i = 0; i < 9; i++){

                    if(i < tickers.size()){
                        player.sendMessage((i+1) + "-   " + tickers.get(i));
                    }else {
                        player.sendMessage((i+1) + "-   ----   $--.--   $--.--  --.--% -----");
                    }
                }
            }

            return true;
        }

        if(args[0].equals("TOP")){

            if(!sender.hasPermission("profitable.asset.tops")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(sender instanceof Player player){
                player.sendMessage(ChatColor.GOLD +"=========== [ Top Assets ] ===========");

                List<String> tickers = Candles.getPerformingAssets(player.getWorld().getFullTime());
                for(int i = 0; i < 9; i++){

                    if(i < tickers.size()){
                        player.sendMessage((i+1) + "-   " + tickers.get(i));
                    }else {
                        player.sendMessage((i+1) + "-   ----   $--.--   $--.--  --.--% -----");
                    }
                }
            }

            return true;
        }

        if(args[0].equals("BIG")){

            if(!sender.hasPermission("profitable.asset.tops")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(sender instanceof Player player){
                player.sendMessage(ChatColor.GOLD +"========= [ Biggest Assets ] =========");

                List<String> tickers = Candles.getExpensiveAssets(player.getWorld().getFullTime());
                for(int i = 0; i < 9; i++){

                    if(i < tickers.size()){
                        player.sendMessage((i+1) + "-   " + tickers.get(i));
                    }else {
                        player.sendMessage((i+1) + "-   ----   $--.--   $--.--  --.--% -----");
                    }
                }
            }

            return true;
        }

        if(args.length == 1){
            if(args[0].equals("peek")){
                sender.sendMessage(ChatColor.RED + "/assets peek <Asset>");
                return true;
            }
        }

        if(sender instanceof Player player){

            if(Objects.equals(args[0], "categories")){

                if(!sender.hasPermission("profitable.asset.categories")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                if(args.length < 2){
                    sender.sendMessage(ChatColor.RED +"/assets category <Asset Category>");
                    return true;
                }

                int assetType;
                if(args[1].equals("currency")){
                    assetType = 1;
                }else if(args[1].equals("commodity")){

                    sender.sendMessage(NamingUtil.profitableTopSeparator());
                    sender.sendMessage("Showing all tradeable Commodities: \n");
                    sender.sendMessage(Assets.getRegisteredAssetType(2).toString());
                    sender.sendMessage(Assets.getRegisteredAssetType(3).toString());
                    sender.sendMessage(NamingUtil.profitableBottomSeparator());

                    return true;

                }else{

                    sender.sendMessage(ChatColor.RED + "Invalid Asset Type");
                    return true;

                }

                sender.sendMessage(NamingUtil.profitableTopSeparator());
                sender.sendMessage("Showing all "+ NamingUtil.nameType(assetType)+ " assets: \n");
                sender.sendMessage(Assets.getRegisteredAssetType(assetType).toString());
                sender.sendMessage(NamingUtil.profitableBottomSeparator());
                return true;
            }

            if(Objects.equals(args[0], "peek")) {

                if(!sender.hasPermission("profitable.asset.info")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                if(args.length == 2){


                    if(Objects.equals(args[1], Configuration.MAINCURRENCYASSET.getCode())) {
                        player.sendMessage(ChatColor.RED+"This is the Main currency");
                        return true;
                    }

                    TemporalItems.sendInfoBook(player, args[1]);
                    return true;
                }

                if (args[2].equals("graph")) {

                    if(!sender.hasPermission("profitable.asset.graphs")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    if(Objects.equals(args[1], Configuration.MAINCURRENCYASSET.getCode())) {
                        player.sendMessage(ChatColor.RED+"This is the Main currency");
                        return true;
                    }

                    if (args.length == 3) {

                        sendGraphOptions(player, args[1]);

                        return true;
                    }

                    long timeFrame;

                    switch (args[3]) {
                        case "1M":
                            timeFrame = 720000;
                            break;

                        case "3M":
                            timeFrame = 2160000;
                            break;

                        case "6M":
                            timeFrame = 4320000;
                            break;

                        case "1Y":
                            timeFrame = 8760000;
                            break;

                        case "2Y":
                            timeFrame = 17520000;
                            break;

                        default:
                            sender.sendMessage(ChatColor.RED+"Invalid time frame");
                            return true;


                    }

                    TemporalItems.sendGraphMap(player, args[1], timeFrame, args[3]);

                    return true;
                }else{
                    sender.sendMessage(ChatColor.RED+"/asset peek <asset> graph <Time frame>");
                    return true;
                }

            }

        }


        return false;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){
                List<String> options = new ArrayList<>(List.of("categories", "peek", "TOP", "LIQUID", "HOT", "BIG"));

                StringUtil.copyPartialMatches(args[0], options, suggestions);
            }

            if(args.length >= 2){
                if(Objects.equals(args[0], "peek")){
                    if(args.length == 2){
                        List<String> options = new ArrayList<>(Configuration.ALLOWEITEMS);
                        options.addAll(Configuration.ALLOWENTITIES);
                        if(Configuration.VAULTENABLED){
                            options.add("VLT");
                        }

                        StringUtil.copyPartialMatches(args[1], options, suggestions);
                    }else if(args.length == 3){
                        suggestions = List.of("graph");
                    }else if(args.length == 4){
                        suggestions = List.of("1M", "3M", "6M", "1Y", "2Y");
                    }
                } else if (Objects.equals(args[0], "categories")) {
                    if(args.length == 2){
                        suggestions = List.of("currency", "commodity");
                    }
                }

            }

            return suggestions;
        }

    }
}
