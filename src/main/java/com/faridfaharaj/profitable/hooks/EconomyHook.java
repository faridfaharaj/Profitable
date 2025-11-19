package com.faridfaharaj.profitable.hooks;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;

import java.util.UUID;

public abstract class EconomyHook extends Hook {

    private Asset ASSET;

    public EconomyHook(Profitable profitable){
        super(profitable);
        ASSET = Configuration.MAINCURRENCYASSET;
    }

    public final Asset getAsset(){
        return ASSET;
    }

    public abstract void depositAccount(String account, double ammount);
    public abstract void withdrawAccount(String account, double ammount);
    public abstract double balanceUUID(UUID uuid);

}
