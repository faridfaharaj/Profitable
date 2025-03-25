package com.cappuccino.profitable.commands;

import com.cappuccino.profitable.Configuration;
import com.cappuccino.profitable.Profitable;
import com.cappuccino.profitable.data.tables.*;
import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.data.holderClasses.Order;
import com.cappuccino.profitable.util.NamingUtil;
import com.cappuccino.profitable.util.RandomUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.*;

public class AdminCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(args.length == 0){

            sender.sendMessage("Admin Help:");
            sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");
            sender.sendMessage(

                     ChatColor.YELLOW +
                            "\n" +
                             "getplayeracc <player> <-- gets player's active account\n\n" +
                             "forcelogout <player> <-- logs someone out of an account\n\n" +
                            "account <account> wallet <-- shows account asset balances\n\n" +
                            "account <account> wallet <asset> <amount> <-- allows you to set an account's asset balance to a specific amount\n\n" +
                            "account <account> passwordreset <-- turns an account's password into '1234' for recovery\n\n" +
                            "account <account> orders <-- shows all active orders on the account\n\n" +
                            "account <account> delivery <-- shows delivery locations on the account\n\n" +
                            "account <account> delivery setitem <x> <y> <z> <world name> <-- allows you to set an account's item delivery location\n\n" +
                            "account <account> delivery setentity <x> <y> <z> <world name> <-- allows you to set an account's entity delivery location\n\n" +
                            "account <account> claimid <-- shows the nametag name that recognizes someone's owned entities\n\n" +
                            "account <account> delete <-- deletes an account\n\n" +
                            "\n\n" +
                            "orders findbyasset <asset> <-- shows all orders from a specific asset\n\n" +
                            "orders getbyid <ID> cancel <-- cancels an order, giving collateral back to owner\n\n" +
                            "orders getbyid <ID> delete <-- deletes an order\n\n" +
                            "orders deleteall <-- deletes all existing orders\n\n" +
                            "orders cancelall <-- cancels all existing orders (warning: may be heavy)\n\n" +
                            "orders newlimitorder <asset> <price> <units> <-- creates a limit order from thin air (useful for asset's initial supply)\n\n" +
                            "\n\n" +
                            "assets <-- shows all registered assets\n\n" +
                            "assets register commodityEntity <code> <-- allows you to enable trading for a specific entity (won't account for multiple worlds if not in config)\n\n" +
                            "assets register commodityItem <code> <-- allows you to enable trading for a specific item (won't account for multiple worlds if not in config)\n\n" +
                            "assets register currency <code> <-- allows you to create a currency to trade in the exchange\n\n" +
                            "assets fromid <asset> delete <-- deletes a certain asset (will come back after a restart if asset is in config)\n\n" +
                            "assets fromid <asset> newtransaction <price> <volume> <-- fakes transactions to make the illusion of market movement on an asset\n\n" +
                            "assets fromid <asset> resettransactions <-- deletes all records of transactions from an asset (this kills graphs)"

            );
            sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");

            return true;
        }

        if(Objects.equals(args[0], "forcelogout")){

            if(!sender.hasPermission("profitable.admin.accounts.manage.forcelogout")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(args.length < 2){
                sender.sendMessage(ChatColor.RED + "/admin forcelogout <player>");
                return true;
            }

            Player player = Profitable.getInstance().getServer().getPlayer(args[1]);
            if(player == null){
                sender.sendMessage( ChatColor.RED + args[1] + " isn't online");
                return true;
            }

            Accounts.logOut(player.getUniqueId());
            sender.sendMessage("Logged "+ player.getName() + " out");
            return true;
        }


        if(Objects.equals(args[0], "assets")){

            if(args.length == 1){

                if(!sender.hasPermission("profitable.admin.assets.info.getallassets")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                sender.sendMessage(ChatColor.YELLOW +"Showing all registered assets in Profitable:");
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");
                sender.sendMessage(Assets.getAll().toString());
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");

                return true;
            }

            if(Objects.equals(args[1], "register")){

                if(!sender.hasPermission("profitable.admin.assets.manage.register")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                if(args.length < 4){

                    sender.sendMessage(ChatColor.RED+"/admin register currency <Asset Type> <Symbol>");

                    return true;
                }


                String asset = args[3].toUpperCase();

                if(asset.length() > 3){
                    sender.sendMessage(ChatColor.RED+"Currencies must only have 3 letters");
                    return true;
                }

                switch (args[2]){
                    case "currency":

                        String name = args.length  == 5? args[4]:asset.toLowerCase();

                        try {
                            if(Assets.registerAsset(asset, 1, Asset.metaData(RandomUtil.randomChatColor(), name))){
                                sender.sendMessage("Registered " + asset);
                            }else{
                                sender.sendMessage(ChatColor.RED + "There is already an asset with Symbol: " + asset);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return true;
                    case "commodityitem":

                        if(Material.getMaterial(asset) != null){

                            try {
                                if(Assets.registerAsset(asset, 2, Asset.metaData(RandomUtil.randomChatColor(), NamingUtil.nameCommodity(asset)))){
                                    Configuration.ALLOWEITEMS.add(asset);
                                    sender.sendMessage("Registered " + asset);
                                    sender.sendMessage("You should add this to the config file");
                                }else{
                                    sender.sendMessage(ChatColor.RED + asset + " is already registered");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }else{
                            sender.sendMessage(ChatColor.RED+"Commodities must come from an existing item");
                        }

                        return true;
                    case "commodityentity":

                        if(EntityType.fromName(asset) != null){

                            try {
                                if(Assets.registerAsset(asset, 3, Asset.metaData(RandomUtil.randomChatColor(), NamingUtil.nameCommodity(asset)))){
                                    Configuration.ALLOWENTITIES.add(asset);
                                    sender.sendMessage("Registered " + asset);
                                    sender.sendMessage("You should add this to the config file");
                                }else{
                                    sender.sendMessage(ChatColor.RED + asset + " is already registered");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }else{
                            sender.sendMessage(ChatColor.RED+"Commodities must come from an existing entity");
                        }

                        return true;
                    default:
                        sender.sendMessage(ChatColor.RED+ "Invalid asset type");
                        return true;
                }

            }

            if(Objects.equals(args[1], "fromid")){

                if(args.length < 4){
                    return false;
                }

                if(Objects.equals(args[3], "newtransaction")){

                    if(!sender.hasPermission("profitable.admin.assets.manage.newtransaction")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    if(args.length == 4){
                        sender.sendMessage(ChatColor.RED+"Price missing");
                        return true;
                    }

                    if(args.length == 5){
                        sender.sendMessage(ChatColor.RED+"Volume missing");
                        return true;
                    }

                    if(sender instanceof Player player){

                        Candles.updateDay(args[2], player.getWorld(), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                        sender.sendMessage("inserted transaction in " + args[2]);
                    }else {

                        if(args.length == 6){
                            sender.sendMessage(ChatColor.RED+"world is missing");
                            return true;
                        }

                        Candles.updateDay(args[2], Profitable.getInstance().getServer().getWorld(args[6]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                        sender.sendMessage("inserted transaction in " + args[2]);
                    }

                    return true;

                }else if(Objects.equals(args[3], "resettransactions")){

                    if(!sender.hasPermission("profitable.admin.assets.manage.resettransactions")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    Candles.assetDeleteAllCandles(args[2]);

                } else if (Objects.equals(args[3], "delete")) {

                    if(!sender.hasPermission("profitable.admin.assets.manage.delete")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    Assets.deleteAsset(args[2]);

                }

            }

        }

        if(Objects.equals(args[0], "orders")){

            if(args.length == 1){
                return false;
            }

            if(Objects.equals(args[1], "findbyasset")){

                if(!sender.hasPermission("profitable.admin.orders.info.findbyasset")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                if(args.length < 3){

                    sender.sendMessage(ChatColor.RED+ "Must specify an asset");
                    return true;
                }

                List<String> ordersString = new ArrayList<>();
                List<Order> orders = Orders.getAssetOrders(args[2]);
                for(Order order : orders){
                    ordersString.add(order.toString());
                }


                sender.sendMessage(ChatColor.YELLOW +"Showing all active orders for " + args[2] + ":");
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");
                sender.sendMessage(ordersString.toString());
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");

                return true;

            }

            if(Objects.equals(args[1], "getbyid")){

                if(args.length < 3){

                    sender.sendMessage(ChatColor.RED+ "/admin orders getbyid <ID> <Action>");
                    return true;

                }

                if(args.length < 4){

                    sender.sendMessage(ChatColor.RED+ "/admin orders getbyid <ID> <Action>");
                    return true;

                }

                if(Objects.equals(args[3], "cancel")){

                    if(!sender.hasPermission("profitable.admin.orders.manage.cancel")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    if(Orders.cancelOrder(args[2])){
                        sender.sendMessage("Canceled: "+ args[2]);
                        return true;
                    }else{
                        sender.sendMessage(ChatColor.RED+ "Couldn't cancel that order");
                        return true;
                    }

                }

                if(Objects.equals(args[3], "delete")){

                    if(!sender.hasPermission("profitable.admin.orders.manage")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    Orders.deleteOrder(args[2]);
                    sender.sendMessage("executed");

                    return true;

                }

            }

            if (Objects.equals(args[1], "deleteall")) {

                if(!sender.hasPermission("profitable.admin.orders.manage.deleteall")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                Orders.deleteAllOrders();
                sender.sendMessage("DELETED all orders from all assets");
                return true;

            }

            if (Objects.equals(args[1], "cancelall")) {

                if(!sender.hasPermission("profitable.admin.orders.manage.cancelall")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                List<Order> orders = Orders.getAllOrders();
                for(Order order : orders){
                    Orders.cancelOrder(order.getUuid());
                }
                sender.sendMessage("Cancelled all orders from all assets");

                return true;

            }

            if (Objects.equals(args[1], "newlimitorder")) {

                if(!sender.hasPermission("profitable.admin.orders.manage.newlimitorder")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                if(args.length < 6){

                    sender.sendMessage(ChatColor.RED+ "/admin orders newlimitorder <asset> <side> <price> <units>");

                    return true;
                }

                boolean sidebuy = Objects.equals(args[3], "buy");

                double units;
                double price;

                try{

                    units = Double.parseDouble(args[4]);
                    price = Double.parseDouble(args[5]);

                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Invalid Price/Units");
                    return true;
                }


                if(sender instanceof Player player){
                    String account = Accounts.getAccount(player);
                    Orders.insertOrder(UUID.randomUUID().toString(), account, args[2], sidebuy, price, units);
                    sender.sendMessage("Trying to place limit order to " + sidebuy + " " + units + " " + args[2] + " at $" + price + " in " + account + "...");
                    return true;
                }

            }

        }




        String account;

        if(Objects.equals(args[0], "getplayeracc")){

            if(!sender.hasPermission("profitable.admin.accounts.info.getplayeracc")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(args.length < 2){
                sender.sendMessage(ChatColor.RED + "/admin getplayeracc <player>");
                return true;
            }

            Player player = Profitable.getInstance().getServer().getPlayer(args[1]);
            if(player == null){
                sender.sendMessage( ChatColor.RED + args[1] + " isn't online");
                return true;
            }

            if(args.length == 2){
                sender.sendMessage(player.getName() + "'s active account is: " + Accounts.getCurrentAccounts().getOrDefault(player.getUniqueId(), ChatColor.GRAY+"(Default)"));
                return true;
            }
            account = Accounts.getAccount(player);

        }else if (Objects.equals(args[0], "account")){

            if(args.length < 2){
                return false;
            }
            account = args[1];

        }else {
            return false;
        }

        if(args.length < 3){
            return false;
        }

        if(Objects.equals(args[2], "wallet")){

            if(args.length == 3){

                if(!sender.hasPermission("profitable.admin.accounts.info.wallet")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                List<String> balances = AccountHoldings.AssetBalancesToString(account, 1);
                if (!balances.isEmpty()) {
                    sender.sendMessage(NamingUtil.profitableTopSeparator());
                    sender.sendMessage(account +"'s Wallet:");
                    for (String balance : balances) {

                        sender.sendMessage(balance);

                    }
                    sender.sendMessage(NamingUtil.profitableBottomSeparator());
                } else {
                    sender.sendMessage(NamingUtil.profitablePrefix() + ChatColor.DARK_GRAY + "Nothing found");
                }

                return true;
            }

            if(args.length < 5){
                sender.sendMessage(ChatColor.RED+"/admin account <Account> wallet <Asset> <Amount>");
                return true;
            }

            if(!sender.hasPermission("profitable.admin.accounts.manage.wallet")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            Double ammount;
            try{
                ammount = Double.parseDouble(args[4]);
            }catch (Exception e){
                sender.sendMessage(ChatColor.RED+ "Invalid ammount");
                return true;
            }

            AccountHoldings.setHolding(account, args[3], ammount);
            sender.sendMessage("Set " + args[3]+ " to "+ ammount + ", on" + account, "'s wallet");
            return true;
        }

        if(Objects.equals(args[2], "passwordreset")){

            if(!sender.hasPermission("profitable.admin.accounts.manage.passwordreset")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            Accounts.changePassword(account, "1234");
            sender.sendMessage(account+ "'s password set to '1234' for recovery");
            return true;
        }

        if(Objects.equals(args[2], "orders")){

            if(!sender.hasPermission("profitable.admin.accounts.info.orders")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            List<Order> orders = Orders.getAccountOrders(account);
            if(orders.isEmpty()){
                sender.sendMessage(NamingUtil.profitablePrefix()+ChatColor.GRAY + "No active orders on this account");
            }else {
                List<String> ordersString = new ArrayList<>();
                for(Order order : orders){
                    ordersString.add(order.toString());
                }

                sender.sendMessage(ChatColor.YELLOW +"Showing all active orders on account " + account + ":");
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");
                sender.sendMessage(ordersString.toString());
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");
            }
            return true;
        }

        if(Objects.equals(args[2], "delivery")){

            if(args.length == 3){

                if(!sender.hasPermission("profitable.admin.accounts.info.delivery")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                Location entityDelivery = Accounts.getEntityDelivery(account);
                Location itemDelivery = Accounts.getItemDelivery(account);

                sender.sendMessage(ChatColor.YELLOW +"Delivery " + account + ":");
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");
                sender.sendMessage("Item Delivery Location:");
                sender.sendMessage(ChatColor.YELLOW+(itemDelivery == null?"Not set":itemDelivery.toVector() + " (" + itemDelivery.getWorld().getName()+")"));
                sender.sendMessage("");
                sender.sendMessage("Entity Delivery Location:");
                sender.sendMessage(ChatColor.YELLOW+(entityDelivery == null?"Not set":entityDelivery.toVector() + " (" + entityDelivery.getWorld().getName()+")"));
                sender.sendMessage(ChatColor.YELLOW +"--------------------------------------------");

                return true;
            }

            if(args.length < 7){

                sender.sendMessage(ChatColor.RED + "/admin account <account> delivery setitem <x> <y> <z> <world (optional)>");

                return true;
            }

            if(!sender.hasPermission("profitable.admin.accounts.manage.delivery")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            World world;
            if(args.length == 7){
                if(sender instanceof Player player){
                    world = player.getWorld();
                } else {
                    sender.sendMessage( ChatColor.RED+"Must specify world when running command from console");
                    return true;
                }
            }else {
                world = Profitable.getInstance().getServer().getWorld(args[7]);
            }

            if(world == null){

                sender.sendMessage( ChatColor.RED+"invalid world");

                return true;
            }

            double x,y,z;

            try{
                x = Double.parseDouble(args[4]);
                y = Double.parseDouble(args[5]);
                z = Double.parseDouble(args[6]);
            }catch (Exception e){
                sender.sendMessage(ChatColor.RED + "Invalid coordinates");
                return  true;
            }

            Location location = new Location(world, x, y, z);

            if(Objects.equals(args[3], "setitem")){

                if(Accounts.changeItemDelivery(account, location)){
                    sender.sendMessage("changed " + account + " item delivery to:" + location.toVector());
                }else {
                    sender.sendMessage(ChatColor.RED+ "couldn't change item delivery location");
                }


            }

            if(Objects.equals(args[3], "setentity")){

                if(Accounts.changeEntityDelivery(account, location)){
                    sender.sendMessage("changed " + account + " entity delivery to:" + location.toVector());
                }else {
                    sender.sendMessage(ChatColor.RED+ "couldn't change item delivery location");
                }

            }

            return true;

        }

        if(Objects.equals(args[2], "claimid")){

            if(!sender.hasPermission("profitable.admin.accounts.info.claimid")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            sender.sendMessage(NamingUtil.profitablePrefix()+ account + "'s Entity claim id: " + Accounts.getEntityClaimId(account));

            return true;

        }

        if(Objects.equals(args[2], "delete")){

            if(!sender.hasPermission("profitable.admin.accounts.manage.delete")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(args.length < 4){

                sender.sendMessage(ChatColor.RED + "Must write account name again as confirmation");

                return true;
            }

            if(Objects.equals(account, args[3])){
                if(Accounts.getCurrentAccounts().containsValue(account)){
                    sender.sendMessage(ChatColor.RED + "someone is still using this account");
                }else {
                    Accounts.deleteAccount(account);
                    sender.sendMessage("DELETED account: " + account);
                }
            }

            return true;

        }

        return false;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if (args.length == 1) {
                suggestions = List.of("account", "orders", "assets", "getplayeracc","forcelogout");
            }

            if(Objects.equals(args[0], "getplayeracc") || Objects.equals(args[0], "forcelogout")){
                if (args.length == 2) {
                    return null;
                }
            }

            if(Objects.equals(args[0], "account") || Objects.equals(args[0], "getplayeracc")){
                if (args.length == 2) {

                    if(Objects.equals(args[0], "getplayeracc")){
                        return null;
                    }else {
                        suggestions = List.of("[<account>]");
                    }

                }

                if(args.length > 2){
                    if (args.length == 3) {
                        List<String> options = List.of("wallet", "passwordreset", "orders", "delivery", "claimid", "delete");

                        StringUtil.copyPartialMatches(args[2], options, suggestions);
                    }

                    if(args.length > 3){

                        if(Objects.equals(args[2], "delete")){

                            if(args.length == 4){
                                suggestions = List.of("[<Account>]");
                            }

                        }

                        if(Objects.equals(args[2], "wallet")){

                            if(args.length == 4){
                                suggestions = List.of("[<Asset>]");

                            }

                            if(args.length == 5){
                                suggestions = List.of("[<Amount>]");

                            }

                        }

                        if(Objects.equals(args[2], "delivery")){

                            if(args.length == 4){
                                suggestions = List.of("setitem", "setentity");

                            }

                            if(args.length == 5){
                                suggestions = List.of("[<x>]");
                            }

                            if(args.length == 6){
                                suggestions = List.of("[<y>]");
                            }

                            if(args.length == 7){
                                suggestions = List.of("[<z>]");
                            }

                            if(args.length == 8){
                                suggestions = List.of("[<world name>]");
                            }

                        }
                    }
                }

            }

            if(Objects.equals(args[0], "orders")){
                if (args.length == 2) {
                    List<String> options = List.of("findbyasset", "getbyid", "deleteall", "cancelall", "newlimitorder");

                    StringUtil.copyPartialMatches(args[1], options, suggestions);
                }

                if(args.length > 2){
                    if (Objects.equals(args[1], "newlimitorder")) {

                        if(args.length == 3){
                            suggestions = List.of("[<Asset>]");
                        }

                        if(args.length == 4){
                            suggestions = List.of("buy", "sell");
                        }

                        if(args.length == 5){
                            suggestions = List.of("[<Units>]");
                        }

                        if(args.length == 6){
                            suggestions = List.of("[<Price>]");
                        }
                    }

                    if (Objects.equals(args[1], "findbyasset")){

                        if(args.length == 3){
                            suggestions = List.of("[<Asset>]");
                        }

                    }

                    if (Objects.equals(args[1], "getbyid")) {

                        if(args.length == 3){
                            suggestions = List.of("[<ID>]");
                        }

                        if(args.length == 4){
                            suggestions = List.of("cancel", "delete");
                        }

                    }


                }

            }

            if(Objects.equals(args[0], "assets")){

                if (args.length == 2) {
                    List<String> options = List.of("register", "fromid");

                    StringUtil.copyPartialMatches(args[1], options, suggestions);
                }

                if(args.length > 2){

                    if (Objects.equals(args[1], "register")) {

                        if(args.length == 3){
                            suggestions = List.of("commodityentity", "commodityitem", "currency");
                        }

                        if(args.length == 4){
                            suggestions = List.of("[<Symbol>]");
                        }

                        if(args.length == 5){
                            suggestions = List.of("[<Name>]");
                        }

                    }

                    if (Objects.equals(args[1], "fromid")) {

                        if(args.length == 3){
                            suggestions = List.of("[<Asset>]");
                        }

                        if(args.length == 4){
                            suggestions = List.of("delete", "newtransaction", "resettransactions");
                        }

                        if(args.length > 4){
                            if(Objects.equals(args[3], "newtransaction")){

                                if(args.length == 5){
                                    suggestions = List.of("[<price>]");
                                }

                                if(args.length == 6){
                                    suggestions = List.of("[<volume>]");
                                }

                            }
                        }

                    }

                }

            }

            return suggestions;
        }
    }


    // getplayeracc <player> <-- shows player's current active account

    // account <account> wallet <-- shows account asset balances
    // account <account> wallet <asset> <amount> <-- allows you to set an account's asset balance to a specific amount
    // account <account> passwordreset <-- turns an account's password into '1234' for recovery
    // account <account> orders <-- shows all active orders on the account
    // account <account> delivery <-- shows delivery locations on the account
    // account <account> delivery setitem <x> <y> <z> <world name> <-- allows you to set an account's item delivery location
    // account <account> delivery setentity <x> <y> <z> <world name> <-- allows you to set an account's entity delivery location
    // account <account> claimid <-- shows the nametag name that recognizes someone's owned entities
    // account <account> delete <-- deletes an account

    // orders findbyasset <asset> <-- shows all orders from a specific asset
    // orders getbyid <ID> cancel <-- cancels an order, giving collateral back to owner
    // orders getbyid <ID> delete <-- deletes an order
    // orders deleteall <-- deletes all existing orders
    // orders cancelall <-- cancels all existing orders (warning: may be heavy)
    // orders newlimitorder <asset> <price> <units> <-- creates a limit order from thin air (useful for asset's initial supply)

    // assets <-- shows all registered assets
    // assets register commodityEntity <code> <-- allows you to enable trading for a specific entity (won't account for multiple worlds if not in config)
    // assets register commodityItem <code> <-- allows you to enable trading for a specific item (won't account for multiple worlds if not in config)
    // assets register currency <code> <-- allows you to create a currency to trade in the exchange
    // assets fromid <asset> delete <-- deletes a certain asset (will come back if in config)
    // assets fromid <asset> newtransaction <price> <volume> <-- fakes transactions to make the illusion of market movement on an asset
    // assets fromid <asset> resettransactions <-- deletes all records of transactions from an asset (this kills graphs)

}
