package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.tasks.TemporalItems;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeliveryCommand implements CommandExecutor {


    @Override
    public boolean onCommand( CommandSender sender,  Command command,  String s,  String[] args) {

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }
        if(sender instanceof Player player){

            String account = Accounts.getAccount(player);

            if(args.length < 1){

                if(!sender.hasPermission("profitable.account.info.delivery")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                Profitable.getfolialib().getScheduler().runAsync(task -> {
                    Location entityDelivery = Accounts.getEntityDelivery(account);
                    Location itemDelivery = Accounts.getItemDelivery(account);

                    MessagingUtil.sendCustomMessage(sender,
                            Component.newline()
                                    .append(Component.text("Item Delivery Location:",Configuration.COLORTEXT)).appendNewline()
                                    .appendSpace().appendSpace().append(Component.text((itemDelivery == null?"Not set":itemDelivery.toVector() + " (" + itemDelivery.getWorld().getName()+")")).color(Configuration.COLORHIGHLIGHT)).appendNewline()
                                    .append(Component.text("Entity Delivery Location:",Configuration.COLORTEXT)).appendNewline()
                                    .appendSpace().appendSpace().append(Component.text((entityDelivery == null?"Not set":entityDelivery.toVector() + " (" + entityDelivery.getWorld().getName()+")")).color(Configuration.COLORHIGHLIGHT))
                                    .appendNewline()
                    );
                });

                return true;
            }

            if(args[0].equals("set")){

                if(!sender.hasPermission("profitable.account.manage.setdelivery")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 2){
                    MessagingUtil.sendError(player, "/account delivery set item OR /account delivery set entity");
                    return true;
                }

                if(args[1].equals("item")){

                    TemporalItems.sendDeliveryStick(player, true);

                    return true;

                } else if (args[1].equals("entity")) {
                    TemporalItems.sendDeliveryStick(player, false);

                    return true;
                }else{
                    MessagingUtil.sendError(player, "Invalid delivery");

                }

                return true;


            }else{
                MessagingUtil.sendError(player, "/account delivery set item OR /account delivery set entity");
            }

        }

        return false;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){
                suggestions = List.of("set");
            }

            if(args.length == 2 && Objects.equals(args[0], "set")){
                suggestions = List.of("item", "entity");
            }
            return suggestions;

        }

    }

}
