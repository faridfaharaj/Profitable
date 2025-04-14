package com.faridfaharaj.profitable.util;

import net.kyori.adventure.text.format.TextColor;

import java.awt.*;
import java.util.Random;

public class RandomUtil {

    public static Random RANDOM = new Random();

    public static TextColor randomTextColor(){

        return TextColor.color(Color.getHSBColor(
                RANDOM.nextFloat(1f),
                RANDOM.nextFloat(0.7f)+0.3f,
                RANDOM.nextFloat(0.3f)+0.7f
        ).getRGB());

    }

}
