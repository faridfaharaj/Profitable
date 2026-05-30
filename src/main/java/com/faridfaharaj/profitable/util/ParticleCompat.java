package com.faridfaharaj.profitable.util;

import org.bukkit.Particle;

public final class ParticleCompat {

    private static final Particle FIREWORK = resolve("FIREWORK", Particle.FIREWORKS_SPARK);
    private static final Particle HAPPY_VILLAGER = resolve("HAPPY_VILLAGER", Particle.VILLAGER_HAPPY);

    private ParticleCompat() {
    }

    public static Particle firework() {
        return FIREWORK;
    }

    public static Particle happyVillager() {
        return HAPPY_VILLAGER;
    }

    private static Particle resolve(String name, Particle fallback) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
