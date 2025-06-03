package com.faridfaharaj.profitable.data.holderClasses;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
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

        @Override
        public String toString() {
            switch (this) {
                case LIMIT: return Profitable.getLang().getString("orders.order-types.limit-order");
                case MARKET: return Profitable.getLang().getString("orders.order-types.market-order");
                case STOP_LIMIT: return Profitable.getLang().getString("orders.order-types.stop-limit-order");
                case STOP_MARKET: return "Stop Market Order";
                default: return name(); // fallback to enum name
            }
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
        return  Component.text("Asset: ", Configuration.COLORTEXT).append(Component.text(asset, Configuration.COLORHIGHLIGHT)).append(Component.text(" Side: ")).append((sideBuy? Component.text("buy",Configuration.COLORBULLISH) : Component.text("sell",Configuration.COLORBEARISH))).append(Component.text(" Type: ")).append(Component.text(type.toString().replace("_", "-"), Configuration.COLORHIGHLIGHT)).appendNewline()
                .append(Component.text("Price: ")).append(Component.text(price, Configuration.COLORHIGHLIGHT)).append(Component.text(" Units: ")).append(Component.text(units, Configuration.COLORHIGHLIGHT));
    }

    // [ sell 32 VLT $23 ]
    public String toStringSimplified() {
        return "[ "+ type.toString().replace("_", "-") +(sideBuy? " buy " : " sell ") + units + " " + asset + " $" + price + " ]";
    }

}
