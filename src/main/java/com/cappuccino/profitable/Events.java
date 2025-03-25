package com.cappuccino.profitable;

import com.cappuccino.profitable.data.DataBase;
import com.cappuccino.profitable.data.tables.Accounts;
import com.cappuccino.profitable.data.holderClasses.Asset;
import com.cappuccino.profitable.tasks.TemporalItems;
import com.cappuccino.profitable.util.NamingUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class Events implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {



    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        if (TemporalItems.holdingTemp.containsKey(player.getUniqueId())) {
            TemporalItems.removeTempItem(player);
        }

    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {

        DataBase.closeWorldConnection(event.getWorld().getName());

    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {

        TemporalItems.removeTempItem(event.getPlayer());

    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (TemporalItems.holdingTemp.containsKey(player.getUniqueId())) {
            Item droppedItem = event.getItemDrop();
            droppedItem.remove();
            TemporalItems.removeTemp(player);
        }
    }

    @EventHandler
    public void onPlayerSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (TemporalItems.holdingTemp.containsKey(player.getUniqueId())) {
            TemporalItems.removeTempItem(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (TemporalItems.holdingTemp.containsKey(player.getUniqueId())) {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            event.getDrops().removeIf(item -> item != null && item.isSimilar(mainHandItem));
            TemporalItems.removeTemp(player);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        if(TemporalItems.holdingTemp.containsKey(player.getUniqueId())){
            TemporalItems.removeTempItem((Player) player);
            if(player.getGameMode() == GameMode.CREATIVE) {
                event.setCancelled(false);
            }else{
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if(Objects.equals(TemporalItems.holdingTemp.get(player.getUniqueId()), TemporalItems.TemporalItem.CLAIMINGTAG)){
            Entity entity = event.getRightClicked();
            if(entity.getCustomName() != null){
                player.sendMessage(ChatColor.RED + "Cannot claim named entities");
            }else {

                double fees = Profitable.getInstance().getConfig().getDouble("exchange.commodities.claiming-fees");
                if(fees > 0 && !Asset.retrieveAsset(player, "Couldn't claim "+ entity.getName() , Configuration.MAINCURRENCYASSET.getCode(), Configuration.MAINCURRENCYASSET.getAssetType(), fees)){
                    event.setCancelled(true);
                    return;
                }

                player.sendMessage(NamingUtil.profitablePrefix()+"Claimed "+entity.getName() + " using: " + Configuration.MAINCURRENCYASSET.getColor() + fees + " " + Configuration.MAINCURRENCYASSET.getCode());
                entity.setCustomName(Accounts.getEntityClaimId(Accounts.getAccount(player)));
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        TemporalItems.TemporalItem tempItem = TemporalItems.holdingTemp.get(player.getUniqueId());
        if (tempItem != null) {

            if(tempItem == TemporalItems.TemporalItem.ITEMDELIVERYSTICK){

                Material material = event.getMaterial();
                if(!materialCooldown(material, player)){

                    Block clickedlock = event.getClickedBlock();
                    if(Accounts.changeItemDelivery(Accounts.getAccount(player), clickedlock)){
                        player.sendMessage(NamingUtil.profitableTopSeparator());
                        player.sendMessage("Updated item delivery location to: ");
                        player.sendMessage(ChatColor.YELLOW+clickedlock.getLocation().toVector().toString());
                        player.sendMessage(NamingUtil.profitableBottomSeparator());
                        TemporalItems.removeTempItem(player);
                    }else {
                        player.sendMessage(ChatColor.RED + "Item delivery location must be a container");
                    }

                }

                event.setCancelled(true);

            } else if (tempItem == TemporalItems.TemporalItem.ENTITYDELIVERYSTICK) {


                Material material = event.getMaterial();
                if(!materialCooldown(material, player)){

                    Location correctedlocation = event.getClickedBlock().getLocation().add(0.5,0,0.5);
                    correctedlocation = correctedlocation.add(event.getBlockFace().getDirection());

                    Accounts.changeEntityDelivery(Accounts.getAccount(player), correctedlocation);
                    event.getClickedBlock();
                    player.sendMessage(NamingUtil.profitableTopSeparator());
                    player.sendMessage("Updated Entity delivery location to: ");
                    player.sendMessage(ChatColor.YELLOW+correctedlocation.toVector().toString() + " (" + correctedlocation.getWorld().getName() + ")");
                    player.sendMessage(NamingUtil.profitableBottomSeparator());
                    TemporalItems.removeTempItem(player);

                }

                event.setCancelled(true);

            }

        }
    }

    private boolean materialCooldown(Material material, Player player){
        boolean onCooldown = player.getCooldown(material) != 0;
        if(!onCooldown){
            player.setCooldown(material, 40);
        }
        return onCooldown;
    }


}
