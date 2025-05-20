package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.tasks.gui.elements.specific.AssetCache;
import com.faridfaharaj.profitable.tasks.gui.guis.HoldingsMenu;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.hooks.VaultHook;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

import static com.faridfaharaj.profitable.data.holderClasses.Asset.retrieveCommodityEntity;
import static com.faridfaharaj.profitable.data.holderClasses.Asset.retrieveCommodityItem;

public class WalletCommand implements CommandExecutor {

    AssetCache[][] assetCache;
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

                new HoldingsMenu(player, null).openGui(player);

                return true;
            }


            if(args[0].equals("deposit")){

                Asset asset = Assets.getAssetData(args[1]);

                if(asset == null){
                    MessagingUtil.sendError(sender, "Invalid asset");
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

                depositAsset(asset, ammount, player);

                return true;
            }else if(args[0].equals("withdraw")){

                Asset asset = Assets.getAssetData(args[1]);

                if(asset == null){
                    MessagingUtil.sendError(sender, "Invalid asset");
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

                withdrawAsset(asset, ammount, player);

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

    public static void depositAsset(Asset asset, double ammount, Player player){

        if(asset.getAssetType() == 1){

            double fee = Configuration.parseFee(Configuration.DEPOSITFEES, ammount);
            if(fee > ammount){
                MessagingUtil.sendError(player, "Amount cannot be lower than " + fee);
                return;
            }

            if(VaultHook.isConnected() && Objects.equals(VaultHook.getAsset().getCode(), asset.getCode())){

                if(VaultHook.getEconomy().withdrawPlayer(player, ammount).transactionSuccess()){
                    Profitable.getfolialib().getScheduler().runAsync(task -> {
                        Asset.distributeAsset(Accounts.getAccount(player), asset, ammount-fee);
                        MessagingUtil.sendPaymentNotice(player, ammount, fee, asset);
                    });

                    return;
                }

            }

            if (PlayerPointsHook.isConnected() && Objects.equals(PlayerPointsHook.getAsset().getCode(), asset.getCode())){

                int integerAmount = (int) ammount;
                if(integerAmount < 1){
                    MessagingUtil.sendError(player, "Cannot Withdraw fractional Player Points");
                    return;
                }
                if(PlayerPointsHook.getApi().take(player.getUniqueId(), integerAmount)){
                    Profitable.getfolialib().getScheduler().runAsync(task -> {
                        Asset.distributeAsset(Accounts.getAccount(player), asset, integerAmount- Math.ceil(fee));
                        MessagingUtil.sendPaymentNotice(player, ammount, Math.ceil(fee), asset);
                    });
                    return;
                }

            }else {
                MessagingUtil.sendError(player, "This asset cannot be deposited");
                return;
            }


            MessagingUtil.sendError(player, "Insufficient funds to deposit this amount");

        }

        if(asset.getAssetType() == 2){


            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                if(retrieveCommodityItem(player, asset.getCode(), (int) ammount)){
                    Asset.sendBalance(Accounts.getAccount(player), asset.getCode(), ammount);
                    MessagingUtil.sendPaymentNotice(player, ammount, 0, asset);
                }else {
                    MessagingUtil.sendError(player, "Not enough " + asset.getCode().toLowerCase().replace("_", " "));
                }

            });

        }

        if(asset.getAssetType() == 3){

            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                if(retrieveCommodityEntity(player, asset.getCode(), Accounts.getEntityClaimId(Accounts.getAccount(player)), (int) ammount)){
                    Asset.sendBalance(Accounts.getAccount(player), asset.getCode(), ammount);
                    MessagingUtil.sendPaymentNotice(player, ammount, 0, asset);
                }else{
                    MessagingUtil.sendError(player,"Not enough claimed " + asset.getCode().toLowerCase().replace("_", " ") + "s around");
                }

            });

        }

    }

    public static void withdrawAsset(Asset asset, double ammount, Player player){

        if(asset.getAssetType() == 1){

            double fee = Configuration.parseFee(Configuration.WITHDRAWALFEES, ammount);
            if(fee > ammount){
                MessagingUtil.sendError(player, "Amount cannot be lower than its fee ($" + fee + ")");
                return;
            }

            if(VaultHook.isConnected() && asset.getCode().equals(VaultHook.getAsset().getCode())){

                Profitable.getfolialib().getScheduler().runAsync(async -> {
                    String account = Accounts.getAccount(player);
                    double balance = AccountHoldings.getAccountAssetBalance(account, asset.getCode());
                    if(Asset.retrieveBalance(account, balance, asset.getCode(), ammount)){
                        Profitable.getfolialib().getScheduler().runNextTick(global -> {
                            EconomyResponse es = VaultHook.getEconomy().depositPlayer(player, ammount);
                            if(es.transactionSuccess()){
                                MessagingUtil.sendChargeNotice(player, ammount-fee, fee, asset);
                            }else{
                                MessagingUtil.sendError(player, es.errorMessage);
                                Asset.distributeAsset(Accounts.getAccount(player), asset, ammount);
                            }
                        });
                    }else {
                        MessagingUtil.sendError(player, "Not enough " + asset.getCode());
                    }
                });
                return;

            }else if (PlayerPointsHook.isConnected() && asset.getCode().equals(PlayerPointsHook.getAsset().getCode())){

                int integerAmount = (int) ammount;
                if(integerAmount < 1){
                    MessagingUtil.sendError(player, "Cannot Withdraw fractional Player Points, not enough " + asset.getCode());
                    return;
                }
                Profitable.getfolialib().getScheduler().runAsync(async -> {
                    String account = Accounts.getAccount(player);
                    double balance = AccountHoldings.getAccountAssetBalance(account, asset.getCode());
                    if(Asset.retrieveBalance(account, balance, asset.getCode(), ammount)){
                        Profitable.getfolialib().getScheduler().runNextTick(global -> {
                            double ceilFee = Math.ceil(fee);
                            PlayerPointsHook.getApi().give(player.getUniqueId(), (int) (integerAmount-ceilFee));
                            MessagingUtil.sendChargeNotice(player, ammount-ceilFee, ceilFee, asset);
                        });
                    }else {
                        MessagingUtil.sendError(player, "Not enough " + asset.getCode());
                    }
                });
                return;
            }else {
                MessagingUtil.sendError(player, "This asset cannot be withdrawn");
                return;
            }

        }

        if(asset.getAssetType() == 3){

            String account = Accounts.getAccount(player);
            double balance = AccountHoldings.getAccountAssetBalance(account, asset.getCode());
            if(Asset.retrieveBalance(account, balance, asset.getCode(), ammount)){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Asset.sendCommodityEntityToPlayer(player, account, asset.getCode(), (int) ammount);
                    MessagingUtil.sendPaymentNotice(player, ammount, 0, asset);
                });
            }else {
                MessagingUtil.sendError(player, "Not enough " + asset.getCode() + " on your account");
            }
            return;

        }

        if(asset.getAssetType() == 2){

            String account = Accounts.getAccount(player);
            double balance = AccountHoldings.getAccountAssetBalance(account, asset.getCode());
            if(Asset.retrieveBalance(account, balance, asset.getCode(), ammount)){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Asset.sendItemToPlayer(player, asset.getCode(), (int) ammount);
                    MessagingUtil.sendPaymentNotice(player, ammount, 0, asset);
                });
                return;
            }else {
                MessagingUtil.sendError(player, "Not enough " + asset.getCode() + " on your account");
            }

        }

    }

}
