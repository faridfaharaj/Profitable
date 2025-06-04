package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(sender instanceof Player player){

            if(!sender.hasPermission("profitable.account.info.wallet")){
                MessagingUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length == 0){

                new HoldingsMenu(player, null).openGui(player);

                return true;
            }


            if(args[0].equals("deposit")){

                String assetid;
                if(args[1].equals("hand")){
                    assetid = player.getInventory().getItemInMainHand().getType().name();
                }else {
                    assetid = args[1].toUpperCase();
                }

                Asset asset = Assets.getAssetData(player.getWorld(), assetid);

                if(asset == null){
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("assets.error.asset-not-found",
                        Map.entry("%asset%", assetid)
                    ));
                    return true;
                }

                double ammount;
                if(args.length != 2){
                    try{
                        ammount = Double.parseDouble(args[2]);
                        if(asset.getAssetType() == 2 || asset.getAssetType() == 3){
                            ammount = (int) ammount;
                        }
                        if(ammount <= 0){
                            MessagingUtil.sendGenericInvalidAmount(sender, args[2]);
                            return true;
                        }
                    }catch (Exception e){
                        MessagingUtil.sendGenericInvalidAmount(sender, args[2]);
                        return true;
                    }
                }else {
                    if(args[1].equals("hand")){
                        ammount = player.getInventory().getItemInMainHand().getAmount();
                    }else {
                        ammount = 1;
                    }
                }

                depositAsset(asset, ammount, player);

                return true;
            }else if(args[0].equals("withdraw")){

                String assetid;
                if(args[1].equals("hand")){
                    assetid = player.getInventory().getItemInMainHand().getType().name();
                }else {
                    assetid = args[1].toUpperCase();
                }

                Asset asset = Assets.getAssetData(player.getWorld(), assetid);

                if(asset == null){
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("assets.error.asset-not-found",
                            Map.entry("%asset%", assetid)
                    ));
                    return true;
                }

                double ammount;
                if(args.length != 2){
                    try{
                        ammount = Double.parseDouble(args[2]);
                        if(asset.getAssetType() == 2 || asset.getAssetType() == 3){
                            ammount = (int) ammount;
                        }
                        if(ammount <= 0){
                            MessagingUtil.sendGenericInvalidAmount(sender, args[2]);
                            return true;
                        }
                    }catch (Exception e){
                        MessagingUtil.sendGenericInvalidAmount(sender, args[2]);
                        return true;
                    }
                }else {
                    ammount = 1;
                }

                withdrawAsset(asset, ammount, player);

            }

            return true;
        }else{
            MessagingUtil.sendGenericCantConsole(sender);
        }




        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){
                suggestions = List.of("deposit", "withdraw");
            }else{

                if(Objects.equals(args[0], "deposit")){

                    if(args.length == 2){
                        suggestions.add("[<Asset>]");
                        suggestions.add("hand");
                    }

                    if(args.length == 3){
                        suggestions = List.of("[<Amount>]");
                    }

                }

                if(Objects.equals(args[0], "withdraw")){

                    if(args.length == 2){
                        suggestions.add("[<Asset>]");
                    }

                    if(args.length == 3){
                        suggestions = List.of("[<Amount>]");
                    }

                }

            }

            return suggestions;

        }

    }

    public static void depositAsset(Asset asset, double amount, Player player){

        if(asset.getAssetType() == 1){

            double fee = Configuration.parseFee(Configuration.DEPOSITFEES, amount);
            if(fee > amount){
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.minimum-deposit",
                    Map.entry("%asset_amount%", MessagingUtil.assetAmmount(asset, amount))
                ));

                return;
            }

            if(VaultHook.isConnected() && Objects.equals(VaultHook.getAsset().getCode(), asset.getCode())){

                if(VaultHook.getEconomy().withdrawPlayer(player, amount).transactionSuccess()){
                    Profitable.getfolialib().getScheduler().runAsync(task -> {
                        Asset.distributeAsset(player.getWorld(), Accounts.getAccount(player), asset, amount -fee);
                        MessagingUtil.sendPaymentNotice(player, amount, fee, asset);
                    });

                    return;
                }

            }

            if (PlayerPointsHook.isConnected() && Objects.equals(PlayerPointsHook.getAsset().getCode(), asset.getCode())){

                int integerAmount = (int) amount;
                if(integerAmount < 1){
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.cant-fractional",
                        Map.entry("%asset%", asset.getCode())
                    ));
                    return;
                }
                if(PlayerPointsHook.getApi().take(player.getUniqueId(), integerAmount)){
                    Profitable.getfolialib().getScheduler().runAsync(task -> {
                        Asset.distributeAsset(player.getWorld() ,Accounts.getAccount(player), asset, integerAmount- Math.ceil(fee));
                        MessagingUtil.sendPaymentNotice(player, amount, Math.ceil(fee), asset);
                    });
                    return;
                }

            }else {
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.cant-fractional",
                        Map.entry("%asset%", asset.getCode())
                ));
                return;
            }



            MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("hooks.error.insufficient-funds"));

        }

        if(asset.getAssetType() == 2){


            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                if(retrieveCommodityItem(player, asset.getCode(), (int) amount)){
                    Asset.sendBalance(player.getWorld() ,Accounts.getAccount(player), asset.getCode(), amount);
                    MessagingUtil.sendPaymentNotice(player, amount, 0, asset);
                }else {
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                            Map.entry("%asset%", asset.getCode())
                    ));
                }

            });

        }

        if(asset.getAssetType() == 3){

            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                if(retrieveCommodityEntity(player, asset.getCode(), Accounts.getEntityClaimId(player.getWorld(), Accounts.getAccount(player)), (int) amount)){
                    Asset.sendBalance(player.getWorld() ,Accounts.getAccount(player), asset.getCode(), amount);
                    MessagingUtil.sendPaymentNotice(player, amount, 0, asset);
                }else{
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                            Map.entry("%asset%", asset.getCode())
                    ));
                }

            });

        }

    }

    public static void withdrawAsset(Asset asset, double ammount, Player player){

        if(asset.getAssetType() == 1){

            double fee = Configuration.parseFee(Configuration.WITHDRAWALFEES, ammount);

            if(VaultHook.isConnected() && asset.getCode().equals(VaultHook.getAsset().getCode())){

                Profitable.getfolialib().getScheduler().runAsync(async -> {
                    String account = Accounts.getAccount(player);
                    double balance = AccountHoldings.getAccountAssetBalance(player.getWorld() ,account, asset.getCode());
                    if(Asset.retrieveBalance(player.getWorld() ,account, balance, asset.getCode(), ammount)){
                        Profitable.getfolialib().getScheduler().runNextTick(global -> {
                            EconomyResponse es = VaultHook.getEconomy().depositPlayer(player, ammount);
                            if(es.transactionSuccess()){
                                MessagingUtil.sendChargeNotice(player, ammount+fee, fee, asset);
                            }else{
                                MessagingUtil.sendSyntaxError(player, es.errorMessage);
                                Asset.distributeAsset(player.getWorld() ,Accounts.getAccount(player), asset, ammount);
                            }
                        });
                    }else {
                        MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                                Map.entry("%asset%", asset.getCode())
                        ));
                    }
                });
                return;

            }else if (PlayerPointsHook.isConnected() && asset.getCode().equals(PlayerPointsHook.getAsset().getCode())){

                int integerAmount = (int) ammount;
                if(integerAmount < 1){
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.cant-fractional",
                            Map.entry("%asset%", asset.getCode())
                    ));
                    return;
                }
                Profitable.getfolialib().getScheduler().runAsync(async -> {
                    String account = Accounts.getAccount(player);
                    double balance = AccountHoldings.getAccountAssetBalance(player.getWorld() ,account, asset.getCode());
                    if(Asset.retrieveBalance(player.getWorld() ,account, balance, asset.getCode(), ammount)){
                        Profitable.getfolialib().getScheduler().runNextTick(global -> {
                            double ceilFee = Math.ceil(fee);
                            PlayerPointsHook.getApi().give(player.getUniqueId(), (int) (integerAmount-ceilFee));
                            MessagingUtil.sendChargeNotice(player, ammount-ceilFee, ceilFee, asset);
                        });
                    }else {
                        MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                                Map.entry("%asset%", asset.getCode())
                        ));
                    }
                });
                return;
            }else {
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-depositable",
                        Map.entry("%asset%", asset.getCode())
                ));
                return;
            }

        }

        if(asset.getAssetType() == 3){

            String account = Accounts.getAccount(player);
            double balance = AccountHoldings.getAccountAssetBalance(player.getWorld() ,account, asset.getCode());
            if(Asset.retrieveBalance(player.getWorld() ,account, balance, asset.getCode(), ammount)){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Asset.sendCommodityEntityToPlayer(player, account, asset.getCode(), (int) ammount);
                    MessagingUtil.sendPaymentNotice(player, ammount, 0, asset);
                });
            }else {
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                        Map.entry("%asset%", asset.getCode())
                ));
            }
            return;

        }

        if(asset.getAssetType() == 2){

            String account = Accounts.getAccount(player);
            double balance = AccountHoldings.getAccountAssetBalance(player.getWorld() ,account, asset.getCode());
            if(Asset.retrieveBalance(player.getWorld() ,account, balance, asset.getCode(), ammount)){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Asset.sendItemToPlayer(player, asset.getCode(), (int) ammount);
                    MessagingUtil.sendPaymentNotice(player, ammount, 0, asset);
                });
                return;
            }else {
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                        Map.entry("%asset%", asset.getCode())
                ));
            }

        }

    }

}
