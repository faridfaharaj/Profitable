package com.faridfaharaj.profitable.util;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.UUID;

public class MessagingUtil {

    static DecimalFormat decimalFormat = new DecimalFormat("0.0####");

    public static Component buttonComponent(String text, String command){

        return Component.text(text).color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(command).color(NamedTextColor.GREEN)));

    }

    public static Component assetAmmount(Asset asset, double amount){
        return Component.text(decimalFormat.format(amount) + " " + asset.getCode(), asset.getColor()).hoverEvent(assetSummary(asset));
    }

    public static Component assetSummary(Asset asset){
        Component component = Component.text("").append(Component.text(asset.getName(), asset.getColor())).appendNewline()
                .append(Component.text(NamingUtil.nameType(asset.getAssetType()), NamedTextColor.BLUE));

        if(!asset.getStringData().isEmpty()){
            component = component.appendNewline();

            String[] words = asset.getStringData().getFirst().split(" ");
            int linelen = 0;
            for (String word : words) {
                linelen += word.length();
                if (linelen >= 26) {
                    linelen = 0;
                    component = component.appendNewline();
                }
                component = component.append(Component.text(word)).appendSpace();
            }
        }

        return  component;
    }

    public static Component profitablePrefix(){
        return Component.text("", Configuration.COLORTEXT);
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
            component = Component.text("             ", Configuration.COLORPROFITABLE)
                    .append(MessagingUtil.buttonComponent(" [Past] ", cmndpast));
        }else {
            component = Component.text("                   ", Configuration.COLORPROFITABLE);
        }

        component = component.append(Component.text("<" + current + "/" + total  + ">"));

        if(total > current){

            component = component.append(MessagingUtil.buttonComponent(" [Next] ", cmndnext));

        }

        return component;


    }

    public static void sendCustomMessage(CommandSender sender, Component component){
        if(Profitable.getfolialib().isSpigot()){

            String jsonMessage = GsonComponentSerializer.gson().serialize(component);
            sender.spigot().sendMessage(ComponentSerializer.parse(jsonMessage));

        }else {

            if(sender instanceof Player player){
                Profitable.getfolialib().getScheduler().runAtEntity(player, task -> {
                    sender.sendMessage(component);
                });
            }else{
                Profitable.getfolialib().getScheduler().runNextTick(task -> {
                    sender.sendMessage(component);
                });
            }

        }
    }

    public static void sendEmptyNotice(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text).color(Configuration.COLOREMPTY)));

    }

    public static void sendInfoNotice(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text).color(Configuration.COLORHIGHLIGHT)));

    }

    public static void sendSuccsess(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text, Configuration.COLORTEXT)));
    }

    public static void sendPlain(CommandSender sender, String text){

        sendCustomMessage(sender, profitablePrefix().append(Component.text(text, Configuration.COLORTEXT)));
    }

    public static void sendChargeNotice(CommandSender sender, double amount, double fee, Asset assetCharged){
        if(fee != 0){
            sendCustomMessage(sender, profitablePrefix().append(Component.text("Charged ")).append(assetAmmount(assetCharged, amount+fee)).append(Component.text(" (incl. " + decimalFormat.format(fee) + " " + assetCharged.getCode() + " fee)", NamedTextColor.RED)));
        }else {
            sendCustomMessage(sender, profitablePrefix().append(Component.text("Charged ")).append(assetAmmount(assetCharged, amount+fee)));
        }
    }

    public static void sendPaymentNotice(CommandSender sender, double amount, double fee, Asset assetCharged){
        if(fee != 0){
            sendCustomMessage(sender, profitablePrefix().append(Component.text("Received ")).append(assetAmmount(assetCharged, amount-fee)).append(Component.text(" (incl. " + decimalFormat.format(fee) + " " + assetCharged.getCode() + " fee)", NamedTextColor.RED)));
        }else {
            sendCustomMessage(sender, profitablePrefix().append(Component.text("Received ")).append(assetAmmount(assetCharged, amount-fee)));
        }
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

    public static String formatVolume(double number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000).replaceAll("\\.0$", "");
        } else if (number >= 1_000) {
            return String.format("%.1fk", number / 1_000).replaceAll("\\.0$", "");
        } else {
            if (number < 10) {
                return String.valueOf(number);
            } else {
                return String.format("%.0f", number);
            }
        }
    }

    public static String formatNumber(double number){
        return decimalFormat.format(number);
    }

}
