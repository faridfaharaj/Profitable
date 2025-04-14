package com.faridfaharaj.profitable.data.holderClasses;

import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.RandomUtil;
import com.faridfaharaj.profitable.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
import java.util.Objects;

public class Asset {

    private final String code;
    private final int assetType;
    private final String name;
    private final TextColor color;

    List<String> stringData;
    List<Double> numericalData;

    public Asset(String code, int assetType, TextColor color, String name, List<String> stringData, List<Double> numericalData){

        this.code = code;
        this.assetType = assetType;
        this.name = name;
        this.color = color;

        this.stringData = stringData;
        this.numericalData = numericalData;

    }

    public Asset(String code, int assetType, TextColor color, String name){

        this.code = code;
        this.assetType = assetType;
        this.name = name;
        this.color = color;

        this.stringData = Collections.emptyList();
        this.numericalData = Collections.emptyList();

    }

    public static Asset assetFromMeta(String code, int assetType, byte[] meta){
        TextColor color;
        String name;

        List<String> stringList = new ArrayList<>();
        List<Double> numericList = new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(meta);
             DataInputStream dis = new DataInputStream(bis)) {

            color = TextColor.color(dis.readInt());
            name = dis.readUTF();


            int lengthStrings = dis.readInt();
            if(lengthStrings > 0){
                for(int i = 0; i<lengthStrings; i++){
                    stringList.add(dis.readUTF());
                }
            }

            int lengthNumeric = dis.readInt();
            if(lengthNumeric > 0){
                for(int i = 0; i<lengthNumeric; i++){
                    numericList.add(dis.readDouble());
                }
            }


        } catch (IOException e) {
            color = NamedTextColor.WHITE;
            name = code.toLowerCase();
        }

        return new Asset(code, assetType, color, name, stringList, numericList);
    }

    public static Asset StringToCurrency(String currencystring){

        String[] MCdata = currencystring.split("_");

        TextColor color;
        String name;

        if(MCdata.length >= 2){

            name = MCdata[1];

        }else{
            name = MCdata[0];
        }
        if(MCdata.length >= 3){

            color = TextColor.fromHexString(MCdata[2]);

            if(color == null){
                color = RandomUtil.randomTextColor();
                Profitable.getInstance().getLogger().warning("Invalid main currency color, selecting at random");
            }

        }else {
            color = RandomUtil.randomTextColor();
        }

        return new Asset(MCdata[0], 1, color, name);
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

    public TextColor getColor(){
        return color;
    }

    public List<String> getStringData(){
        return stringData;
    }

    public List<Double> getNumericalData(){
        return numericalData;
    }

    public static Component holdingToChat(String code, double ammount, byte[] meta) throws IOException {

        TextColor color;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(meta);
             DataInputStream dis = new DataInputStream(bis)) {

            color = TextColor.color(dis.readInt());

        } catch (IOException e) {
            color = NamedTextColor.WHITE;
        }


        return Component.text( ammount + " " + code).color(color);
    }

    public static byte[] metaData(int color, String name) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            dos.writeInt(color);

            dos.writeUTF(name);

            dos.writeInt(0);

            dos.writeInt(0);

            return bos.toByteArray();
        }
    }

    public static byte[] metaData(String name) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            dos.writeInt(RandomUtil.randomTextColor().value());

            dos.writeUTF(name);

            dos.writeInt(0);

            dos.writeInt(0);

            return bos.toByteArray();
        }
    }

    public static byte[] metaData(Color color, String name, List<String> stringData, List<Double> numericData) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            if(color == null){
                dos.writeInt(RandomUtil.randomTextColor().value());
            }else{
                dos.writeInt(color.asRGB());
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

    public static byte[] metaData(Asset asset) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            dos.writeInt(asset.getColor().value());

            dos.writeUTF(asset.getName());

            dos.writeInt(asset.getStringData().size());
            for(String string : asset.getStringData()){
                dos.writeUTF(string);
            }

            dos.writeInt(asset.getNumericalData().size());
            for(double number : asset.getNumericalData()){
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
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();

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

        switch (assetType) {
            case 2: // Item
                    return retrieveCommodityItem(player, notice, asset, (int) ammount);
            case 3: // Entity
                    return retrieveCommodityEntity(player, notice, asset, Accounts.getEntityClaimId(Accounts.getAccount(player)), (int) ammount);
            case 4: // Fluid
                    return false;
            case 5: // Energy
                    return false;
            default: // any numerical value
                    return retrieveBalance(player, notice, asset, ammount);
        }

    }

    public static boolean retrieveBalance(Player player, String notice, String asset, double ammount){
        String account = Accounts.getAccount(player);

        double balance = AccountHoldings.getAccountAssetBalance(account, asset);
        double difference = balance - ammount;
        if(difference < 0){

            if(Objects.equals(asset, VaultHook.getAsset().getCode())){

                if(VaultHook.getEconomy().withdrawPlayer(player, ammount).transactionSuccess()){
                    return true;
                }

            } else if (Objects.equals(asset, PlayerPointsHook.getAsset().getCode())) {

                if(PlayerPointsHook.getApi().take(player.getUniqueId(), (int) Math.ceil(ammount))){
                    return true;
                }

            }

            TextUtil.sendError(player,notice + ", insufficient " + asset + " on your account");

            return false;
        }
        AccountHoldings.setHolding(account, asset, difference);
        return true;
    }

    public static boolean retrieveBalance(Player player, String notice, String asset, double ammount , boolean takeExternal){
        String account = Accounts.getAccount(player);

        double balance = AccountHoldings.getAccountAssetBalance(account, asset);
        double difference = balance - ammount;
        if(difference < 0){

            if(takeExternal){
                if(Objects.equals(asset, VaultHook.getAsset().getCode())){

                    if(VaultHook.getEconomy().withdrawPlayer(player, ammount).transactionSuccess()){
                        return true;
                    }

                } else if (Objects.equals(asset, PlayerPointsHook.getAsset().getCode())) {

                    if(PlayerPointsHook.getApi().take(player.getUniqueId(), (int) Math.ceil(ammount))){
                        return true;
                    }

                }
            }

            TextUtil.sendError(player,notice + ", insufficient " + asset + " on your account");

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
            TextUtil.sendWarning(player, "Your delivery container seems to be missing");
        }

        if(inventory.containsAtLeast(itemStack, amount)){
            inventory.removeItem(itemStack);
            return true;
        }*/


        TextUtil.sendError(player, notice + ", not enough " + asset.toLowerCase().replace("_", " "));
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
                    world.playSound(retrieved.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    world.spawnParticle(Particle.HAPPY_VILLAGER, retrieved.getLocation(), 5, 1,1,1,1);
                    retrieved.remove();
                }

                return true;
            }

        }

        TextUtil.sendError(player,notice + ", Not enough claimed " + asset.toLowerCase().replace("_", " ") + "s around");
        return false;

    }

}
