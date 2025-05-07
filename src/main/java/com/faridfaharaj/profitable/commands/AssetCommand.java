package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssetCommand  implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(args.length == 0){
            return false;
        }

        if(!sender.hasPermission("profitable.asset.info")){
            MessagingUtil.sendGenericMissingPerm(sender);
            return true;
        }

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }

        if(args.length == 1){

            if(Objects.equals(args[0], Configuration.MAINCURRENCYASSET.getCode())) {
                MessagingUtil.sendError(sender, "This is the Main currency");
                return true;
            }

            if(sender instanceof Player player){
                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    TemporalItems.sendInfoBook(player, args[0]);
                });
            }
            return true;
        }

        if (args[1].equals("graph")) {

            if(sender instanceof Player player){

                if(!sender.hasPermission("profitable.asset.graphs")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(Objects.equals(args[0], Configuration.MAINCURRENCYASSET.getCode())) {
                    MessagingUtil.sendError(sender, "This is the Main currency");
                    return true;
                }

                if (args.length == 2) {

                    sendGraphOptions(player, args[0]);

                    return true;
                }

                long timeFrame;

                switch (args[2]) {
                    case "1M":
                        timeFrame = 720000;
                        break;

                    case "3M":
                        timeFrame = 2160000;
                        break;

                    case "6M":
                        timeFrame = 4320000;
                        break;

                    case "1Y":
                        timeFrame = 8760000;
                        break;

                    case "2Y":
                        timeFrame = 17520000;
                        break;

                    default:
                        MessagingUtil.sendError(sender, "Invalid time frame");
                        return true;


                }

                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    TemporalItems.sendGraphMap(player, args[0], timeFrame, args[2]);
                });
                return true;

            }


        }else{
            MessagingUtil.sendError(sender, "/asset <asset> graph <Time frame>");
            return true;
        }

        return false;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();
            if(args.length == 1){
                List<String> options = new ArrayList<>(Configuration.ALLOWEITEMS);
                options.addAll(Configuration.ALLOWENTITIES);
                if(VaultHook.isConnected()){
                    options.add(VaultHook.getAsset().getCode());
                }
                if(PlayerPointsHook.isConnected()){
                    options.add(PlayerPointsHook.getAsset().getCode());
                }

                StringUtil.copyPartialMatches(args[0], options, suggestions);
            }else if(args.length == 2){
                suggestions = List.of("graph");
            }else if(args.length == 3){
                suggestions = List.of("1M", "3M", "6M", "1Y", "2Y");
            }


            return suggestions;

        }

    }


    public static void sendGraphOptions(Player player, String asset){

        String[] durations = {"1M", "3M", "6M", "1Y", "2Y"};
        String[] durationsText = {"1 Month", "3 Months", "6 Months", "1 Year", "2 Years"};

        Component component = MessagingUtil.profitableTopSeparator("Graphs", "-----------------").appendNewline()
                .append(Component.text("Graphs for " + asset + " (In Minecraft time):")).appendNewline();
        for (int i = 0; i < durations.length; i++) {
            String cmnd = "/asset " + asset + " graph " + durations[i];
            component = component.append(Component.text("[" + durationsText[i] + "]", Configuration.COLORINFO).clickEvent(ClickEvent.runCommand(cmnd)).hoverEvent(HoverEvent.showText(Component.text(cmnd, Configuration.COLORINFO)))).appendNewline();
        }
        component = component.append(MessagingUtil.profitableBottomSeparator());

        MessagingUtil.sendCustomMessage(player, component);
    }

}
