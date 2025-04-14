package com.faridfaharaj.profitable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Scheduler {

    private static boolean isFolia;
    private static final Plugin plugin;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (final ClassNotFoundException ignored) {
            isFolia = false;
        }
        plugin = Profitable.getInstance();
    }

    public static void runAtLocation(Location location, Runnable task) {
        if (isFolia) {
            // Folia: Use RegionScheduler to run at this location's region
            Server server = Bukkit.getServer();
            server.getRegionScheduler().execute(plugin, location, task);
        } else {
            // Paper/Spigot fallback
            new BukkitRunnable() {
                @Override
                public void run() {
                    task.run();
                }
            }.runTask(plugin);
        }
    }
}
