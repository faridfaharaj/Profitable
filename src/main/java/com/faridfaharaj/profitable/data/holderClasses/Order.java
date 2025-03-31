package com.faridfaharaj.profitable.data.holderClasses;

import com.faridfaharaj.profitable.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
    public String toString() {
        return  "ID: " + uuid +
                "\nAsset: " + asset + " Side: " + (sideBuy? "buy" : "sell") +
                "\nPrice: " + price + " Units: " + units;
    }

    //ID: 234SD-DA324g-AS234-234
    //Asset: VLT Side: buy
    //Price: 23 Units: 32
    public Component toComponent(){
        return Component.text("ID: ").append(Component.text(uuid, NamedTextColor.GRAY)).appendNewline()
                .append(Component.text("Asset: ")).append(Component.text(asset,NamedTextColor.GRAY)).append(Component.text(" Side: ")).append((sideBuy? Component.text("buy",Configuration.COLORBULLISH) : Component.text("sell",Configuration.COLORBEARISH))).appendNewline()
                .append(Component.text("Price: ")).append(Component.text(price,NamedTextColor.GRAY)).append(Component.text(" Units: ")).append(Component.text(units,NamedTextColor.GRAY));
    }

    // [ sell 32 VLT $23 ]
    public Component toStringSimplified() {
        return Component.text("[ "+(sideBuy? "buy " : "sell ") + units + " " + asset + " $" + price + " ]", NamedTextColor.GRAY);
    }

}
