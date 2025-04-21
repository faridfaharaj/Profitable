package com.faridfaharaj.profitable;

import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
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
                MessagingUtil.sendError(player, "Cannot claim named entities");
            }else if(!Configuration.ALLOWENTITIES.contains(entity.getType().name())){

                MessagingUtil.sendError(player, "Owning this entity isn't allowed");

            } else {

                if(Configuration.ENTITYCLAIMINGFEES > 0 && !Asset.retrieveAsset(player, "Couldn't claim "+ entity.getName() , Configuration.MAINCURRENCYASSET, Configuration.ENTITYCLAIMINGFEES)){
                    event.setCancelled(true);
                    return;
                }

                MessagingUtil.sendCustomMessage(player, MessagingUtil.profitablePrefix().append(Component.text("Claimed "+entity.getName()))
                        .append(Configuration.ENTITYCLAIMINGFEES == 0?
                                 Component.text(" FOR FREE", NamedTextColor.GREEN):
                                 Component.text(" using ").append(MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, Configuration.ENTITYCLAIMINGFEES))
                        )
                );
                entity.setCustomName(Accounts.getEntityClaimId(Accounts.getAccount(player)));
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if(Configuration.MULTIWORLD){
            try {
                DataBase.updateWorld(event.getWorld());
                Assets.generateAssets();
                Accounts.registerDefaultAccount("server");
                Accounts.changeEntityDelivery("server", Location.BLOCK_ZERO.toLocation(event.getWorld()));
                Accounts.changeItemDelivery("server", Location.BLOCK_ZERO.toLocation(event.getWorld()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        if(Configuration.MULTIWORLD){
            Accounts.logOut(event.getPlayer().getUniqueId());
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
                    Block block = event.getClickedBlock();
                    if(block != null){
                        if(!(block.getState() instanceof Container)){
                            MessagingUtil.sendError(player, "Item delivery location must be a container");
                            return;
                        }
                        Location correctedlocation = block.getLocation();
                        if(Accounts.changeItemDelivery(Accounts.getAccount(player), correctedlocation)){
                            MessagingUtil.sendSuccsess(player,"Updated item delivery to: " + correctedlocation.toVector() + " (" + correctedlocation.getWorld().getName() + ")");

                            TemporalItems.removeTempItem(player);
                        }else {
                            MessagingUtil.sendError(player, "Could not update Item delivery");
                        }
                    }

                }

                event.setCancelled(true);

            } else if (tempItem == TemporalItems.TemporalItem.ENTITYDELIVERYSTICK) {


                Material material = event.getMaterial();
                if(!materialCooldown(material, player)){

                    Block block = event.getClickedBlock();
                    if(block != null){
                        Location correctedlocation = block.getLocation().add(0.5,0,0.5);
                        correctedlocation = correctedlocation.add(event.getBlockFace().getDirection());

                        Accounts.changeEntityDelivery(Accounts.getAccount(player), correctedlocation);
                        MessagingUtil.sendSuccsess(player,"Updated entity delivery to: " + correctedlocation.toVector() + " (" + correctedlocation.getWorld().getName() + ")");
                        TemporalItems.removeTempItem(player);
                    }

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
