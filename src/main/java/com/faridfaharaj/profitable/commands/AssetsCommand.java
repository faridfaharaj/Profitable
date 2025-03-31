package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;


public class AssetsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0){
            return false;
        }

        if(Objects.equals(args[0], "categories")){

            if(!sender.hasPermission("profitable.asset.categories")){
                TextUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 2){
                TextUtil.sendError(sender, "/assets category <Asset Category>");
                return true;
            }

            int assetType;
            if(args[1].equals("forex")){
                assetType = 1;
            }else if(args[1].equals("commodity")){

                List<Asset> foundAssets = Assets.getAssetFancyType(2);
                foundAssets.addAll(Assets.getAssetFancyType(3));

                int page;

                if(args.length == 2){
                    page = 0;
                }else {
                    try {
                        page = Integer.parseInt(args[2]);
                    }catch (Exception e){
                        TextUtil.sendError(sender, "Invalid page number");
                        return true;
                    }
                }

                int totalPages = (foundAssets.size()-1)/7;
                Component component = TextUtil.profitableTopSeparator().appendNewline()
                        .append(getAssetList(page, foundAssets, "Tradeable commodities:")).appendNewline()
                        .append(TextUtil.profitablePaginator(page, totalPages, "/assets categories " + args[1]))
                        ;

                TextUtil.sendCustomMessage(sender, component);

                return true;

            }else{

                TextUtil.sendError(sender, "Invalid Asset Type");
                return true;

            }

            List<Asset> foundAssets = Assets.getAssetFancyType(assetType);

            int page;

            if(args.length == 2){
                page = 0;
            }else {
                try {
                    page = Integer.parseInt(args[2]);
                }catch (Exception e){
                    TextUtil.sendError(sender, "Invalid page number");
                    return true;
                }
            }

            int totalPages = (foundAssets.size()-1)/7;
            Component component = TextUtil.profitableTopSeparator().appendNewline()
                    .append(getAssetList(page, foundAssets, "Tradeable " + TextUtil.nameType(assetType) + ":")).appendNewline()
                    .append(TextUtil.profitablePaginator(page, totalPages, "/assets categories " + args[1]))
                    ;

            TextUtil.sendCustomMessage(sender, component);

            return true;
        }

        return false;
    }

    private static Component getAssetList(int page, List<Asset> foundAssets, String cat) {
        Component component = Component.text(cat);

        for(int i = page *7; i<Math.min(page *7+7, foundAssets.size()); i++){
            String cmnd = "/asset " + foundAssets.get(i).getCode();
            component = component.appendNewline().append(
                    Component.text("["+foundAssets.get(i).getCode()+ "]").color(foundAssets.get(i).getColor())
                            .clickEvent(ClickEvent.runCommand(cmnd))
                            .hoverEvent(HoverEvent.showText(Component.text(cmnd, Configuration.COLORINFO))));
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
