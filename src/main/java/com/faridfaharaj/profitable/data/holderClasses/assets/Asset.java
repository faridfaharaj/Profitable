package com.faridfaharaj.profitable.data.holderClasses.assets;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.tables.AccountHoldings;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public abstract class Asset {

    protected final String code;
    protected final AssetType assetType;

    protected final TextColor color;
    protected final String name;
    protected final ItemStack stack;

    public enum AssetType {

        CURRENCY(1),
        COMMODITY_ITEM(2),
        COMMODITY_ENTITY(3);

        private final int value;

        AssetType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Asset.AssetType fromValue(int value) {
            for (Asset.AssetType t : Asset.AssetType.values()) {
                if (t.value == value) return t;
            }
            throw new IllegalArgumentException("Invalid AssetType code: " + value);
        }

    }

    public Asset(String code, AssetType assetType, TextColor color, String name, ItemStack stack){

        this.code = code;
        this.assetType = assetType;
        this.name = name;
        this.color = color;
        this.stack = stack;

    }

    public String getCode(){
        return code;
    }

    public AssetType getAssetType(){
        return assetType;
    }

    public String getName(){
        return name;
    }

    public TextColor getColor(){
        return color;
    }

    public ItemStack getStack(){
        return stack;
    }

    public byte[] metaData() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            dos.writeInt(getColor().value());
            dos.writeUTF(getName());

            Map<String, Object> map = stack.serialize();
            String json = GsonComponentSerializer.gson().serializer().toJson(map);
            dos.writeUTF(json);

            return bos.toByteArray();
        }
    }

    public static Asset assetFromMeta(String code, AssetType assetType, byte[] meta){
        TextColor color;
        String name;
        ItemStack stack;
        String desc;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(meta);
             DataInputStream dis = new DataInputStream(bis)) {

            color = TextColor.color(dis.readInt());
            name = dis.readUTF();

            String json = dis.readUTF();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> map = GsonComponentSerializer.gson().serializer().fromJson(json, type);
            stack = ItemStack.deserialize(map);

            desc = dis.readUTF();



        } catch (Exception e) {
            color = NamedTextColor.WHITE;
            name = code.toLowerCase();
            if(assetType == AssetType.CURRENCY){
                stack = new ItemStack(Material.EMERALD);
                ItemMeta itemMeta = stack.getItemMeta();
                itemMeta.setEnchantmentGlintOverride(true);
                stack.setItemMeta(itemMeta);
            }else if(assetType == AssetType.COMMODITY_ITEM){
                Material material = Material.getMaterial(code);
                if(material == null){
                    material = Material.PAPER;
                }
                stack = new ItemStack(material);
            } else if (assetType == AssetType.COMMODITY_ENTITY) {
                Material material = Material.getMaterial(code+"_SPAWN_EGG");
                if(material == null){
                    material = Material.PAPER;
                }
                stack = new ItemStack(material);
            }else {
                stack = new ItemStack(Material.PAPER);
            }
        }

        switch (assetType){
            case AssetType.CURRENCY:
                return new Currency(code, color, name, stack);
            case AssetType.COMMODITY_ITEM:
                return new ComItem(code, color, name, stack);
            case AssetType.COMMODITY_ENTITY:
                return new ComEntity(code, color, name, stack);
            default:
                return new Currency(code, color, name, stack);
        }
    }

    public abstract void distributeAsset(String account, double ammount);

    public void sendBalance(String account, double ammount){

        double balance = AccountHoldings.getAccountAssetBalance(account, code);
        AccountHoldings.setHolding(account, code, balance + ammount);

    }

    public abstract void chargeAndRun(Player player, double ammount, Runnable runnable);

    public boolean retrieveBalance(String account, double balance, double ammount){

        double difference = balance - ammount;
        if(difference < 0){

            return false;

        }
        if(difference <= 0){
            AccountHoldings.deleteHolding(account, code);
        }else{
            AccountHoldings.setHolding(account, code, difference);
        }
        return true;

    }

    public static boolean retrieveBalanceHook(String account, double balance, String asset, double ammount, Player player){
        if(VaultHook.isConnected() && Objects.equals(asset, VaultHook.getAsset().getCode())){

            double fee = Configuration.parseFee(Configuration.DEPOSITFEES, ammount);

            if(VaultHook.getEconomy().withdrawPlayer(player, ammount+fee).transactionSuccess()){
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.auto-deposit-notice",
                    Map.entry("%asset_amount%", MessagingUtil.assetAmmount(VaultHook.getAsset(), ammount))
                ));
                return true;
            }

        } else if (PlayerPointsHook.isConnected() && Objects.equals(asset, PlayerPointsHook.getAsset().getCode())) {

            double fee = Configuration.parseFee(Configuration.DEPOSITFEES, ammount);
            double total = Math.ceil(ammount+fee);
            if(PlayerPointsHook.getApi().take(player.getUniqueId(), (int) total)){
                if(total-ammount+fee != 0){
                    AccountHoldings.setHolding(account, asset, balance+(total-ammount+fee));
                }
                MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("assets.auto-deposit-notice",
                        Map.entry("%asset_amount%", MessagingUtil.assetAmmount(PlayerPointsHook.getAsset(), ammount))
                ));
                return true;
            }

        }

        return false;
    }

}
