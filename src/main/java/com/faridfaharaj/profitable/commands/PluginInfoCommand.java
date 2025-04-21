package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PluginInfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        MessagingUtil.sendCustomMessage(sender, Component.text("Running ", TextColor.color(0x41B169)).append(Component.text("Profitable", TextColor.color(0x64FF9D)).decorate(TextDecoration.BOLD)).append(Component.text(" v0.1.1-beta", TextColor.color(0x64FF9D))).clickEvent(ClickEvent.openUrl("https://github.com/faridfaharaj/Profitable")).hoverEvent(HoverEvent.showText(Component.text("link to download page", NamedTextColor.BLUE)))
                .appendNewline().append(Component.text("[/profitable:help]",Configuration.COLORINFO).clickEvent(ClickEvent.runCommand("/profitable:help")).hoverEvent(HoverEvent.showText(Component.text("/profitable:help",Configuration.COLORINFO))))
                .append(Component.text("  [/profitable:help admin]",Configuration.COLORINFO).clickEvent(ClickEvent.runCommand("/profitable:help admin")).hoverEvent(HoverEvent.showText(Component.text("/profitable:help admin",Configuration.COLORINFO))))

        );
        return true;
    }
}
