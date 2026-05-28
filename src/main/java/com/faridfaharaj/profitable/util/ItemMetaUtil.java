package com.faridfaharaj.profitable.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public final class ItemMetaUtil {

    private ItemMetaUtil() {
    }

    public static void applyGlint(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }

        if (!setModernGlint(meta)) {
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);
    }

    private static boolean setModernGlint(ItemMeta meta) {
        try {
            Method method = meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class);
            method.invoke(meta, Boolean.TRUE);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
