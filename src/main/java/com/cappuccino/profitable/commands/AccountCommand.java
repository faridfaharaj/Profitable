package com.cappuccino.profitable.commands;

import com.cappuccino.profitable.Configuration;
import com.cappuccino.profitable.data.tables.AccountHoldings;
import com.cappuccino.profitable.data.tables.Accounts;
import com.cappuccino.profitable.data.tables.Orders;
import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.data.holderClasses.Order;
import com.cappuccino.profitable.tasks.TemporalItems;
import com.cappuccino.profitable.util.NamingUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AccountCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(args.length == 0){
            if(sender instanceof  Player player){
                String account = Accounts.getAccount(player);
                player.sendMessage(NamingUtil.profitablePrefix() + "Account: " + (Objects.equals(account, player.getUniqueId().toString()) ? "(Default)":account));
                return true;
            }
            return false;
        }

        //BASIC ACCOUNT COMMANDS
        if(args[0].equals("register")){

            if(!sender.hasPermission("profitable.account.manage.register")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(args.length < 4){
                sender.sendMessage( ChatColor.RED + "/account register <Username> <Password> <Repeat Password>");
                return true;
            }

            if(Objects.equals(args[2], args[3])){
                if(Accounts.registerAccount(args[1], args[2])){
                    sender.sendMessage(NamingUtil.profitablePrefix()+"Account " + args[1] + " registered successfully");
                    return true;
                }else{
                    sender.sendMessage(ChatColor.RED + "Account already exists");
                    return true;
                }
            }else {
                sender.sendMessage(ChatColor.RED+ "Passwords don't match");
                return true;
            }

        }

        if(args[0].equals("delete")){

            if(!sender.hasPermission("profitable.account.manage.delete")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(args.length < 3){
                sender.sendMessage( ChatColor.RED + "/account delete <Account> <password>");
                return true;
            }

            if(sender instanceof Player player){
                String account = args[1];
                UUID playerid = player.getUniqueId();

                if(Objects.equals(playerid.toString(), args[2])){
                    sender.sendMessage("Cannot delete default account");
                }

                if(Objects.equals(account, Accounts.getAccount(player))){

                    if(!Accounts.comparePasswords(account, args[2])){
                        sender.sendMessage(ChatColor.RED + "");
                        return true;
                    }

                    sender.sendMessage(NamingUtil.profitablePrefix()+"Logging out...");
                    Accounts.logOut(playerid);

                    if(Accounts.getCurrentAccounts().containsValue(account)) {
                        sender.sendMessage(ChatColor.RED + "Someone else is still using this account, cannot delete");
                    }else{
                        Accounts.deleteAccount(account);
                        sender.sendMessage(NamingUtil.profitablePrefix()+"DELETED account: " + account);
                    }
                    return true;

                }else{
                    sender.sendMessage(ChatColor.RED + "Account name doesn't match with active account's");
                }

                return true;

            }else{
                sender.sendMessage("cannot run this from console");
                return true;
            }

        }

        if(args[0].equals("login")){

            if(!sender.hasPermission("profitable.account.manage.login")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(args.length < 3){
                sender.sendMessage( ChatColor.RED + "/account login <Account> <Password>");
                return true;
            }

            if(sender instanceof Player player){

                if(Objects.equals(args[1], Accounts.getCurrentAccounts().get(player.getUniqueId()))){
                    player.sendMessage(ChatColor.RED + "Already logged in");
                    return true;
                }

                if(Accounts.logIn(player.getUniqueId(), args[1], args[2])){
                    player.sendMessage(NamingUtil.profitablePrefix()+"successfully logged into " + args[1]);
                }else{
                    player.sendMessage(ChatColor.RED + "Incorrect account or password");
                }
                return true;
            }else{
                sender.sendMessage("Cannot use this command from console");
            }

        }

        if(args[0].equals("logout")){

            if(!sender.hasPermission("profitable.account.manage.logout")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(sender instanceof Player player){
                UUID playerid = player.getUniqueId();
                if(!Accounts.getCurrentAccounts().containsKey(playerid)){
                    player.sendMessage(ChatColor.RED+"No active account found");
                    return true;
                }

                Accounts.logOut(playerid);

                player.sendMessage(NamingUtil.profitablePrefix()+"Logged out successfully");
                return  true;

            }

        }

        if(args[0].equals("password")){

            if(!sender.hasPermission("profitable.account.manage.password")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(args.length < 3){
                sender.sendMessage( ChatColor.RED + "/account password <Old password> <New password>");
                return true;
            }

            if(sender instanceof Player player){
                String account = Accounts.getAccount(player);
                if(Accounts.comparePasswords(account, args[1])){
                    Accounts.changePassword(account, args[2]);
                    player.sendMessage(NamingUtil.profitablePrefix()+"Password updated successfully");
                }else{
                    player.sendMessage(ChatColor.RED + "Incorrect password");
                }
            }else{
                sender.sendMessage(ChatColor.RED + "Cannot use this command from console");
            }
            return true;
        }

        //DATA
        if(args[0].equals("claim")){

            if(!sender.hasPermission("profitable.account.claim")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(sender instanceof Player player){

                TemporalItems.sendClaimingTag(player);

                return true;

            }

        }

        if(args[0].equals("delivery")){

            if(sender instanceof Player player){

                String account = Accounts.getAccount(player);

                if(args.length < 2){

                    if(!sender.hasPermission("profitable.account.info.delivery")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    Location entityDelivery = Accounts.getEntityDelivery(account);
                    Location itemDelivery = Accounts.getItemDelivery(account);

                    player.sendMessage(NamingUtil.profitableTopSeparator());
                    player.sendMessage("Item Delivery Location:");
                    player.sendMessage(ChatColor.YELLOW+(itemDelivery == null?"Not set":itemDelivery.toVector() + " (" + itemDelivery.getWorld().getName()+")"));
                    player.sendMessage("");
                    player.sendMessage("Entity Delivery Location:");
                    player.sendMessage(ChatColor.YELLOW+(entityDelivery == null?"Not set":entityDelivery.toVector() + " (" + entityDelivery.getWorld().getName()+")"));
                    player.sendMessage(NamingUtil.profitableBottomSeparator());

                    return true;
                }

                if(args[1].equals("set")){

                    if(!sender.hasPermission("profitable.account.manage.setdelivery")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    if(args.length < 3){
                        sender.sendMessage( ChatColor.RED + "/account delivery set item OR /account delivery set entity");
                        return true;
                    }

                    if(args[2].equals("item")){

                        TemporalItems.sendDeliveryStick(player, true);

                        return true;

                    } else if (args[2].equals("entity")) {
                        TemporalItems.sendDeliveryStick(player, false);

                        return true;
                    }else{
                        sender.sendMessage( ChatColor.RED + "/account delivery set item OR /account delivery set entity");
                    }

                    return true;


                }else{
                    sender.sendMessage( ChatColor.RED + "/account delivery set item OR /account delivery set entity");
                }

            }
        }

        if(args[0].equals("wallet")){

            if(!sender.hasPermission("profitable.account.info.wallet")){
                sender.sendMessage(NamingUtil.genericMissingPerm());
                return true;
            }

            if(sender instanceof Player player){

                List<String> balances = AccountHoldings.AssetBalancesToString(Accounts.getAccount(player), 1);
                player.sendMessage(NamingUtil.profitableTopSeparator());
                player.sendMessage("Exchange Wallet:");
                if (!balances.isEmpty()) {
                    for (String balance : balances) {
                        player.sendMessage(balance);
                    }
                } else {
                    player.sendMessage(ChatColor.GRAY + "Empty");
                }
                player.sendMessage(NamingUtil.profitableBottomSeparator());

            }
            return true;
        }

        if(args[0].equals("orders")){
            if(sender instanceof Player player){

                if(args.length > 1 && args[1].equals("cancel")){

                    if(!sender.hasPermission("profitable.account.manage.orders.cancel")){
                        sender.sendMessage(NamingUtil.genericMissingPerm());
                        return true;
                    }

                    if(args.length < 3){

                        player.sendMessage(ChatColor.RED + "/account orders cancel <Order ID>");
                        return true;
                    }

                    if(!Orders.cancelOrder(args[2], player)){
                        player.sendMessage(ChatColor.RED + "Couldn't cancel that order");
                    }
                    return true;
                }

                if(!sender.hasPermission("profitable.account.info.orders")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                int page;
                try {
                    page = args.length == 2? Integer.parseInt(args[1]) : 0;
                }catch (Exception e){
                    player.sendMessage(ChatColor.RED + "Invalid page number");
                    return true;
                }

                List<Order> orders = Orders.getAccountOrders(Accounts.getAccount(player));
                if(orders.isEmpty()){
                    player.sendMessage(NamingUtil.profitablePrefix()+ChatColor.GRAY + "No active orders on this account");
                }else {
                    player.sendMessage(NamingUtil.profitableTopSeparator());
                    for(int i = page*2; i<Math.min(page*2+2,orders.size()); i++){
                        player.sendMessage(orders.get(i).toString());
                        Order.sendCancelButton(player, orders.get(i).getUuid());
                    }

                    int totalPages = (orders.size()-1)/2;

                    if(totalPages > page){
                        TextComponent nextPageComponent = new TextComponent(ChatColor.AQUA + "------------ "+ ChatColor.GOLD +"<Page " + page + "/" + totalPages  + ">" + " [ next ] "+ChatColor.AQUA+"------------");
                        nextPageComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                                "/account orders " + (page+1)));

                        player.spigot().sendMessage(nextPageComponent);
                    }else{
                        player.sendMessage(ChatColor.AQUA + "---------------- "+ ChatColor.GOLD +"<Page " + page + "/" + totalPages + ">"+ ChatColor.AQUA +" ----------------");
                    }
                }
            }
            return true;
        }

        if(Configuration.VAULTENABLED){
            if(args[0].equals("deposit")){

                if(!sender.hasPermission("profitable.account.funds.deposit")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                if(args.length < 2){
                    sender.sendMessage( ChatColor.RED + "/account withdraw <amount>");
                    return true;
                }

                if(sender instanceof Player player){
                    double ammount;
                    try{
                        ammount = Double.parseDouble(args[1]);
                    }catch (Exception e){
                        player.sendMessage(ChatColor.RED + "Invalid amount");
                        return true;
                    }

                    if(Configuration.ECONOMY.withdrawPlayer(player, ammount).transactionSuccess()){
                        Asset.distributeAsset(Accounts.getAccount(player), "VLT", 1, ammount);
                        player.sendMessage(NamingUtil.profitablePrefix()+"Added " + ChatColor.GOLD + ammount + " VLT" + ChatColor.RESET + " to your wallet");
                    }else{
                        player.sendMessage(ChatColor.RED +"Cannot add this amount to your wallet");
                    }
                }
                return true;
            }

            if(args[0].equals("withdraw")){

                if(!sender.hasPermission("profitable.account.funds.withdraw")){
                    sender.sendMessage(NamingUtil.genericMissingPerm());
                    return true;
                }

                if(args.length < 2){
                    sender.sendMessage( ChatColor.RED + "/account withdraw <amount>");
                    return true;
                }

                if(sender instanceof Player player){

                    double ammount;
                    try{
                        ammount = Double.parseDouble(args[1]);
                    }catch (Exception e){
                        player.sendMessage(ChatColor.RED + "Invalid amount");
                        return true;
                    }

                    if(Asset.retrieveAsset(player, "Withdraw amount to Vault", "VLT", 1, ammount) && Configuration.ECONOMY.depositPlayer(player, ammount).transactionSuccess()){
                        player.sendMessage(NamingUtil.profitablePrefix()+"Taken " + ChatColor.GOLD + ammount + " VLT" + ChatColor.RESET + " from your wallet");
                    }
                }
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED+"Invalid Subcommand");
        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){
                List<String> options = new ArrayList<>(List.of("register", "login", "logout", "password", "wallet", "claim", "delivery", "orders", "delete"));

                if(Configuration.VAULTENABLED) options.addAll(List.of("deposit", "withdraw"));

                StringUtil.copyPartialMatches(args[0], options, suggestions);
            }

            if(args.length >= 2){

                if(Objects.equals(args[0], "register")){
                    if(args.length == 2){
                        suggestions = List.of("[<Account>]");
                    }else if(args.length == 3){
                        suggestions = List.of("[<Password>]");
                    }else if(args.length == 4){
                        suggestions = List.of("[<Repeat password>]");
                    }
                }

                if(Objects.equals(args[0], "delete")){
                    if(args.length == 2){
                        suggestions = List.of("[<Account>]");
                    }else if(args.length == 3){
                        suggestions = List.of("[<Password>]");
                    }
                }

                if(Objects.equals(args[0], "login")){
                    if(args.length == 2){
                        suggestions = List.of("[<Account>]");
                    }else if(args.length == 3){
                        suggestions = List.of("[<Password>]");
                    }
                }

                if(Objects.equals(args[0], "password")){
                    if(args.length == 2){
                        suggestions = List.of("[<Old password>]");
                    }else{
                        suggestions = List.of("[<New password>]");
                    }
                }

                if(Objects.equals(args[0], "delivery")){

                    if(args.length == 2){
                        suggestions = List.of("set");
                    }

                    if(args.length == 3 && Objects.equals(args[1], "set")){
                        suggestions = List.of("item", "entity");
                    }

                }

                if(Objects.equals(args[0], "orders")){

                    if(args.length == 2){
                        suggestions = List.of("[<Page>]","cancel");
                    }

                    if(args.length == 3){
                        suggestions = List.of("[<Order ID>]");
                    }

                }

                if(Configuration.VAULTENABLED){
                    if(Objects.equals(args[0], "deposit")){

                        if(args.length == 2){
                            suggestions = List.of("[<Amount>]");
                        }

                    }

                    if(Objects.equals(args[0], "withdraw")){

                        if(args.length == 2){
                            suggestions = List.of("[<Amount>]");
                        }

                    }
                }
            }

            return suggestions;
        }

    }
}
