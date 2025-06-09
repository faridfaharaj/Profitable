package com.faridfaharaj.profitable.data.holderClasses.assets;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Currency extends Asset {

    public Currency(String code, TextColor color, String name, ItemStack stack) {
        super(code, AssetType.CURRENCY, color, name, stack);
    }

    @Override
    public void distributeAsset(String account, double ammount){

        sendBalance(account, ammount);

    }

    @Override
    public void chargeAndRun(Player player, double ammount, Runnable runnable) {

        if(ammount == 0){
            runnable.run();
            return;
        }

        String account = Accounts.getAccount(player);
        double balance = AccountHoldings.getAccountAssetBalance(account, code);
        if(retrieveBalance(account, balance, ammount)){
            runnable.run();
        }else {
            if(retrieveBalanceHook(account, balance, code, ammount, player)){
                runnable.run();
            }else {
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                        Map.entry("%asset%", code)
                ));
            }
        }
    }

}
