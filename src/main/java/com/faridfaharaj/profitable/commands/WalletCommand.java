package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.hooks.VaultHook;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
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
            MessagingUtil.sendGenericMissingPerm(sender);
            return true;
        }

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }

        if(sender instanceof Player player){



            if(args.length == 0){

                String account = Accounts.getAccount(player);
                MessagingUtil.sendCustomMessage(sender,
                        MessagingUtil.profitableTopSeparator("Wallet","------------------").appendNewline()
                                .append(AccountHoldings.AssetBalancesToString(account)).appendNewline()
                                .append(MessagingUtil.profitableBottomSeparator())
                );

                return true;
            }

            if(Configuration.HOOKED){
                if(args[0].equals("deposit")){

                    if(!sender.hasPermission("profitable.account.funds.deposit")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length < 3){
                        MessagingUtil.sendError(player, "/wallet deposit <Currency> <amount>");
                        return true;
                    }

                    double ammount;
                    try{
                        ammount = Double.parseDouble(args[2]);
                        if(ammount <= 0){
                            MessagingUtil.sendError(sender, "Invalid amount");
                            return true;
                        }
                    }catch (Exception e){
                        MessagingUtil.sendError(sender, "Invalid amount");
                        return true;
                    }
                    double fee = Configuration.parseFee(Configuration.DEPOSITFEES, ammount);
                    if(fee > ammount){
                        MessagingUtil.sendError(sender, "Amount cannot be lower than " + fee);
                        return true;
                    }

                    if(VaultHook.isConnected() && Objects.equals(VaultHook.getAsset().getCode(), args[1])){

                        Asset asset = VaultHook.getAsset();
                        if(VaultHook.getEconomy().withdrawPlayer(player, ammount).transactionSuccess()){
                            Profitable.getfolialib().getScheduler().runAsync(task -> {
                                Asset.distributeAsset(Accounts.getAccount(player), asset, ammount-fee);
                                MessagingUtil.sendPaymentNotice(sender, ammount, fee, asset);
                            });

                            return true;
                        }

                    }

                    if (PlayerPointsHook.isConnected() && Objects.equals(PlayerPointsHook.getAsset().getCode(), args[1])){
                        Asset asset = PlayerPointsHook.getAsset();
                        int integerAmount = (int) ammount;
                        if(integerAmount < 1){
                            MessagingUtil.sendError(sender, "Cannot Withdraw fractional Player Points");
                            return true;
                        }
                        if(PlayerPointsHook.getApi().take(player.getUniqueId(), integerAmount)){
                            Profitable.getfolialib().getScheduler().runAsync(task -> {
                                Asset.distributeAsset(Accounts.getAccount(player), asset, integerAmount- Math.ceil(fee));
                                MessagingUtil.sendPaymentNotice(sender, ammount, Math.ceil(fee), asset);
                            });
                            return true;
                        }

                    }


                    MessagingUtil.sendError(player, "Insufficient funds to deposit this amount");
                    return true;
                }else if(args[0].equals("withdraw")){

                    if(!sender.hasPermission("profitable.account.funds.withdraw")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length < 3){
                        MessagingUtil.sendError(player, "/wallet withdraw <Currency> <amount>");
                        return true;
                    }

                    double ammount;
                    try{
                        ammount = Double.parseDouble(args[2]);
                        if(ammount <= 0){
                            MessagingUtil.sendError(sender, "Invalid amount");
                            return true;
                        }
                    }catch (Exception e){
                        MessagingUtil.sendError(sender, "Invalid amount");
                        return true;
                    }
                    double fee = Configuration.parseFee(Configuration.WITHDRAWALFEES, ammount);
                    if(fee > ammount){
                        MessagingUtil.sendError(sender, "Amount cannot be lower than its fee ($" + fee + ")");
                        return true;
                    }

                    if(args[1].equals(VaultHook.getAsset().getCode()) && VaultHook.isConnected()){
                        Asset asset = VaultHook.getAsset();

                        Profitable.getfolialib().getScheduler().runAsync(async -> {
                            if(Asset.retrieveBalance(player, "Couldn't withdraw amount to Vault", asset.getCode(), ammount, false)){
                                Profitable.getfolialib().getScheduler().runNextTick(global -> {
                                    EconomyResponse es = VaultHook.getEconomy().depositPlayer(player, ammount);
                                    if(es.transactionSuccess()){
                                        MessagingUtil.sendChargeNotice(sender, ammount-fee, fee, asset);
                                    }else{
                                        MessagingUtil.sendError(sender, es.errorMessage);
                                        Asset.distributeAsset(Accounts.getAccount(player), asset, ammount);
                                    }
                                });
                            }
                        });

                    }else if (args[1].equals(PlayerPointsHook.getAsset().getCode()) && PlayerPointsHook.isConnected()){
                        Asset asset = PlayerPointsHook.getAsset();
                        int integerAmount = (int) ammount;
                        if(integerAmount < 1){
                            MessagingUtil.sendError(sender, "Cannot Withdraw fractional Player Points");
                            return true;
                        }
                        Profitable.getfolialib().getScheduler().runAsync(async -> {
                            if(Asset.retrieveBalance(player, "Couldn't withdraw amount to PlayerPoints", asset.getCode(), ammount, false)){
                                Profitable.getfolialib().getScheduler().runNextTick(global -> {
                                    double ceilFee = Math.ceil(fee);
                                    PlayerPointsHook.getApi().give(player.getUniqueId(), (int) (integerAmount-ceilFee));
                                    MessagingUtil.sendChargeNotice(sender, ammount-ceilFee, ceilFee, asset);
                                });
                            }
                        });
                    }else{
                        MessagingUtil.sendError(sender, "Invalid Currency");
                        return true;
                    }

                    return true;
                }else{
                    MessagingUtil.sendError(sender, "Invalid Subcommand");
                }
            }

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

            if(args.length == 1 && Configuration.HOOKED){
                suggestions = List.of("deposit", "withdraw");
            }else{

                if(Objects.equals(args[0], "deposit")){

                    if(args.length == 2){
                        Set<String> currencies = new HashSet<>();
                        if(VaultHook.isConnected()){
                            currencies.add(VaultHook.getAsset().getCode());
                        }
                        if(PlayerPointsHook.isConnected()){
                            currencies.add(PlayerPointsHook.getAsset().getCode());
                        }
                        suggestions.addAll(currencies);
                    }

                    if(args.length == 3){
                        suggestions = List.of("[<Amount>]");
                    }

                }

                if(Objects.equals(args[0], "withdraw")){

                    if(args.length == 2){
                        Set<String> currencies = new HashSet<>();
                        if(VaultHook.isConnected()){
                            currencies.add(VaultHook.getAsset().getCode());
                        }
                        if(PlayerPointsHook.isConnected()){
                            currencies.add(PlayerPointsHook.getAsset().getCode());
                        }
                        suggestions.addAll(currencies);
                    }

                    if(args.length == 3){
                        suggestions = List.of("[<Amount>]");
                    }

                }

            }

            return suggestions;

        }

    }

}
