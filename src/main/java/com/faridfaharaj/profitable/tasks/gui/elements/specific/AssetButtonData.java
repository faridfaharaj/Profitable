package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;

public class AssetButtonData {

    Asset asset;
    Candle lastCandle;

    public AssetButtonData(Asset asset, Candle lastCandle){
        this.asset = asset;
        this.lastCandle = lastCandle;
    }

    public Asset getAsset(){
        return asset;
    }

    public Candle getlastCandle(){
        return lastCandle;
    }

}
