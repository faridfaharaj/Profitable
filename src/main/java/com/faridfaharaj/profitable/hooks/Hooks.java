package com.faridfaharaj.profitable.hooks;

import com.faridfaharaj.profitable.Profitable;

public class Hooks {

    //Economies
    public static VaultHook vaultHook;
    public static PlayerPointsHook playerPointsHook;

    public static void initHooks(Profitable profitable){
        vaultHook = new VaultHook(profitable);
        playerPointsHook = new PlayerPointsHook(profitable);
    }


}
