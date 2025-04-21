package com.faridfaharaj.profitable.tasks;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.tables.Candles;
import com.faridfaharaj.profitable.data.holderClasses.Candle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import java.awt.*;
import java.util.List;

public class MapGraphRenderer extends MapRenderer {

    static Color volumeColor = new Color(0x9B939393, true);

    static Color graphColor = new Color(0xFFFFFFFF, true);

    private boolean rendered = false;
    private final String asset;
    private final long time;
    private final String interval;

    public MapGraphRenderer(String asset, long time, String interval){
        this.asset = asset;
        this.time = time;
        this.interval = interval;
    }


    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (rendered) return;
        rendered = true;


        canvas.drawText(1, 1, MinecraftFont.Font, asset + " (" + interval + ")");

        int bottomOffset = 2;
        int bottom = 128 - bottomOffset;
        int top = 128-bottomOffset-10;

        rectangle(canvas, 0, bottom-top-1,128, bottom-top-1, graphColor);

        rectangle(canvas, 0, bottom+1,128, bottom+1, graphColor);


        List<Candle> candles;

        if (time > 5376000) {
            candles = Candles.getInterval(asset, player.getWorld().getFullTime()/720000*720000-time, 2);
        }else if(time > 768000){
            candles = Candles.getInterval(asset, player.getWorld().getFullTime()/168000*168000-time, 1);
        } else{
            candles = Candles.getInterval(asset, player.getWorld().getFullTime()/24000*24000-time, 0);
        }

        if(candles.size() <= 1){

            canvas.drawText(46, bottom - top/2 - 5, MinecraftFont.Font, "No data");

            return;
        }

        double lowest = candles.getLast().getLow();
        double highest = candles.getLast().getHigh();

        if(highest == lowest){
            canvas.drawText(25, bottom - top/2 - 5, MinecraftFont.Font, "No price change");
            return;
        }

        double ceiling = highest - lowest;


        double range = highest-lowest;

        double interval = Math.pow(10, Math.floor(Math.log10(range)));

        while (range / interval < 3) {
            interval/=2;
        }

        int wideness = Math.max(Math.round((float) 128 /(candles.size()-1)), 4);
        int spacing = Math.max(1,Math.round((float) wideness / 4));
        int bodyWideness = wideness-spacing;
        int center = bodyWideness/2 + spacing;

        int start = 128%wideness > 0?1:0;

        for(int i = 0; i<candles.size()-1-start; i++){

            System.out.println(candles.get(i+start).toString());

            int candleIndex = i+start;
            int offset = wideness*i;

            double openPos = (candles.get(candleIndex).getOpen()-lowest),
                    closePos = (candles.get(candleIndex).getClose()-lowest),
                    highPos = (candles.get(candleIndex).getHigh()-lowest),
                    lowPos = (candles.get(candleIndex).getLow()-lowest),
                    volume = (candles.get(candleIndex).getVolume());


            Color color = new Color(closePos<openPos? Configuration.COLORBEARISH.value() : Configuration.COLORBULLISH.value());

            openPos = bottom - Math.ceil( openPos / ceiling * top);
            closePos = bottom - Math.ceil( closePos / ceiling * top);
            highPos = bottom - Math.ceil( highPos / ceiling * top);
            lowPos = bottom - Math.ceil( lowPos / ceiling * top);


            if(volume > 0){
                volume = bottom - (int) Math.ceil(volume / candles.getLast().getVolume() * ((double) top /3));
                shadedRectangle(canvas, offset+spacing, bottom,offset+wideness-1, (int) volume, volumeColor);
            }

            shadedRectangle(canvas, offset+center, (int) Math.min(openPos, closePos),offset+center, (int) highPos, color);
            shadedRectangle(canvas, offset+spacing, (int) openPos,offset+wideness-1, (int) closePos, color);
            shadedRectangle(canvas, offset+center, (int) Math.max(openPos,closePos),offset+center, (int) lowPos, color);
        }

        for (double current = Math.ceil(lowest / interval) * interval; current < highest; current += interval) {

            int y = bottom - (int) Math.ceil((current-lowest) / ceiling * top);

            if(y <= bottom-(top-7)){
                break;
            }

            if(y >= bottom-16){
                continue;
            }

            DIERectangle(canvas, 0, y, 128, y, graphColor);
            canvas.drawText(1, y+2, MinecraftFont.Font, String.valueOf(current));
        }

        canvas.drawText(1, bottom-top+1, MinecraftFont.Font, String.valueOf(highest));
        canvas.drawText(1, bottom-7, MinecraftFont.Font, String.valueOf(lowest));


    }

    public static void rectangle(MapCanvas canvas, int initX, int initY, int targetX, int targetY, Color color){

        for (int x = Math.min(initX, targetX); x <= Math.max(initX, targetX); x++) {
            for (int y = Math.min(initY, targetY); y <= Math.max(initY, targetY); y++) {
                canvas.setPixelColor(x, y, color);
            }
        }

    }

    public static void transparentRectangle(MapCanvas canvas, int initX, int initY, int targetX, int targetY, Color color){

        for (int x = Math.min(initX, targetX); x <= Math.max(initX, targetX); x++) {
            for (int y = Math.min(initY, targetY); y <= Math.max(initY, targetY); y++) {
                canvas.setPixelColor(x, y, blendColors(canvas.getPixelColor(x,y), color));
            }
        }
    }

    public static void shadedRectangle(MapCanvas canvas, int initX, int initY, int targetX, int targetY, Color color){

        for (int x = Math.min(initX, targetX); x <= Math.max(initX, targetX); x++) {
            for (int y = Math.min(initY, targetY); y <= Math.max(initY, targetY); y++) {
                canvas.setPixelColor(x, y, color);
                canvas.setPixelColor(x+1, y+1, color.darker());
            }
        }

    }

    public static void DIERectangle(MapCanvas canvas, int initX, int initY, int targetX, int targetY, Color color){


        for (int x = Math.min(initX, targetX); x <= Math.max(initX, targetX); x++) {
            for (int y = Math.min(initY, targetY); y <= Math.max(initY, targetY); y++) {
                if(canvas.getPixelColor(x,y) == null){
                    canvas.setPixelColor(x, y, color);
                }
            }
        }
    }

    public static ItemStack createGraphMap(Player player, String assetid, long time, String interval) {
        MapView mapView = Bukkit.createMap(player.getWorld());
        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(new MapGraphRenderer(assetid, time, interval));

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapView(mapView);
        mapItem.setItemMeta(meta);

        return mapItem;
    }


    public static Color blendColors(Color base, Color overlay) {
        if (base == null) return overlay;

        float alphaOver = overlay.getAlpha() / 255.0f;
        float alphaBase = base.getAlpha() / 255.0f;

        int r = (int) ((overlay.getRed() * alphaOver) + (base.getRed() * (1 - alphaOver)));
        int g = (int) ((overlay.getGreen() * alphaOver) + (base.getGreen() * (1 - alphaOver)));
        int b = (int) ((overlay.getBlue() * alphaOver) + (base.getBlue() * (1 - alphaOver)));

        int alphaOut = (int) ((alphaOver + alphaBase * (1 - alphaOver)) * 255);

        return new Color(r, g, b, alphaOut);
    }

    public static Color blendColors(Color base, Color overlay, float amount) {
        if (base == null) return overlay;

        float alphaOver = amount / 255.0f;
        float alphaBase = base.getAlpha() / 255.0f;

        int r = (int) ((overlay.getRed() * alphaOver) + (base.getRed() * (1 - alphaOver)));
        int g = (int) ((overlay.getGreen() * alphaOver) + (base.getGreen() * (1 - alphaOver)));
        int b = (int) ((overlay.getBlue() * alphaOver) + (base.getBlue() * (1 - alphaOver)));

        int alphaOut = (int) ((alphaOver + alphaBase * (1 - alphaOver)) * 255);

        return new Color(r, g, b, alphaOut);
    }

}
