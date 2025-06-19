package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.util.MessagingUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class AccountCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(sender instanceof Player player){

            if(args.length == 0){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    String account = Accounts.getAccount(player);

                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("account.display",
                            Map.entry("%account%", Objects.equals(account, player.getUniqueId().toString())? "Default" : account)
                    ));

                });
                return true;
            }

            //BASIC ACCOUNT COMMANDS
            if(args[0].equals("register")){

                if(!sender.hasPermission("profitable.account.manage.register")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 4){
                    MessagingUtil.sendSyntaxError(sender, "/account register <Username> <Password> <Repeat Password>");
                    return true;
                }

                if(Objects.equals(args[2], args[3])){
                    if(args[2].length() < 32){
                        Profitable.getfolialib().getScheduler().runAsync(task -> {
                            if(Accounts.registerAccount(player.getWorld(), args[1], args[2])){

                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.registry",
                                        Map.entry("%account%", args[1])
                                ));

                            }else{
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.account-already-exists"));
                            }
                        });
                        return true;
                    }else{
                        MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.password-too-long"));
                    }
                }else {
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.password-mismatch"));
                    return true;
                }

            }

            if(args[0].equals("delete")){

                if(!sender.hasPermission("profitable.account.manage.delete")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 3){
                    MessagingUtil.sendSyntaxError(sender, "/account delete <Account> <password>");
                    return true;
                }

                String account = args[1];
                UUID playerid = player.getUniqueId();

                if(Objects.equals(playerid.toString(), args[2])){
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.cant-delete-default"));
                }

                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    if(Objects.equals(account, Accounts.getAccount(player))){
                        if(!Accounts.comparePasswords(player.getWorld(), account, args[2])){
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.wrong-password"));
                            return;
                        }

                        Accounts.logOut(playerid);
                        if(Accounts.getCurrentAccounts().containsValue(account)) {
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.cant-delete-active-account"));
                        }else{
                            Accounts.deleteAccount(player.getWorld(), account);
                            MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("account.delete",
                                    Map.entry("%account%", account)
                            ));
                        }

                    }else{
                        MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.active-account-mismatch"));
                    }
                });

                return true;

            }

            if(args[0].equals("login")){

                if(!sender.hasPermission("profitable.account.manage.login")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 3){
                    MessagingUtil.sendSyntaxError(sender, "/account login <Account> <Password>");
                    return true;
                }

                Profitable.getfolialib().getScheduler().runAsync(task -> {

                    if(Accounts.logIn(player, args[1], args[2])){
                        MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("account.login",
                                Map.entry("%account%", args[1])
                        ));

                    }else{
                        MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.wrong-password"));
                    }

                });

            }

            if(args[0].equals("logout")){

                if(!sender.hasPermission("profitable.account.manage.logout")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                UUID playerid = player.getUniqueId();

                Accounts.logOut(playerid);

                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.logout"));

                return  true;

            }

            if(args[0].equals("password")){

                if(!sender.hasPermission("profitable.account.manage.password")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 3){
                    MessagingUtil.sendSyntaxError(sender,"/account password <Old password> <New password>");
                    return true;
                }

                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    String account = Accounts.getAccount(player);
                    if(Accounts.comparePasswords(player.getWorld(), account, args[1])){
                        if(args[2].length() < 32){
                            Accounts.changePassword(player.getWorld(), account, args[2]);
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.password-update"));


                        }else{
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.password-too-long"));
                        }
                    }else{
                        MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.wrong-password"));
                    }
                });
                return true;
            }

        }





        MessagingUtil.sendGenericInvalidSubCom(sender, args[0]);
        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){
                List<String> options = new ArrayList<>(List.of("register", "login", "logout", "password", "delete"));

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
            }

            return suggestions;
        }

    }
}
