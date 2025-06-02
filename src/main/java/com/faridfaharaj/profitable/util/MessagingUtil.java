package com.faridfaharaj.profitable.util;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

public class MessagingUtil {

    static DecimalFormat decimalFormat = new DecimalFormat("0.0####");

    public static Component buttonComponent(String text, String command){

        return Component.text(text).color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(command).color(NamedTextColor.GREEN)));

    }

    public static String assetAmmount(Asset asset, double amount){
        return "<color:" + asset.getColor().asHexString() + ">" + decimalFormat.format(amount) + " " + asset.getCode() + "</color>";
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

    public static void sendMiniMessage(CommandSender sender, String message){

        Component component = MiniMessage.miniMessage().deserialize(message);

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

    public static void sendChargeNotice(CommandSender sender, double amount, double fee, Asset assetCharged){
        String feeString;
        if(fee != 0){
            feeString = Profitable.getLang().get("exchange.fee-display",
                    Map.entry("%amount%", decimalFormat.format(fee)),
                    Map.entry("%asset%", assetCharged.getCode())
            );
        }else {
            feeString = "";
        }

        MessagingUtil.sendMiniMessage(sender, Profitable.getLang().get("exchange.charge-notice").replace("%asset_amount%", MessagingUtil.assetAmmount(assetCharged, amount+fee)).replace("%fee%", feeString));

    }

    public static void sendPaymentNotice(CommandSender sender, double amount, double fee, Asset assetCharged){

        String feeString;
        if(fee != 0){
            feeString = Profitable.getLang().get("exchange.fee-display",
                    Map.entry("%amount%", decimalFormat.format(fee)),
                    Map.entry("%asset%", assetCharged.getCode())
            );
        }else {
            feeString = "";
        }

        MessagingUtil.sendMiniMessage(sender, Profitable.getLang().get("exchange.payment-notice").replace("%asset_amount%", MessagingUtil.assetAmmount(assetCharged, amount+fee)).replace("%fee%", feeString));
    }

    public static void sendSyntaxError(CommandSender sender, String text){
        MessagingUtil.sendMiniMessage(sender, "<red>"+text+"</red>");
    }

    public static void sendGenericInvalidAmount(CommandSender sender, String amount){
        MessagingUtil.sendMiniMessage(sender, Profitable.getLang().get("generic.error.invalid-amount").replace("%invalid_amount%", amount));
    }

    public static void sendGenericMissingPerm(CommandSender sender){
        MessagingUtil.sendMiniMessage(sender, Profitable.getLang().get("generic.error.missing-perm"));
    }

    public static void sendGenericCantConsole(CommandSender sender){
        MessagingUtil.sendMiniMessage(sender, Profitable.getLang().get("generic.error.cant-console"));
    }

    public static void sendGenericInvalidSubCom(CommandSender sender, String subCommand){
        MessagingUtil.sendMiniMessage(sender, Profitable.getLang().get("generic.error.invalid-subcommand").replace("%sub_command%", subCommand));
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
