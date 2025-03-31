package com.faridfaharaj.profitable.util;

import net.kyori.adventure.text.format.TextColor;

import java.util.Random;

public class RandomUtil {

    public static Random RANDOM = new Random();

    public static TextColor randomTextColor(){

        int r, g, b;

        do {
            r = RANDOM.nextInt(256);
            g = RANDOM.nextInt(256);
            b = RANDOM.nextInt(256);
        } while (Math.max(Math.max(r,g),b) >= 200);

        return TextColor.color(r,g,b);

    }

}
