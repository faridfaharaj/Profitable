package com.faridfaharaj.profitable.util;

import com.faridfaharaj.profitable.data.holderClasses.Asset;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.io.IOException;

public class VaultCompat {


    private static final String VAULT_CODE = "VLT";
    private static final String VAULT_NAME = "Vault Currency";
    private static final TextColor VAULT_COLOR = TextColor.color(0xFFBB15);


    public static Economy getEconomy(Plugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }

        return rsp.getProvider();
    }

    public static String getVaultCode() {

        return VAULT_CODE;

    }

    public static String getVaultName() {

        return VAULT_NAME;

    }

    public static TextColor getVaultColor() {

        return VAULT_COLOR;

    }

    public static byte[] getVaultCurrencyMeta() throws IOException {

        return Asset.metaData(

                VAULT_COLOR.value(),
                VAULT_NAME

        );

    }

}
