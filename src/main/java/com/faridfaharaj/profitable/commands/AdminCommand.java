package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Lang;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.holderClasses.assets.Asset;
import com.faridfaharaj.profitable.data.holderClasses.assets.ComEntity;
import com.faridfaharaj.profitable.data.holderClasses.assets.ComItem;
import com.faridfaharaj.profitable.data.holderClasses.assets.Currency;
import com.faridfaharaj.profitable.data.tables.*;
import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.*;

public class AdminCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {


        if (sender instanceof Player player) {

            if (args.length == 0) {
                return false;
            }

            if(Objects.equals(args[0], "config")){

                if(args.length == 1){
                    MessagingUtil.sendSyntaxError(sender, "/admin config <Sub Command>");
                }

                if(Objects.equals(args[1], "reloadconfig")){

                    if(!sender.hasPermission("profitable.admin.config.reloadconfig")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    Configuration.reloadConfig(Profitable.getInstance());
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.reload-config.reloaded"));
                }else{
                    MessagingUtil.sendGenericInvalidSubCom(sender, args[1]);
                    return true;
                }

                if(Configuration.MULTIWORLD){
                    for(World world:Profitable.getInstance().getServer().getWorlds()){
                        Assets.generateAssets(world);
                    }
                }

                return true;
            }

            if (Objects.equals(args[0], "assets")) {

                if (args.length > 1) {
                    if (Objects.equals(args[1], "add")) {

                        if (!sender.hasPermission("profitable.admin.assets.manage.register")) {
                            MessagingUtil.sendGenericMissingPerm(sender);
                            return true;
                        }

                        if (args.length < 3) {
                            MessagingUtil.sendSyntaxError(sender, "/admin add <Asset type> <Symbol>");
                            return true;
                        }


                        String assetid = args[3].toUpperCase();
                        TextColor color = TextColor.fromHexString(args[4]);
                        if (color == null) {
                            color = NamedTextColor.YELLOW;
                        }

                        StringBuilder name = new StringBuilder();
                        for (int i = 5; i < args.length; i++) {
                            name.append(args[i]).append(" ");
                        }
                        if(name.isEmpty()){
                            name.append(args[3]);
                        }

                        ItemStack stack = player.getInventory().getItemInMainHand();
                        if(stack.isEmpty() || stack.getType() == Material.AIR){
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.add.error.main-hand-empty",
                                    Map.entry("%type%", args[2])
                            ));
                            return true;
                        }

                        Asset asset;
                        switch (args[2]) {
                            case "currency":

                                asset = new Currency(assetid, color, name.toString(), stack);

                                break;
                            case "commodityitem":

                                asset = new ComItem(assetid, color, name.toString(), stack);

                                break;
                            case "commodityentity":
                                EntityType entityType = EntityType.fromName(assetid);
                                if(entityType == null){
                                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.add.error.entity-name-unmatch"));
                                    return true;
                                }

                                asset = new ComEntity(assetid, color, name.toString(), stack);

                                break;
                            default:
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.add.error.invalid-asset-type",
                                        Map.entry("%type%", args[2])
                                        ));
                                return true;
                        }

                        if (Assets.registerAsset(player.getWorld(), asset)) {

                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.add.success",
                                    Map.entry("%asset_type%",asset.getAssetType().name()),
                                    Map.entry("%asset%",asset.getCode())
                            ));

                        } else {
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.add.failure",
                                    Map.entry("%asset%",asset.getCode())
                            ));
                        }

                        return true;

                    }

                    if(Objects.equals(args[1], "fromid")){

                        if(args.length < 4){
                            return false;
                        }

                        if(Objects.equals(args[3], "newtransaction")){

                            if(!sender.hasPermission("profitable.admin.assets.manage.newtransaction")){
                                MessagingUtil.sendGenericMissingPerm(sender);
                                return true;
                            }

                            if(args.length < 6){
                                MessagingUtil.sendSyntaxError(sender, "/admin assets fromid " + args[2] + " newtransaction <price> <volume>");
                                return true;
                            }

                            World world;
                            if(player == null){

                                if(args.length == 6){
                                    MessagingUtil.sendSyntaxError(sender, "Must specify world on console: /admin <assets> " + args[1] + " newtransaction <price> <volume> <world>");
                                    return true;
                                }

                                world = Profitable.getInstance().getServer().getWorld(args[6]);

                            }else{
                                world = player.getWorld();
                            }


                            if(Candles.updateDay(player.getWorld(), args[2], Double.parseDouble(args[4]), Double.parseDouble(args[5]))){
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.from-id.new-transaction.success",
                                        Map.entry("%asset%",args[2])
                                ));
                            }else {
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.from-id.new-transaction.failure",
                                        Map.entry("%asset%",args[2])
                                ));
                            }

                            return true;

                        }

                        if(Objects.equals(args[3], "resettransactions")){

                            if(!sender.hasPermission("profitable.admin.assets.manage.resettransactions")){
                                MessagingUtil.sendGenericMissingPerm(sender);
                                return true;
                            }

                            Candles.assetDeleteAllCandles(player.getWorld(), args[2]);
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.from-id.new-transaction.success",
                                    Map.entry("%asset%",args[2])
                            ));

                            return true;

                        }

                        if (Objects.equals(args[3], "delete")) {

                            if(!sender.hasPermission("profitable.admin.assets.manage.delete")){
                                MessagingUtil.sendGenericMissingPerm(sender);
                                return true;
                            }

                            if(args.length < 5){
                                MessagingUtil.sendSyntaxError(sender, "/admin assets fromid <asset> delete <asset again>");
                                return true;
                            }

                            if(!Objects.equals(args[2], args[4])){
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.from-id.delete.error.confirmation-fail"));
                                return true;
                            }

                            if(Objects.equals(args[2], Configuration.MAINCURRENCYASSET.getCode())){
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.from-id.delete.error.its-main-currency"));
                                return true;
                            }

                            Asset asset = Assets.getAssetData(player.getWorld(),args[2]);
                            if(asset == null){
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("assets.error.asset-not-found", Map.entry("%asset%", args[2])));
                                return true;
                            }
                            if(Assets.deleteAsset(player.getWorld(),args[2])){
                                if(Configuration.GENERATEASSETS){

                                    if(asset.getAssetType() == Asset.AssetType.COMMODITY_ITEM){


                                        if(!Profitable.getInstance().getConfig().getBoolean("exchange.commodities.generation.item-whitelisting")){
                                            //blacklist
                                            List<String> itemlist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-item-blacklist");
                                            itemlist.add(asset.getCode());
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-item-blacklist", itemlist);
                                        }else{
                                            //whitelist
                                            List<String> itemlist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-item-whitelist");
                                            itemlist.remove(asset.getCode());
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-item-whitelist", itemlist);
                                        }
                                        Configuration.ALLOWEITEMS.remove(asset.getCode());

                                    }else if(asset.getAssetType() == Asset.AssetType.COMMODITY_ENTITY){

                                        if(!Profitable.getInstance().getConfig().getBoolean("exchange.commodities.generation.entity-whitelisting")){
                                            //blacklist
                                            List<String> entitylist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-entity-blacklist");
                                            entitylist.add(asset.getCode());
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-entity-blacklist", entitylist);
                                        }else{
                                            //whitelist
                                            List<String> entitylist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-entity-whitelist");
                                            entitylist.remove(asset.getCode());
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-entity-whitelist", entitylist);
                                        }
                                        Configuration.ALLOWENTITIES.remove(asset.getCode());
                                    }
                                    Profitable.getInstance().saveConfig();

                                }
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.from-id.delete.success",
                                        Map.entry("%asset%",args[2])
                                ));
                            }else{
                                MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.assets.from-id.delete.failure"));
                            }
                            return true;

                        }

                    }
                }


            }

            if(Objects.equals(args[0], "orders")){

                if(args.length == 1){
                    MessagingUtil.sendSyntaxError(sender, "/profitable:admin <subcommand> <args>...");
                    return true;
                }

                if(Objects.equals(args[1], "findbyasset")){

                    if(!sender.hasPermission("profitable.admin.orders.info.findbyasset")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length < 3){

                        MessagingUtil.sendSyntaxError(sender, "/profitable:admin orders findbyasset <asset>");
                        return true;
                    }

                    List<String> ordersString = new ArrayList<>();
                    List<Order> orders = Orders.getAssetOrders(player.getWorld(),args[2]);
                    for(Order order : orders){
                        ordersString.add(order.toString());
                    }

                    Component component = Component.text(Profitable.getLang().getString("admin.orders.title", Map.entry("%asset%",args[2]))).color(Configuration.COLORHIGHLIGHT).appendNewline()
                            .append(Component.text("--------------------------------------------")).appendNewline();
                    for(Order order : orders){
                        component = component.append(order.toComponent()).appendNewline()
                                .append(Component.text("["+Profitable.getLang().getString("admin.orders.cancel-button")+"] ",Configuration.COLORWARN).clickEvent(ClickEvent.runCommand("/profitable:admin orders getbyid " + order.getUuid() + " cancel")));
                    }
                    component = component.append(Component.text("--------------------------------------------"));
                    MessagingUtil.sendComponentMessage(sender, component);

                    return true;

                }

                if(Objects.equals(args[1], "getbyid")){

                    if(args.length < 3){

                        MessagingUtil.sendSyntaxError(sender, "/admin orders getbyid <ID> <Action>");
                        return true;

                    }

                    if(args.length < 4){

                        MessagingUtil.sendSyntaxError(sender, "/admin orders getbyid <ID> <Action>");
                        return true;

                    }

                    if(Objects.equals(args[3], "cancel")){

                        if(!sender.hasPermission("profitable.admin.orders.manage.cancel")){
                            MessagingUtil.sendGenericMissingPerm(sender);
                            return true;
                        }

                        Orders.cancelOrder(player.getWorld(),UUID.fromString(args[2]));
                        MessagingUtil.sendComponentMessage(player, Profitable.getLang().get("orders.cancel",
                                Map.entry("%order%", args[2]))
                        );
                        return true;

                    }

                }

                if (Objects.equals(args[1], "deleteall")) {

                    if(!sender.hasPermission("profitable.admin.orders.manage.deleteall")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    Orders.deleteAllOrders();
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.orders.delete-all"));
                    return true;

                }

                if (Objects.equals(args[1], "cancelall")) {

                    if(!sender.hasPermission("profitable.admin.orders.manage.cancelall")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    List<Order> orders = Orders.getAllOrders();

                    for(Order order : orders){
                        Orders.cancelOrder(player.getWorld(),order.getUuid());
                    }

                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.orders.cancel-all"));

                    return true;

                }

                if (Objects.equals(args[1], "newlimitorder")) {

                    if(!sender.hasPermission("profitable.admin.orders.manage.newlimitorder")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length < 6){
                        MessagingUtil.sendSyntaxError(sender, "/admin orders newlimitorder <asset> <side> <price> <units>");

                        return true;
                    }

                    boolean sidebuy = Objects.equals(args[3], "buy");

                    double units;
                    double price;

                    try{

                        units = Double.parseDouble(args[4]);

                    } catch (Exception e) {
                        MessagingUtil.sendGenericInvalidAmount(sender,args[4]);
                        return true;
                    }

                    try{

                        price = Double.parseDouble(args[5]);

                    } catch (Exception e) {
                        MessagingUtil.sendGenericInvalidAmount(sender,args[5]);
                        return true;
                    }


                    if(player != null){
                        if(Orders.insertOrder(player.getWorld(),UUID.randomUUID(), "server", args[2], sidebuy, price, units, Order.OrderType.LIMIT)){
                            MessagingUtil.sendComponentMessage(player,Profitable.getLang().get("exchange.new-order-notice",
                                            Map.entry("%order_type%", Order.OrderType.LIMIT.toString().replace("_","-").toLowerCase()),
                                            Map.entry("%side%", sidebuy?
                                                    Profitable.getLang().getString("orders.sides.buy"):
                                                    Profitable.getLang().getString("orders.sides.sell")),
                                            Map.entry("%base_asset_amount%", "<white>"+args[2] + " " + units+"</white>"),
                                            Map.entry("%quote_asset_amount%", MessagingUtil.assetAmmount(Configuration.MAINCURRENCYASSET, price))
                                    )
                            );
                        }else{
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("assets.error.asset-not-found",
                                    Map.entry("%asset%", args[2])
                            ));
                        }

                        return true;
                    }

                }

                MessagingUtil.sendGenericInvalidSubCom(sender, args[1]);
                return true;

            }




            String account;

            if(Objects.equals(args[0], "getplayeracc")){

                if(!sender.hasPermission("profitable.admin.accounts.info.getplayeracc")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 2){
                    MessagingUtil.sendSyntaxError(sender, "/admin getplayeracc <player>");
                    return true;
                }

                Player gotPlayer = Profitable.getInstance().getServer().getPlayer(args[1]);
                if(gotPlayer == null){
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.get-player-account.player-not-found",
                            Map.entry("%player%", args[1])
                    ));
                    return true;
                }

                account = Accounts.getAccount(gotPlayer);
                if(args.length == 2){
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.get-player-account.display",
                            Map.entry("%player%", args[1]),
                            Map.entry("%account%", account)
                    ));
                    return true;
                }

            }else if (Objects.equals(args[0], "account")){

                if(args.length < 3){
                    MessagingUtil.sendSyntaxError(sender, "/admin account <account> <subcommand> <args>");
                    return false;
                }
                account = args[1];

            }else {
                return false;
            }


            if(Objects.equals(args[2], "set")){

                if(args.length < 5){
                    MessagingUtil.sendSyntaxError(sender,"/admin account <Account> set <Asset> <Amount>");
                    return true;
                }

                if(!sender.hasPermission("profitable.admin.accounts.manage.wallet")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                Double ammount;
                try{
                    ammount = Double.parseDouble(args[4]);
                }catch (Exception e){
                    MessagingUtil.sendGenericInvalidAmount(sender, args[4]);
                    return true;
                }


                if(AccountHoldings.setHolding(player.getWorld(),account, args[3], ammount)){
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.account.set.success",
                            Map.entry("%account%", account),
                            Map.entry("%amount%", String.valueOf(ammount)),
                            Map.entry("%asset%", args[3])
                    ));
                }else{
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.account.set.failure",
                            Map.entry("%account%", account),
                            Map.entry("%asset%", args[3])
                    ));
                }
                return true;
            }

            /*
            if(Objects.equals(args[2], "wallet")){

                if(args.length == 3){

                    if(!sender.hasPermission("profitable.admin.accounts.info.wallet")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    MessagingUtil.sendCustomMessage(sender,
                            MessagingUtil.profitableTopSeparator("Wallet","------------------").appendNewline()
                                    .append(AccountHoldings.AssetBalancesToString( account)).appendNewline()
                                    .append(MessagingUtil.profitableBottomSeparator())
                    );

                    return true;
                }

                if(args.length < 5){
                    MessagingUtil.sendSyntaxError(sender,"/admin account <Account> wallet <Asset> <Amount>");
                    return true;
                }

                if(!sender.hasPermission("profitable.admin.accounts.manage.wallet")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                Double ammount;
                try{
                    ammount = Double.parseDouble(args[4]);
                }catch (Exception e){
                    MessagingUtil.sendError(sender,"Invalid ammount");
                    return true;
                }


                if(AccountHoldings.setHolding(account, args[3], ammount)){
                    MessagingUtil.sendSuccsess(sender,"Set " + args[3]+ " to "+ ammount + ", on " + account + "'s wallet");
                }else{
                    MessagingUtil.sendError(sender, "Could not add " + args[3]);
                }
                return true;
            }

            /*
            if(Objects.equals(args[2], "orders")){

                if(!sender.hasPermission("profitable.admin.accounts.info.orders")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                List<Order> orders = Orders.getAccountOrders(account);
                if(orders.isEmpty()){
                    MessagingUtil.sendEmptyNotice(sender, "No active orders on this account");
                }else {


                    Component component = Component.text("Showing all active orders on account " + account + ":").color(Configuration.COLORHIGHLIGHT).appendNewline()
                            .append(Component.text("--------------------------------------------")).appendNewline();
                    for(Order order : orders){
                        component = component.append(order.toComponent()).appendNewline()
                                .append(Component.text("[Cancel] ",Configuration.COLORWARN).clickEvent(ClickEvent.runCommand("/profitable:admin orders getbyid " + order.getUuid() + " cancel")).hoverEvent(HoverEvent.showText(Component.text("Cancel this Order and give back collateral to owner"))))
                                .append(Component.text("[Delete]",NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/profitable:admin orders getbyid " + order.getUuid() + " delete")).hoverEvent(HoverEvent.showText(Component.text("Delete this order, no compensation")))).appendNewline()

                        ;
                    }
                    component = component.append(Component.text("--------------------------------------------"));


                    MessagingUtil.sendCustomMessage(sender, component);
                }
                return true;
            }

            if(Objects.equals(args[2], "delivery")){

                if(args.length == 3){

                    if(!sender.hasPermission("profitable.admin.accounts.info.delivery")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    Location entityDelivery = Accounts.getEntityDelivery(account);
                    Location itemDelivery = Accounts.getItemDelivery(account);

                    MessagingUtil.sendCustomMessage(sender,
                            Component.text("Delivery " + account + ":").color(Configuration.COLORHIGHLIGHT).appendNewline()
                                    .append(Component.text("--------------------------------------------")).appendNewline()
                                    .append(Component.text("Item Delivery Location:").color(Configuration.COLORTEXT)).appendNewline()
                                    .append(Component.text(itemDelivery == null?"Not set":itemDelivery.toVector() + " (" + itemDelivery.getWorld().getName()+")")).appendNewline()
                                    .appendNewline()
                                    .append(Component.text("Entity Delivery Location:").color(Configuration.COLORTEXT)).appendNewline()
                                    .append(Component.text(entityDelivery == null?"Not set":entityDelivery.toVector() + " (" + entityDelivery.getWorld().getName()+")")).appendNewline()
                                    .append(Component.text("--------------------------------------------"))


                    );

                    return true;
                }

                if(args.length < 7){

                    MessagingUtil.sendSyntaxError(sender, "/admin account <account> delivery setitem <x> <y> <z> <world (optional)>");

                    return true;
                }

                if(!sender.hasPermission("profitable.admin.accounts.manage.delivery")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                World world;
                if(args.length == 7){
                    if(player != null){
                        world = player.getWorld();
                    } else {
                        MessagingUtil.sendError(sender, "Must specify world when running command from console");
                        return true;
                    }
                }else {
                    world = Profitable.getInstance().getServer().getWorld(args[7]);
                }

                if(world == null){

                    MessagingUtil.sendError(sender, "Invalid world");

                    return true;
                }

                double x,y,z;

                try{
                    x = Double.parseDouble(args[4]);
                    y = Double.parseDouble(args[5]);
                    z = Double.parseDouble(args[6]);
                }catch (Exception e){
                    MessagingUtil.sendError(sender,"Invalid coordinates");
                    return  true;
                }

                Location location = new Location(world, x, y, z);

                if(Objects.equals(args[3], "setitem")){

                    if(Accounts.changeItemDelivery(account, location)){
                        MessagingUtil.sendSuccsess(sender,"changed " + account + " item delivery to:" + location.toVector());
                    }else {
                        MessagingUtil.sendError(sender, "couldn't change item delivery location");
                    }


                }

                if(Objects.equals(args[3], "setentity")){

                    if(Accounts.changeEntityDelivery(account, location)){
                        MessagingUtil.sendSuccsess(sender, "changed " + account + " entity delivery to:" + location.toVector());
                    }else {
                        MessagingUtil.sendError(sender, "couldn't change entity delivery location");
                    }

                }

                return true;

            }

            if(Objects.equals(args[2], "claimid")){

                if(!sender.hasPermission("profitable.admin.accounts.info.claimid")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                String claimId = Accounts.getEntityClaimId(account);
                if(claimId != null){
                    MessagingUtil.sendCustomMessage(sender, Component.text(account + "'s Entity claim id: ", Configuration.GUICOLORTEXT).append(Component.text(claimId).color(Configuration.COLORHIGHLIGHT)));
                }else{
                    MessagingUtil.sendError(sender, "Could not get this claim id");
                }

                return true;

            }

             */

            if(Objects.equals(args[2], "delete")){

                if(!sender.hasPermission("profitable.admin.accounts.manage.delete")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 4){

                    MessagingUtil.sendSyntaxError(sender, "/admin " + args[0] + " <account> delete <account_name> <account_name>");

                    return true;
                }

                if(Objects.equals(account, args[3])){
                    if(Accounts.getCurrentAccounts().containsValue(account)){
                        MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("account.error.cant-delete-active-account"));
                    }else {
                        if(Accounts.deleteAccount(player.getWorld(),account)){
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.account.delete.success",
                                    Map.entry("%account%",account)
                            ));
                        }else {
                            MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.account.delete.failure",
                                    Map.entry("%account%",account)
                            ));
                        }
                    }
                }else {
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.account.delete.error.confirmation-fail"));
                }

                return true;

            }

            if(Objects.equals(args[2], "passwordreset")){

                if(Objects.equals(args[1], "server")){
                    MessagingUtil.sendSyntaxError(sender, "Not a good idea");
                    return true;
                }

                if(!sender.hasPermission("profitable.admin.accounts.manage.passwordreset")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(Accounts.changePassword(player.getWorld(),account, "1234")){
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.account.password-reset.success",
                            Map.entry("%account%",account)
                    ));
                }else {
                    MessagingUtil.sendComponentMessage(sender, Profitable.getLang().get("admin.account.password-reset.failure",
                            Map.entry("%account%",account)
                    ));
                }



                return true;
            }
        }

        return false;
    }


    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {


            List<String> suggestions = new ArrayList<>();

            if (args.length == 1) {
                suggestions = List.of("account", "orders", "assets", "getplayeracc","forcelogout", "config");
            }

            if(Objects.equals(args[0], "getplayeracc") || Objects.equals(args[0], "forcelogout")){
                if (args.length == 2) {
                    return null;
                }
            }

            if(Objects.equals(args[0], "config")){
                if (args.length == 2) {
                    suggestions = List.of("reloadconfig");
                }
            }

            if(Objects.equals(args[0], "account") || Objects.equals(args[0], "getplayeracc")){
                if (args.length == 2) {

                    if(Objects.equals(args[0], "getplayeracc")){
                        return null;
                    }else {
                        suggestions = List.of("[<account>]");
                    }

                }

                if(args.length > 2){
                    if (args.length == 3) {
                        List<String> options = List.of("passwordreset","set","delete");

                        StringUtil.copyPartialMatches(args[2], options, suggestions);
                    }

                    if(args.length > 3){

                        if(Objects.equals(args[2], "delete")){

                            if(args.length == 4){
                                suggestions = List.of("[<Account>]");
                            }

                        }

                        if(Objects.equals(args[2], "set")){

                            if(args.length == 4){
                                suggestions = List.of("[<Asset>]");

                            }

                            if(args.length == 5){
                                suggestions = List.of("[<Amount>]");

                            }

                        }

                        if(Objects.equals(args[2], "delivery")){

                            if(args.length == 4){
                                suggestions = List.of("setitem", "setentity");

                            }

                            if(args.length == 5){
                                suggestions = List.of("[<x>]");
                            }

                            if(args.length == 6){
                                suggestions = List.of("[<y>]");
                            }

                            if(args.length == 7){
                                suggestions = List.of("[<z>]");
                            }

                            if(args.length == 8){
                                suggestions = List.of("[<world name>]");
                            }

                        }
                    }
                }

            }

            if(Objects.equals(args[0], "orders")){
                if (args.length == 2) {
                    List<String> options = List.of("findbyasset", "getbyid", "deleteall", "cancelall", "newlimitorder");

                    StringUtil.copyPartialMatches(args[1], options, suggestions);
                }

                if(args.length > 2){
                    if (Objects.equals(args[1], "newlimitorder")) {

                        if(args.length == 3){
                            suggestions = List.of("[<Asset>]");
                        }

                        if(args.length == 4){
                            suggestions = List.of("buy", "sell");
                        }

                        if(args.length == 5){
                            suggestions = List.of("[<Units>]");
                        }

                        if(args.length == 6){
                            suggestions = List.of("[<Price>]");
                        }
                    }

                    if (Objects.equals(args[1], "findbyasset")){

                        if(args.length == 3){
                            suggestions = List.of("[<Asset>]");
                        }

                    }

                    if (Objects.equals(args[1], "getbyid")) {

                        if(args.length == 3){
                            suggestions = List.of("[<ID>]");
                        }

                        if(args.length == 4){
                            suggestions = List.of("cancel", "delete");
                        }

                    }


                }

            }

            if(Objects.equals(args[0], "assets")){

                if (args.length == 2) {
                    List<String> options = List.of("add", "fromid");

                    StringUtil.copyPartialMatches(args[1], options, suggestions);
                }

                if(args.length > 2){

                    if (Objects.equals(args[1], "add")) {

                        if(args.length == 3){
                            suggestions = List.of("commodityentity", "commodityitem", "currency");
                        }

                        if(args.length == 4){
                            suggestions = List.of("[<Symbol>]");
                        }

                        if(args.length == 5){
                            suggestions = List.of("[<Hex Color>]");
                        }

                        if(args.length >= 6){
                            suggestions = List.of("[<Name_word_"+(args.length-5)+">]");
                        }

                    }

                    if (Objects.equals(args[1], "fromid")) {

                        if(args.length == 3){
                            suggestions = List.of("[<Asset>]");
                        }

                        if(args.length == 4){
                            suggestions = List.of("delete", "newtransaction", "resettransactions");
                        }

                        if(args.length > 4){
                            if(Objects.equals(args[3], "newtransaction")){

                                if(args.length == 5){
                                    suggestions = List.of("[<price>]");
                                }

                                if(args.length == 6){
                                    suggestions = List.of("[<volume>]");
                                }

                            }

                            if(Objects.equals(args[3], "delete")){

                                if(args.length == 5){
                                    suggestions = List.of("[<Asset again>]");
                                }

                            }
                        }

                    }

                }

            }


            return suggestions;

        }
    }

}
