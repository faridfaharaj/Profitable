package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.util.TextUtil;
import com.faridfaharaj.profitable.util.VaultCompat;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class WalletCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!sender.hasPermission("profitable.account.info.wallet")){
            TextUtil.sendGenericMissingPerm(sender);
            return true;
        }


        if(sender instanceof Player player){



            if(args.length == 0){

                String account = Accounts.getAccount(player);
                TextUtil.sendCustomMessage(sender,
                        TextUtil.profitableTopSeparator().appendNewline()
                                .append(AccountHoldings.AssetBalancesToString(account, 1)).appendNewline()
                                .append(TextUtil.profitableBottomSeparator())
                );

                return true;
            }

            if(Configuration.VAULTENABLED){
                if(args[0].equals("deposit")){

                    if(!sender.hasPermission("profitable.account.funds.deposit")){
                        TextUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length < 2){
                        TextUtil.sendError(player, "/wallet deposit <amount>");
                        return true;
                    }

                    double ammount;
                    try{
                        ammount = Double.parseDouble(args[1]);
                    }catch (Exception e){
                        TextUtil.sendError(sender, "Invalid amount");
                        return true;
                    }

                    if(Configuration.ECONOMY.withdrawPlayer(player, ammount).transactionSuccess()){
                        Asset.distributeAsset(Accounts.getAccount(player), "VLT", 1, ammount);
                        TextUtil.sendCustomMessage(sender, TextUtil.profitablePrefix().append(Component.text("Added ")).append(Component.text(ammount + " VLT").color(VaultCompat.getVaultColor())).append(Component.text(" to your wallet")));
                    }else{
                        TextUtil.sendError(player, "Not enough funds");
                    }
                    return true;
                }

                if(args[0].equals("withdraw")){

                    if(!sender.hasPermission("profitable.account.funds.withdraw")){
                        TextUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length < 2){
                        TextUtil.sendError(player, "/wallet withdraw <amount>");
                        return true;
                    }

                    double ammount;
                    try{
                        ammount = Double.parseDouble(args[1]);
                    }catch (Exception e){
                        TextUtil.sendError(sender, "Invalid amount");
                        return true;
                    }

                    if(Asset.retrieveAsset(player, "Withdraw amount to Vault", "VLT", 1, ammount) && Configuration.ECONOMY.depositPlayer(player, ammount).transactionSuccess()){
                        TextUtil.sendCustomMessage(sender, TextUtil.profitablePrefix().append(Component.text("Taken ")).append(Component.text(ammount + " VLT").color(VaultCompat.getVaultColor())).append(Component.text(" from your wallet")));
                    }
                    return true;
                }
            }

            return true;
        }else{
            TextUtil.sendGenericCantConsole(sender);
        }




        return false;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(Configuration.VAULTENABLED){

                if(args.length == 1){
                    suggestions = List.of("deposit", "withdraw");
                }else{

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
