package com.faridfaharaj.profitable.util;

import com.faridfaharaj.profitable.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class TextUtil {

    private static final Map<String, String> commodityNaming = new HashMap<>();

    private static final Pattern UNWANTED_SUFFIXES = Pattern.compile("_(block|ingot|nugget|bucket)$");

    static {
        commodityNaming.put("COW", "Live Cattle");
        commodityNaming.put("PIG", "Lean Hogs");
        commodityNaming.put("SHEEP", "Lamb");
        commodityNaming.put("CHICKEN", "Broilers");

        commodityNaming.put("ACACIA_PLANKS", "Lumber");
        commodityNaming.put("ACACIA_LOG", "Timber");

        commodityNaming.put("BIRCH_PLANKS", "Lumber");
        commodityNaming.put("BIRCH_LOG", "Timber");

        commodityNaming.put("CHERRY_PLANKS", "Lumber");
        commodityNaming.put("CHERRY_LOG", "Timber");

        commodityNaming.put("DARK_OAK_PLANKS", "Lumber");
        commodityNaming.put("DARK_OAK_LOG", "Timber");

        commodityNaming.put("JUNGLE_PLANKS", "Lumber");
        commodityNaming.put("JUNGLE_LOG", "Timber");

        commodityNaming.put("MANGROVE_PLANKS", "Lumber");
        commodityNaming.put("MANGROVE_LOG", "Timber");

        commodityNaming.put("OAK_PLANKS", "Lumber");
        commodityNaming.put("OAK_LOG", "Timber");

        commodityNaming.put("SPRUCE_PLANKS", "Lumber");
        commodityNaming.put("SPRUCE_LOG", "Timber");
    }




    private static final String[] assetTypeNaming = {"" , "Forex", "Commodity", "Commodity", "Commodity", "Commodity", "Stock"};

    public static String nameCommodity(String code) {
        String name = commodityNaming.get(code);
        if (name != null) {
            return name;
        }

        return UNWANTED_SUFFIXES.matcher(code).replaceAll("").toLowerCase().replace("_", " ");
    }

    public static String nameType(int assetType) {
        if (assetType < 1 || assetType >= assetTypeNaming.length) {
            return "Unknown";
        }
        return assetTypeNaming[assetType];
    }

    public static void sendButton(CommandSender sender, String text, String command){

        sendCustomMessage(sender, Component.text(text).color(Configuration.COLORINFO)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(command).color(Configuration.COLORINFO))));

    }

    public static Component profitablePrefix(){
        return Component.text("").append(Component.text("[",Configuration.COLORPROFITABLE))
                .append(Component.text("PRFT",Configuration.COLORPROFITABLE).decorate(TextDecoration.BOLD))
                .append(Component.text("] ",Configuration.COLORPROFITABLE));
    }

    public static Component profitableTopSeparator(){
        return Component.text("").append(Component.text("--------------- [",Configuration.COLORPROFITABLE))
                .append(Component.text("Profitable",Configuration.COLORPROFITABLE).decorate(TextDecoration.BOLD))
                .append(Component.text("] ----------------",Configuration.COLORPROFITABLE));
    }

    public static Component profitableTopSeparator(String text, String sides){
        return Component.text("").append(Component.text(sides + " [",Configuration.COLORPROFITABLE))
                .append(Component.text(text,Configuration.COLORPROFITABLE).decorate(TextDecoration.BOLD))
                .append(Component.text("] " + sides,Configuration.COLORPROFITABLE));
    }

    public static Component profitableBottomSeparator(){
        return Component.text("--------------------------------------------",Configuration.COLORPROFITABLE);
    }

    public static Component profitablePaginator(int current, int total, String command){

        String cmndpast = command + " " + (current-1);
        String cmndnext = command + " " + (current+1);

        Component component;

        if(0 < current){
            component = Component.text("-------------", Configuration.COLORPROFITABLE)
                    .append(Component.text(" [past] ")
                            .clickEvent(ClickEvent.runCommand(cmndpast))
                            .hoverEvent(HoverEvent.showText(Component.text(cmndpast, Configuration.COLORINFO))));
        }else {
            component = Component.text("------------------ ", Configuration.COLORPROFITABLE);
        }

        component = component.append(Component.text("<" + current + "/" + total  + ">"));

        if(total > current){

            component = component.append(Component.text(" [next] ")
                    .clickEvent(ClickEvent.runCommand(cmndnext))
                    .hoverEvent(HoverEvent.showText(Component.text(cmndnext, Configuration.COLORINFO))))
                    .append(Component.text("-------------"));

        }else{

            component = component.append(Component.text(" ------------------"));

        }

        return component;


    }

    public static void sendCustomMessage(CommandSender sender, Component component){
        String jsonMessage = GsonComponentSerializer.gson().serialize(component);

        sender.spigot().sendMessage(ComponentSerializer.parse(jsonMessage));
    }

    public static void sendEmptyNotice(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text).color(Configuration.COLOREMPTY)));

    }

    public static void sendInfoNotice(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text).color(Configuration.COLORINFO)));

    }

    public static void sendSuccsess(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text)));
    }

    public static void sendWarning(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text).color(Configuration.COLORWARN)));
    }

    public static void sendError(CommandSender sender, String text){
        sendCustomMessage(sender, Component.text(text).color(Configuration.COLORERROR));
    }

    public static void sendGenericMissingPerm(CommandSender sender){
        sendError(sender, "You don't have permission to do that");
    }

    public static void sendGenericCantConsole(CommandSender sender){
        sendError(sender, "Cant use this command from the console");
    }

    public static void genericInvalidSubcom(CommandSender sender, String subcom){
        sendError(sender, "Invalid Subcommand " + subcom);
    }

    public static byte[] UUIDtoBytes(UUID uuid) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);

        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return buffer.array();
    }

    public static UUID UUIDfromBytes(byte[] uuid) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(uuid);

        return new UUID(buffer.getLong(),buffer.getLong());
    }

}
