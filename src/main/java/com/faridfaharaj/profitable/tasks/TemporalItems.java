package com.faridfaharaj.profitable.tasks;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.util.MessagingUtil;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TemporalItems {

    public enum TemporalItem{

        INFOBOOK,
        CLAIMINGTAG,
        ITEMDELIVERYSTICK,
        ENTITYDELIVERYSTICK,
        GRAPHMAP;

    }

    public static HashMap<UUID, TemporalItem> holdingTemp = new HashMap();

    public static void removeTempItem(Player player){
        if(holdingTemp.containsKey(player.getUniqueId())){
            player.getInventory().setItemInMainHand(null);
            holdingTemp.remove(player.getUniqueId());
            player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1 , 1);
        }

    }

    public static void removeTemp(Player player){
        holdingTemp.remove(player.getUniqueId());
        player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1 , 1);

    }

    public static void addTemp(Player player, TemporalItem Item){
        removeTempItem(player);
        player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1 , 1);
        holdingTemp.put(player.getUniqueId(), Item);
    }



    //ITEMS-------------------------------------

    public static void sendClaimingTag(Player player){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            MessagingUtil.sendMiniMessage(player, Profitable.getLang().get("temp-items.error.main-hand-occupied"));
            return;
        }

        if(Configuration.ENTITYCLAIMINGFEES != 0){
            MessagingUtil.sendWarning(player,"Claiming fees are " + Configuration.ENTITYCLAIMINGFEES + " " + Configuration.MAINCURRENCYASSET.getCode() + " per entity");
        }

        TemporalItems.addTemp(player, TemporalItem.CLAIMINGTAG);

        setItemOnHand(player, new ItemStack(Material.NAME_TAG), "§dClaiming Tag");

    }

    public static void sendDeliveryStick(Player player, boolean items){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            MessagingUtil.sendMiniMessage(player, Profitable.getLang().get("temp-items.error.main-hand-occupied"));
            return;
        }

        TemporalItems.addTemp(player, items?TemporalItem.ITEMDELIVERYSTICK:TemporalItem.ENTITYDELIVERYSTICK);

        setItemOnHand(player, new ItemStack(Material.CARROT_ON_A_STICK), "§ddelivery marker");

    }

    public static void sendGraphMap(Player player, String assetid, long time, String interval){

        if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
            MessagingUtil.sendMiniMessage(player, Profitable.getLang().get("temp-items.error.main-hand-occupied"));
            return;
        }

        Asset asset = Assets.getAssetData(assetid);

        if(asset == null){
            MessagingUtil.sendMiniMessage(player, Profitable.getLang().get("assets.error.asset-not-found").replace("%asset%", assetid));
            return;
        }

        TemporalItems.addTemp(player, TemporalItem.GRAPHMAP);

        setItemOnHand(player, MapGraphRenderer.createGraphMap(player, assetid, time, interval), "§dGraph " + assetid);
    }

    public static void setItemOnHand(Player player, ItemStack item, String displayName){
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }

        Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
            player.getInventory().setItemInMainHand(item);
        });
    }

}
