package com.cappuccino.profitable.data.holderClasses;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Order{

    private final String uuid;
    private final String owner;
    private final String asset;
    private final boolean sideBuy;
    private final double price;
    private double units;

    public Order(String uuid, String owner, String asset, boolean sideBuy, double price, double units) {
        this.uuid = uuid;
        this.owner = owner;
        this.asset = asset;
        this.sideBuy = sideBuy;
        this.price = price;
        this.units = units;
    }

    // Getters
    public String getUuid() { return uuid; }
    public String getOwner() { return owner; }
    public String getAsset() { return asset; }
    public boolean isSideBuy() { return sideBuy; }
    public double getPrice() { return price; }
    public double getUnits() { return units; }

    @Override
    //ID: 234SD-DA324g-AS234-234
    //Asset: VLT Side: buy
    //Price: 23 Units: 32

    public String toString() {
        return  "ID: " + ChatColor.GRAY + uuid + ChatColor.RESET +
                "\nAsset: " + ChatColor.GRAY + asset + ChatColor.RESET + " Side: " + (sideBuy? ChatColor.GREEN+"buy" : ChatColor.RED + "sell") + ChatColor.RESET +
                "\nPrice: " + ChatColor.GRAY + price + ChatColor.RESET + " Units: " + ChatColor.GRAY + units;
    }

    // [ sell 32 VLT $23 ]
    public String toStringSimplified() {
        return ChatColor.GRAY +"[ "+(sideBuy? "buy" : "sell") + ChatColor.GRAY + " " + units + " " + asset + " $" + price + " ]";
    }

    public static void sendCancelButton(Player player, String uuid){
        TextComponent textComponent = new TextComponent(ChatColor.YELLOW + "[ Click to cancel ]");
        textComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                "/account orders cancel " + uuid));
        player.spigot().sendMessage(textComponent);
    }

}
