package com.faridfaharaj.profitable.util;

import org.bukkit.Particle;

public final class ParticleUtil {
    public static final Particle FIREWORK = find("FIREWORKS_SPARK", "FIREWORK");
    public static final Particle HAPPY_VILLAGER = find("VILLAGER_HAPPY", "HAPPY_VILLAGER");

    private ParticleUtil() {
    }

    private static Particle find(String primary, String fallback) {
        try {
            return Particle.valueOf(primary);
        } catch (IllegalArgumentException ignored) {
            return Particle.valueOf(fallback);
        }
    }
}
