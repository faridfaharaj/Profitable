package com.faridfaharaj.profitable.data.holderClasses;

public class Candle {

    private final double open;
    private final double close;
    private final double high;
    private final double low;

    private final double volume;

    public Candle(double open, double close, double high, double low, double volume) {
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }


    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getVolume() {
        return volume;
    }

    @Override
    public String toString(){
        return "Open: " + open + "  Close: " + close + "  High: " + high + "  Low: " + low + "  Volume: " + volume;
    }
}
