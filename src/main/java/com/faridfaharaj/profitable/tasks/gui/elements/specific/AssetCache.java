package com.faridfaharaj.profitable.tasks.gui.elements.specific;

import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Candle;

public final class AssetCache {

    private Asset asset;
    private Candle lastCandle;

    public AssetCache(Asset asset, Candle lastCandle){
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
