package com.cappuccino.profitable.commands;

import com.cappuccino.profitable.Profitable;
import com.cappuccino.profitable.util.NamingUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PluginInfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        sender.sendMessage(NamingUtil.profitablePrefix() + " - v0.0.0");

        return true;
    }
}
