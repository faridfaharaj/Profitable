package com.cappuccino.profitable.util;

import org.bukkit.ChatColor;

import java.util.Random;

public class RandomUtil {

    public static Random RANDOM = new Random();

    private static final ChatColor[] colors = {

            ChatColor.DARK_RED,
            ChatColor.RED,
            ChatColor.GOLD,
            ChatColor.YELLOW,
            ChatColor.DARK_GREEN,
            ChatColor.GREEN,
            ChatColor.AQUA,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_BLUE,
            ChatColor.BLUE,
            ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_PURPLE,

    };

    public static ChatColor randomChatColor(){

        return colors[RANDOM.nextInt(colors.length)];
    }

}
