package com.faridfaharaj.profitable;

import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.tasks.gui.ChestGUI;
import com.faridfaharaj.profitable.util.MessagingUtil;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Map;
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
            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                event.getDrops().removeIf(item -> item != null && item.isSimilar(mainHandItem));
                TemporalItems.removeTemp(player);
            });
        }
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory inventory = event.getInventory();
        if(TemporalItems.holdingTemp.containsKey(player.getUniqueId())){
            TemporalItems.removeTempItem((Player) player);
            Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                event.setCancelled(player.getGameMode() != GameMode.CREATIVE);
            });
        }

        if(inventory.getHolder() instanceof ChestGUI gui){

            gui.slotInteracted((Player) player, event.getSlot(), event.getClick());

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if(Objects.equals(TemporalItems.holdingTemp.get(player.getUniqueId()), TemporalItems.TemporalItem.CLAIMINGTAG)){
            runItmCooldown(Material.NAME_TAG, event.getPlayer(), () -> {
                Entity entity = event.getRightClicked();
                if(entity.getCustomName() != null){
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.cant-reclaim-entity"));
                }else if(!Configuration.ALLOWENTITIES.contains(entity.getType().name())){
                    MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.error.cant-claim-entity"));

                } else {

                    Runnable claim = () -> {
                        Profitable.getfolialib().getScheduler().runAtEntity(entity, task -> {
                            entity.setCustomName(Accounts.getEntityClaimId(player.getWorld(), Accounts.getAccount(player)));
                        });
                        MessagingUtil.sendComponentMessage(player,Profitable.getLang().get("assets.entity-claim-notice",
                            Map.entry("%entity%", entity.getName()),
                                Map.entry("%asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, Configuration.ENTITYCLAIMINGFEES))
                        ));
                    };


                    if(Configuration.ENTITYCLAIMINGFEES <= 0){
                        claim.run();
                    }else {
                        Asset.chargeAndRun(player, Configuration.MAINCURRENCYASSET, Configuration.ENTITYCLAIMINGFEES, claim);
                    }

                }
            });
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if(Configuration.MULTIWORLD){
            Assets.generateAssets(event.getWorld());
            Accounts.registerDefaultAccount(event.getWorld(), "server");
            Accounts.changeEntityDelivery(event.getWorld(), "server", new Location(Profitable.getInstance().getServer().getWorlds().getFirst(), 0, 0 ,0));
            Accounts.changeItemDelivery(event.getWorld(), "server", new Location(Profitable.getInstance().getServer().getWorlds().getFirst(), 0, 0 ,0));
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

                runItmCooldown(event.getMaterial(), player, () -> {
                    Block block = event.getClickedBlock();
                    if(block != null){
                        if(!(block.getState() instanceof Container)){
                            MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("delivery.error.items-must-be-container"));
                            return;
                        }
                        Location correctedlocation = block.getLocation();
                        if(Accounts.changeItemDelivery(player.getWorld(), Accounts.getAccount(player), correctedlocation)){
                            MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("delivery.updated-item",
                                    Map.entry("%position%", correctedlocation.toVector() + " (" + correctedlocation.getWorld().getName() + ")")
                            ));

                            TemporalItems.removeTempItem(player);
                        }
                    }
                });

            } else if (tempItem == TemporalItems.TemporalItem.ENTITYDELIVERYSTICK) {

                runItmCooldown(event.getMaterial(), player, () -> {

                    Block block = event.getClickedBlock();
                    if(block != null){
                        Location correctedlocation = block.getLocation().add(0.5,0,0.5).add(event.getBlockFace().getDirection());
                        if(Accounts.changeEntityDelivery(player.getWorld(), Accounts.getAccount(player), correctedlocation)){
                            MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("delivery.updated-entity",
                                    Map.entry("%position%", correctedlocation.toVector() + " (" + correctedlocation.getWorld().getName() + ")")
                            ));

                            TemporalItems.removeTempItem(player);
                        }
                    }

                });

            }

            event.setCancelled(true);

        }
    }

    private void runItmCooldown(Material material, Player player, Runnable runnable){
        Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
            if (player.getCooldown(material) == 0) {
                player.setCooldown(material, 40);
                runnable.run();
            }
        });
    }


}
