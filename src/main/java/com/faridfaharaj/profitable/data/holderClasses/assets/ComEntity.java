package com.faridfaharaj.profitable.data.holderClasses.assets;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComEntity extends Asset {

    public ComEntity(String code, TextColor color, String name, ItemStack stack) {
        super(code, AssetType.COMMODITY_ENTITY, color, name, stack);
    }

    @Override
    public void distributeAsset(World world, String account, double ammount) {
        /*if(Configuration.PHYSICALDELIVERY){
            sendCommodityEntity(world,account, (int) ammount);
        }else {
            sendBalance(world,account, ammount);
        }*/

        sendBalance(world,account, ammount);
    }

    public void sendCommodityEntityToPlayer(Player player, String account, int amount){

        EntityType entityType = EntityType.fromName(stack.getType().name().replace("SPAWN_",""));

        String claimId = Accounts.getEntityClaimId(player.getWorld(),account);
        Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
            World world = player.getWorld();
            Location location = player.getLocation();

            for(int i = 0; i<amount; i++){
                Entity entity = world.spawnEntity(location, entityType);
                entity.setCustomName(claimId);
                entity.setCustomNameVisible(true);
            }

            world.spawnParticle(Particle.FIREWORK, location, 10);
            world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
            world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
            world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
        });

    }

    public void sendCommodityEntity(World world, String account, int amount){

        EntityType entityType = EntityType.fromName(stack.getType().name().replace("_SPAWN_EGG",""));

        Location location = Accounts.getEntityDelivery(world,account);
        String claimId = Accounts.getEntityClaimId(world,account);
        Profitable.getfolialib().getScheduler().runAtLocation(location, task -> {
            World locWorld = location.getWorld();

            for(int i = 0; i<amount; i++){
                Entity entity = locWorld.spawnEntity(location, entityType);
                entity.setCustomName(claimId);
                entity.setCustomNameVisible(true);
            }

            locWorld.spawnParticle(Particle.FIREWORK, location, 10);
            locWorld.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
            locWorld.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
            locWorld.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);
        });

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
            double balance = AccountHoldings.getAccountAssetBalance(player.getWorld(), account, code);
            if(retrieveBalance(player.getWorld(), account, balance, ammount)){
                runnable.run();
                return;
            }
        }

        if (Configuration.ALLOWEDCOMMODITYCOLLATERAL[2]) {

            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                if(retrieveCommodityEntity(player, Accounts.getEntityClaimId(player.getWorld(), Accounts.getAccount(player)), (int) ammount)){
                    runnable.run();
                }else{
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.not-enough-asset",
                            Map.entry("%asset%", code)
                    ));
                }

            });

        }

    }

    public boolean retrieveCommodityEntity(Player player, String id, int amount){

        // get from world

        EntityType entityType = EntityType.fromName(stack.getType().name().replace("_SPAWN_EGG",""));

        int entitiesRemaining = amount;
        List<Entity> entities = new ArrayList<>();

        List<Entity> nearbyEntities = player.getNearbyEntities(20, 20, 20);
        for(Entity entity : nearbyEntities){

            if(id.equals(entity.getCustomName())){

                if(entity.getType().equals(entityType)){

                    entities.add(entity);
                    entitiesRemaining --;

                }

            }

            if(entitiesRemaining <= 0){
                World world = player.getWorld();
                for(Entity retrieved : entities){
                    world.playSound(retrieved.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    world.spawnParticle(Particle.HAPPY_VILLAGER, retrieved.getLocation(), 5, 1,1,1,1);
                    retrieved.remove();
                }
                return true;
            }

        }
        return false;

    }

}
