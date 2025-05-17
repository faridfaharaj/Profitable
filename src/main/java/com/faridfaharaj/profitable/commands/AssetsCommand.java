package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.tasks.gui.guis.AssetExplorer;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;


public class AssetsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }

        if(sender instanceof Player player){
            new AssetExplorer(player).openGui(player);
            return true;
        }


        return false;
    }

    private static Component getAssetList(int page, List<Asset> foundAssets) {
        Component component = Component.text("");

        for(int i = page *8; i<Math.min(page *8+8, foundAssets.size()); i++){
            String cmnd = "/asset " + foundAssets.get(i).getCode();
            component = component.append(
                    Component.text("["+foundAssets.get(i).getCode()+ "]").color(foundAssets.get(i).getColor())
                            .clickEvent(ClickEvent.runCommand(cmnd))
                            .hoverEvent(HoverEvent.showText(Component.text(cmnd, Configuration.COLORINFO)))).appendNewline();
        }
        return component;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){

                return List.of("categories");
            }

            if(args.length >= 2){

                if (Objects.equals(args[0], "categories")) {
                    if(args.length == 2){
                        suggestions = List.of("forex", "commodity");
                    }
                }

            }

            return suggestions;
        }

    }
}
