package com.cappuccino.profitable.data.holderClasses;

import com.cappuccino.profitable.Profitable;
import com.cappuccino.profitable.data.tables.Accounts;
import com.cappuccino.profitable.data.tables.AccountHoldings;
import com.cappuccino.profitable.util.RandomUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Asset {

    private final String code;
    private final int assetType;
    private final String name;
    private final ChatColor color;

    List<String> stringData;
    List<Double> numericalData;

    public Asset(String code, int assetType, ChatColor color, String name, List<String> stringData, List<Double> numericalData){

        this.code = code;
        this.assetType = assetType;
        this.name = name;
        this.color = color;

        this.stringData = stringData;
        this.numericalData = numericalData;

    }

    public Asset(String code, int assetType, ChatColor color, String name){

        this.code = code;
        this.assetType = assetType;
        this.name = name;
        this.color = color;

        this.stringData = Collections.emptyList();
        this.numericalData = Collections.emptyList();

    }

    public String getCode(){
        return code;
    }

    public int getAssetType(){
        return assetType;
    }

    public String getName(){
        return name;
    }

    public ChatColor getColor(){
        return color;
    }

    public List<String> getStringData(){
        return stringData;
    }

    public List<Double> getNumericalData(){
        return numericalData;
    }

    public static String holdingToChat(String code, double ammount, byte[] meta) throws IOException {

        ChatColor color;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(meta);
             DataInputStream dis = new DataInputStream(bis)) {

            color = ChatColor.valueOf(dis.readUTF());

        } catch (IOException e) {
            color = ChatColor.WHITE;
        }


        return color + (ammount + " " + code) + ChatColor.RESET;
    }

    public static byte[] metaData(ChatColor color, String name) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            if(color == null){
                dos.writeUTF(RandomUtil.randomChatColor().name());
            }else{
                dos.writeUTF(color.name());
            }

            dos.writeUTF(name);

            dos.writeInt(0);

            dos.writeInt(0);

            return bos.toByteArray();
        }
    }

    public static byte[] metaData(ChatColor color, String name, List<String> stringData, List<Double> numericData) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            if(color == null){
                dos.writeUTF(RandomUtil.randomChatColor().name());
            }else{
                dos.writeUTF(color.name());
            }

            dos.writeUTF(name);

            dos.writeInt(stringData.size());
            for(String string : stringData){
                dos.writeUTF(string);
            }

            dos.writeInt(numericData.size());
            for(double number : numericData){
                dos.writeDouble(number);
            }

            return bos.toByteArray();
        }
    }

    public static void distributeAsset(String account, String asset, int assetType, double ammount){

        switch (assetType) {
            case 2: // Item

                    sendCommodityItem(account, asset, (int) ammount);
                break;

            case 3: // Entity

                    sendCommodityEntity(account, asset, Accounts.getEntityClaimId(account), (int) ammount);
                    break;

            case 4: // Fluid

                return;

            case 5: // Energy

                return;

            default: // numerical value

                    sendBalance(account, asset, ammount);

                break;
        }

        /*
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1 , 1);
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1 , 1);
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1 , 1);
        */

    }

    public static void sendBalance(String account, String asset, double ammount){

        double balance = AccountHoldings.getAccountAssetBalance(account, asset);
        AccountHoldings.setHolding(account, asset, balance + ammount);

    }

    public static void sendCommodityItem(String account, String asset, int amount){

        Material material = Material.getMaterial(asset);
        int maxStackSize = material.getMaxStackSize();

        Location location = Accounts.getItemDelivery(account);
        World world = location.getWorld();
        Block block = location.getBlock();
        if (block.getState() instanceof Chest chest) {

            int missing = amount;
            while (missing > 0) {
                int giveAmount = Math.min(missing, maxStackSize);
                ItemStack itemStack = new ItemStack(material, giveAmount);


                Inventory inventory = chest.getInventory();
                for (ItemStack drop : inventory.addItem(itemStack).values()) {
                    world.dropItemNaturally(location, drop);
                }

                missing -= giveAmount;
            }

        }else{

            world.dropItemNaturally(location, new ItemStack(material, amount));

        }

        world.spawnParticle(Particle.FIREWORK, location.add(0.5,0.5,0.5), 5);
        world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
        world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
        world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);

    }

    public static void sendCommodityEntity(String player, String asset, String id, int amount){

        EntityType entityType = EntityType.fromName(asset);

        Location location = Accounts.getEntityDelivery(player);
        World world = location.getWorld();

        for(int i = 0; i<amount; i++){
            Entity entity = world.spawnEntity(location, entityType);
            entity.setCustomName(id);
            entity.setCustomNameVisible(true);
        }

        world.spawnParticle(Particle.FIREWORK, location, 10);
        world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
        world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1,1);
        world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1,1);

    }

    public static boolean retrieveAsset(Player player, String notice, String asset, int assetType, double ammount){

        return switch (assetType) {
            case 2 -> // Item
                    retrieveCommodityItem(player, notice, asset, (int) ammount);
            case 3 -> // Entity
                    retrieveCommodityEntity(player, notice, asset, Accounts.getEntityClaimId(Accounts.getAccount(player)), (int) ammount);
            case 4 -> // Fluid
                    false;
            case 5 -> // Energy
                    false;
            default -> // any numerical value
                    retrieveBalance(player, notice, asset, ammount);
        };

    }

    public static boolean retrieveBalance(Player player, String notice, String asset, double ammount){
        String account = Accounts.getAccount(player);

        double balance = AccountHoldings.getAccountAssetBalance(account, asset);
        double difference = balance - ammount;
        if(difference < 0){
            player.sendMessage(ChatColor.RED + notice + ", insufficient " + asset + " on your account");
            return false;
        }
        AccountHoldings.setHolding(account, asset, difference);
        return true;
    }

    public static boolean retrieveCommodityItem(Player player, String notice, String asset, int amount){

        Inventory inventory = player.getInventory();

        ItemStack itemStack = new ItemStack(Material.getMaterial(asset), amount);
        if (inventory.containsAtLeast(itemStack, amount)) {
            inventory.removeItem(itemStack);
            return true;
        }

        //get from delivery chest
        /*
        Location location = Accounts.getItemDelivery(Accounts.getAccount(player));
        Block block = location.getBlock();
        if (block.getState() instanceof Chest chest) {
            chest.getInventory();
        }else{
            player.sendMessage(ChatColor.YELLOW + "Container set for delivery is missing");
        }

        if(inventory.containsAtLeast(itemStack, amount)){
            inventory.removeItem(itemStack);
            return true;
        }*/


        player.sendMessage(ChatColor.RED + notice + ", not enough " + asset.toLowerCase().replace("_", " "));
        return false;

    }

    public static boolean retrieveCommodityEntity(Player player, String notice, String asset, String id, int amount){

        EntityType entityType = EntityType.fromName(asset);
        World world = player.getWorld();

        int entitiesRemaining = amount;
        List<Entity> entities = new ArrayList<>();

        for(Entity entity : world.getEntities()){

            if(id.equals(entity.getCustomName())){

                if(entity.getType().equals(entityType)){

                    entities.add(entity);
                    entitiesRemaining --;

                }

            }

            if(entitiesRemaining <= 0){

                for(Entity retrieved : entities){
                    world.spawnParticle(Particle.HAPPY_VILLAGER, retrieved.getLocation(), 5, 1,1,1,1);
                    retrieved.remove();
                }

                return true;
            }

        }

        player.sendMessage(ChatColor.RED + notice + ", Not enough claimed " + asset.toLowerCase().replace("_", " ") + "s around");
        return false;

    }

}
