package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.tables.Assets;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
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

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }

        if(Objects.equals(args[0], "categories")){

            if(!sender.hasPermission("profitable.asset.categories")){
                MessagingUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 2){
                MessagingUtil.sendError(sender, "/assets category <Asset Category>");
                return true;
            }

            int assetType;
            if(args[1].equals("forex")){
                assetType = 1;
            }else if(args[1].equals("commodity")){

                List<Asset> foundAssets = Assets.getAssetFancyType(2);
                foundAssets.addAll(Assets.getAssetFancyType(3));

                if(foundAssets.isEmpty()){
                    MessagingUtil.sendEmptyNotice(sender, "No commodities can be traded");
                    return true;
                }

                int page;

                if(args.length == 2){
                    page = 0;
                }else {
                    try {
                        page = Integer.parseInt(args[2]);
                    }catch (Exception e){
                        MessagingUtil.sendError(sender, "Invalid page number");
                        return true;
                    }
                }

                int totalPages = (foundAssets.size()-1)/8;
                Component component = MessagingUtil.profitableTopSeparator("Commodities", "--------------").appendNewline()
                        .append(getAssetList(page, foundAssets))
                        .append(MessagingUtil.profitablePaginator(page, totalPages, "/assets categories " + args[1]))
                        ;

                MessagingUtil.sendCustomMessage(sender, component);

                return true;

            }else{

                MessagingUtil.sendError(sender, "Invalid Asset Type");
                return true;

            }

            List<Asset> foundAssets = Assets.getAssetFancyType(assetType);
            if(foundAssets.isEmpty()){
                MessagingUtil.sendEmptyNotice(sender, "No "+ NamingUtil.nameType(assetType) +" can be traded");
                return true;
            }

            int page;

            if(args.length == 2){
                page = 0;
            }else {
                try {
                    page = Integer.parseInt(args[2]);
                }catch (Exception e){
                    MessagingUtil.sendError(sender, "Invalid page number");
                    return true;
                }
            }

            int totalPages = (foundAssets.size()-1)/8;
            Component component = MessagingUtil.profitableTopSeparator(NamingUtil.nameType(assetType), "-----------------").appendNewline()
                    .append(getAssetList(page, foundAssets))
                    .append(MessagingUtil.profitablePaginator(page, totalPages, "/assets categories " + args[1]));
            MessagingUtil.sendCustomMessage(sender, component);

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
