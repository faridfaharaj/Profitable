package com.faridfaharaj.profitable.data.holderClasses.assets;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ComItem extends Asset {
    public ComItem(String code, TextColor color, String name, ItemStack stack) {
        super(code, AssetType.COMMODITY_ITEM, color, name, stack);
    }

    @Override
    public void distributeAsset(String account, double ammount) {
        if(Configuration.PHYSICALDELIVERY){
            sendCommodityItem(account, (int) ammount);
        }else {
            sendBalance(account, ammount);
        }
    }

    public void sendCommodityItem(String account, int amount){

        Location location = Accounts.getItemDelivery(account);

        Profitable.getfolialib().getScheduler().runAtLocation(location, task -> {

                    World world = location.getWorld();
                    Block block = location.getBlock();
                    if (block.getState() instanceof Chest chest) {

                        int missing = amount;
                        while (missing > 0) {
                            int giveAmount = Math.min(missing, stack.getMaxStackSize());
                            ItemStack itemStack = stack.asQuantity(giveAmount);


                            Inventory inventory = chest.getInventory();
                            for (ItemStack drop : inventory.addItem(itemStack).values()) {
                                world.dropItemNaturally(location, drop);
                            }

                            missing -= giveAmount;
                        }

                    }else{

                        world.dropItemNaturally(location, stack.asQuantity(amount));

                    }

                    world.spawnParticle(Particle.FIREWORK, location.add(0.5,0.5,0.5), 5);
                    world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
                    world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
                    world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
                }
        );

    }

    public void giveItemToPlayer(Player player, int amount){

        Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {

                    int missing = amount;
                    while (missing > 0) {
                        int giveAmount = Math.min(missing, stack.getMaxStackSize());
                        ItemStack itemStack = stack.asQuantity(giveAmount);


                        Inventory inventory = player.getInventory();
                        for (ItemStack drop : inventory.addItem(itemStack).values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }

                        missing -= giveAmount;
                    }
                    player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1,1);
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
                    player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
                    player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
                }
        );

    }

    @Override
    public void chargeAndRun(Player player, double ammount, Runnable runnable) {
        if(ammount == 0){
            runnable.run();
            return;
        }

        // get from wallet
        if(Configuration.ALLOWEDCOMMODITYCOLLATERAL[0]){
            String account = Accounts.getAccount(player);
            double balance = AccountHoldings.getAccountAssetBalance(account, code);
            if(retrieveBalance(account, balance, ammount)){
                runnable.run();
                return;
            }
        }

        if(Configuration.ALLOWEDCOMMODITYCOLLATERAL[1]){

            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                if(retrieveCommodityItem(player, (int) ammount)){
                    runnable.run();
                }else {
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                            Map.entry("%asset%", code)
                    ));
                }

            });

        }

    }

    public boolean retrieveCommodityItem(Player player, int amount){

        // get from inventory
        Inventory inventory = player.getInventory();
        ItemStack itemStack = stack;
        if (inventory.containsAtLeast(itemStack, amount)) {
            inventory.removeItem(itemStack);
            return true;
        }

        return false;

    }

    public static boolean getCommodityItemFromChest(Player player){
        /*
        // get from delivery chest
        if (Configuration.ALLOWEDCOMMODITYCOLLATERAL[0]) {
            Location location = Accounts.getItemDelivery(Accounts.getAccount(player));
            if (location != null) {
                final boolean[] result = {false};

                //this is wrong
                Scheduler.runAtLocation(location, () -> {
                    Block block = location.getBlock();
                    Inventory inventoryChest;
                    if (block.getState() instanceof Chest chest) {
                        inventoryChest = chest.getInventory();
                        if (inventoryChest.containsAtLeast(itemStack, amount)) {
                            inventoryChest.removeItem(itemStack);
                            result[0] = true;
                        }
                    }
                });

                return result[0];
            }
        }
        */
        return false;
    }


}
