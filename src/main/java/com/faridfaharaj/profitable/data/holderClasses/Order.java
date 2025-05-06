package com.faridfaharaj.profitable.data.holderClasses;

import com.faridfaharaj.profitable.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class Order{

    private final UUID uuid;
    private final String owner;
    private final String asset;
    private final boolean sideBuy;
    private final double price;
    private double units;
    private OrderType type;

    public Order(UUID uuid, String owner, String asset, boolean sideBuy, double price, double units, OrderType type) {
        this.uuid = uuid;
        this.owner = owner;
        this.asset = asset;
        this.sideBuy = sideBuy;
        this.price = price;
        this.units = units;
        this.type = type;
    }

    public Order(Order order) {
        this.uuid = order.getUuid();
        this.owner = order.getOwner();
        this.asset = order.getAsset();
        this.sideBuy = order.isSideBuy();
        this.price = order.getPrice();
        this.units = order.getUnits();
        this.type = order.getType();
    }

    public enum OrderType {
        LIMIT(0),
        MARKET(1),
        STOP_LIMIT(2),
        STOP_MARKET(3);

        private final int value;

        OrderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static OrderType fromValue(int value) {
            for (OrderType t : OrderType.values()) {
                if (t.value == value) return t;
            }
            throw new IllegalArgumentException("Invalid OrderType code: " + value);
        }

    }

    // Getters
    public UUID getUuid() { return uuid; }
    public String getOwner() { return owner; }
    public String getAsset() { return asset; }
    public boolean isSideBuy() { return sideBuy; }
    public double getPrice() { return price; }
    public double getUnits() { return units; }
    public OrderType getType() { return type; }

    public void setUnits(double units){
        this.units = units;
    }

    public void setType(OrderType type){
        this.type = type;
    }

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
        return  Component.text("Asset: ").append(Component.text(asset,NamedTextColor.GRAY)).append(Component.text(" Side: ")).append((sideBuy? Component.text("buy",Configuration.COLORBULLISH) : Component.text("sell",Configuration.COLORBEARISH))).append(Component.text(" Type: ")).append(Component.text(type.toString().replace("_", "-"), NamedTextColor.GRAY)).appendNewline()
                .append(Component.text("Price: ")).append(Component.text(price,NamedTextColor.GRAY)).append(Component.text(" Units: ")).append(Component.text(units,NamedTextColor.GRAY));
    }

    // [ sell 32 VLT $23 ]
    public Component toStringSimplified() {
        return Component.text("[ "+ type.toString().replace("_", "-") +(sideBuy? " buy " : " sell ") + units + " " + asset + " $" + price + " ]", NamedTextColor.GRAY);
    }

}
