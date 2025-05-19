package com.faridfaharaj.profitable.data.holderClasses;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.RandomUtil;
import com.faridfaharaj.profitable.util.MessagingUtil;
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

        return new Asset(MCdata[0].toUpperCase(), 1, color, name);
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

    public static void distributeAsset(String account, Asset asset, double ammount){

        switch (asset.getAssetType()) {
            case 2: // Item

                if(Configuration.PHYSICALDELIVERY){
                    sendCommodityItem(account, asset.getCode(), (int) ammount);
                }else {
                    sendBalance(account, asset.getCode(), ammount);
                }
                break;

            case 3: // Entity

                if(Configuration.PHYSICALDELIVERY){
                    sendCommodityEntity(account, asset.getCode(), (int) ammount);
                }else {
                    sendBalance(account, asset.getCode(), ammount);
                }
                break;

            case 4: // Fluid

                return;

            case 5: // Energy

                return;

            default: // numerical value

                    sendBalance(account, asset.getCode(), ammount);

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

    public static void sendItemToPlayer(Player player, String asset, int amount){

        Material material = Material.getMaterial(asset);
        int maxStackSize = material.getMaxStackSize();

        Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {

            int missing = amount;
            while (missing > 0) {
                int giveAmount = Math.min(missing, maxStackSize);
                ItemStack itemStack = new ItemStack(material, giveAmount);


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

    public static void sendCommodityEntityToPlayer(Player player, String account, String asset, int amount){

        EntityType entityType = EntityType.fromName(asset);

        String claimId = Accounts.getEntityClaimId(account);
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

    public static void sendCommodityItem(String account, String asset, int amount){

        Material material = Material.getMaterial(asset);
        int maxStackSize = material.getMaxStackSize();

        Location location = Accounts.getItemDelivery(account);

        Profitable.getfolialib().getScheduler().runAtLocation(location, task -> {

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
        );

    }

    public static void sendCommodityEntity(String account, String asset, int amount){

        EntityType entityType = EntityType.fromName(asset);

        Location location = Accounts.getEntityDelivery(account);
        String claimId = Accounts.getEntityClaimId(account);
        Profitable.getfolialib().getScheduler().runAtLocation(location, task -> {
            World world = location.getWorld();

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

    public static void chargeAndRun(Player player, String notice, Asset asset, double ammount, Runnable runnable){

        if(ammount == 0){
            runnable.run();
            return;
        }

        switch (asset.getAssetType()) {
            case 2: // Item

                // get from wallet
                if(Configuration.ALLOWEDCOMMODITYCOLLATERAL[0]){
                    String account = Accounts.getAccount(player);
                    double balance = AccountHoldings.getAccountAssetBalance(account, asset.getCode());
                    if(retrieveBalance(account, balance, asset.getCode(), ammount)){
                        runnable.run();
                        return;
                    }
                }

                if(Configuration.ALLOWEDCOMMODITYCOLLATERAL[1]){

                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        if(retrieveCommodityItem(player, asset.getCode(), (int) ammount)){
                            runnable.run();
                        }else {
                            MessagingUtil.sendError(player, notice + ", not enough " + asset.getCode().toLowerCase().replace("_", " "));
                        }

                    });

                }
                break;
            case 3: // Entity

                // get from wallet
                if(Configuration.ALLOWEDCOMMODITYCOLLATERAL[0]){
                    String account = Accounts.getAccount(player);
                    double balance = AccountHoldings.getAccountAssetBalance(account, asset.getCode());
                    if(retrieveBalance(account, balance, asset.getCode(), ammount)){
                        runnable.run();
                        return;
                    }
                }

                if (Configuration.ALLOWEDCOMMODITYCOLLATERAL[2]) {

                    Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                        if(retrieveCommodityEntity(player, asset.getCode(), Accounts.getEntityClaimId(Accounts.getAccount(player)), (int) ammount)){
                            runnable.run();
                        }else{
                            MessagingUtil.sendError(player,notice + ", Not enough claimed " + asset.getCode().toLowerCase().replace("_", " ") + "s around");
                        }

                    });

                }
                break;
            case 4: // Fluid
                    return;
            case 5: // Energy
                    return;
            default: // any numerical value

                String account = Accounts.getAccount(player);
                double balance = AccountHoldings.getAccountAssetBalance(account, asset.getCode());
                if(retrieveBalance(account, balance, asset.getCode(), ammount)){
                    runnable.run();
                }else {
                    if(retrieveBalanceExternal(account, balance, asset.getCode(), ammount, player)){
                        runnable.run();
                    }else {
                        MessagingUtil.sendError(player,notice + ", insufficient " + asset.getCode());
                    }
                }
        }
    }

    public static boolean retrieveBalance(String account, double balance, String asset, double ammount){

        double difference = balance - ammount;
        if(difference < 0){

            return false;

        }
        if(difference <= 0){
            AccountHoldings.deleteHolding(account, asset);
        }else{
            AccountHoldings.setHolding(account, asset, difference);
        }
        return true;

    }

    public static boolean retrieveBalanceExternal(String account, double balance, String asset, double ammount, Player player){
        if(VaultHook.isConnected() && Objects.equals(asset, VaultHook.getAsset().getCode())){

            double fee = Configuration.parseFee(Configuration.DEPOSITFEES, ammount);

            if(VaultHook.getEconomy().withdrawPlayer(player, ammount+fee).transactionSuccess()){
                MessagingUtil.sendCustomMessage(player, MessagingUtil.profitablePrefix().append(Component.text("Automatically deposited "))
                        .append(MessagingUtil.assetAmmount(VaultHook.getAsset(), ammount))
                        .append(Component.text(fee == 0?"":" (+ fees)", NamedTextColor.RED))
                );
                return true;
            }

        } else if (PlayerPointsHook.isConnected() && Objects.equals(asset, PlayerPointsHook.getAsset().getCode())) {

            double fee = Configuration.parseFee(Configuration.DEPOSITFEES, ammount);
            double total = Math.ceil(ammount+fee);
            if(PlayerPointsHook.getApi().take(player.getUniqueId(), (int) total)){
                if(total-ammount+fee != 0){
                    AccountHoldings.setHolding(account, asset, balance+(total-ammount+fee));
                }
                MessagingUtil.sendCustomMessage(player, MessagingUtil.profitablePrefix().append(Component.text("Automatically deposited "))
                        .append(MessagingUtil.assetAmmount(PlayerPointsHook.getAsset(), ammount))
                        .append(Component.text(fee == 0?"":" (+ fees)", NamedTextColor.RED))
                );
                return true;
            }

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

    public static boolean retrieveCommodityItem(Player player, String asset, int amount){

        // get from inventory
        Inventory inventory = player.getInventory();
        ItemStack itemStack = new ItemStack(Material.getMaterial(asset), amount);
        if (inventory.containsAtLeast(itemStack, amount)) {
            inventory.removeItem(itemStack);
            return true;
        }

        return false;

    }

    public static boolean retrieveCommodityEntity(Player player, String asset, String id, int amount){

        // get from world

        EntityType entityType = EntityType.fromName(asset);

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
