package com.faridfaharaj.profitable.util;

import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public final class ItemMetaCompat {

    private static final Method SET_ENCHANTMENT_GLINT_OVERRIDE = findSetEnchantmentGlintOverride();

    private ItemMetaCompat() {
    }

    public static void setEnchantmentGlintOverride(ItemMeta meta, boolean enabled) {
        if (meta == null || SET_ENCHANTMENT_GLINT_OVERRIDE == null) {
            return;
        }

        try {
            SET_ENCHANTMENT_GLINT_OVERRIDE.invoke(meta, enabled);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Older server APIs do not support forced glint; the item remains usable without it.
        }
    }

    private static Method findSetEnchantmentGlintOverride() {
        try {
            return ItemMeta.class.getMethod("setEnchantmentGlintOverride", Boolean.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
