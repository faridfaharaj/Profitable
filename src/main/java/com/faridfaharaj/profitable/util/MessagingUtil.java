package com.faridfaharaj.profitable.util;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MessagingUtil {

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

    public static void sendFeeNotice(CommandSender sender, double fee, Asset assetCharged){
        sendCustomMessage(sender, profitablePrefix().append(Component.text("("+fee + " " + assetCharged.getCode() + " fee)", NamedTextColor.RED)));
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
