package com.faridfaharaj.profitable.hooks;

import com.faridfaharaj.profitable.Profitable;


public abstract class Hook {

    private boolean isConnected;

    public Hook(Profitable profitable){
        isConnected = initHook(profitable);
    }

    public abstract boolean initHook(Profitable profitable);

    public abstract <T> T getApi();

    public final boolean isConnected(){
        return isConnected;
    }
}
