package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.util.TextUtil;
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
        TextUtil.sendCustomMessage(sender, Component.text("Running ", TextColor.color(0x41B169)).append(Component.text("Profitable", TextColor.color(0x64FF9D)).decorate(TextDecoration.BOLD)).append(Component.text(" v0.1.1-beta", TextColor.color(0x64FF9D))).clickEvent(ClickEvent.openUrl("https://github.com/faridfaharaj/Profitable")).hoverEvent(HoverEvent.showText(Component.text("link to download page", NamedTextColor.BLUE))));

        TextUtil.sendButton(sender, "  /profitable:help", "/profitable:help");
        TextUtil.sendButton(sender, "  /profitable:help admin", "/profitable:help admin");


        return true;
    }
}
