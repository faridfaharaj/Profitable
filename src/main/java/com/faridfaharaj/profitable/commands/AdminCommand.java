package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.Profitable;
import com.faridfaharaj.profitable.data.DataBase;
import com.faridfaharaj.profitable.data.holderClasses.Asset;
import com.faridfaharaj.profitable.data.holderClasses.Order;
import com.faridfaharaj.profitable.data.tables.*;
import com.faridfaharaj.profitable.hooks.PlayerPointsHook;
import com.faridfaharaj.profitable.hooks.VaultHook;
import com.faridfaharaj.profitable.util.MessagingUtil;
import com.faridfaharaj.profitable.util.NamingUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.*;

public class AdminCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        /*

        Player player = null;
        if(sender instanceof Player got){
            player = got;
        }

        if(Configuration.MULTIWORLD){
            DataBase.universalUpdateWorld(sender);
        }

        if(args.length == 0){
            return false;
        }

        if(Objects.equals(args[0], "forcelogout")){

            if(!sender.hasPermission("profitable.admin.accounts.manage.forcelogout")){
                MessagingUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 2){
                MessagingUtil.sendSyntaxError(sender, "/admin forcelogout <player>");
                return true;
            }

            Player gotPlayer = Profitable.getInstance().getServer().getPlayer(args[1]);
            if(gotPlayer == null){
                MessagingUtil.sendError(sender, args[1] + " isn't online");
                return true;
            }

            UUID playerid = player.getUniqueId();
            if(!Accounts.getCurrentAccounts().containsKey(playerid)){
                MessagingUtil.sendError(sender, "No active account found");
                return true;
            }
            Accounts.logOut(playerid);

            MessagingUtil.sendSuccsess(sender, "Logged "+ player.getName() + " out");
            return true;
        }

        if(Objects.equals(args[0], "config")){

            if(args.length == 1){
                MessagingUtil.sendError(sender, "/admin config <property>");
            }

            if(Objects.equals(args[1], "reloadconfig")){

                if(!sender.hasPermission("profitable.admin.config.reloadconfig")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                Configuration.reloadConfig(Profitable.getInstance());
                MessagingUtil.sendSuccsess(sender, "Successfully reloaded config file");
                MessagingUtil.sendWarning(sender, "Some properties require restarting the server");
            }else{
                MessagingUtil.sendGenericInvalidSubCom(sender, args[1]);
                return true;
            }

            if(Configuration.MULTIWORLD){
                for(World world:Profitable.getInstance().getServer().getWorlds()){
                    try {
                        DataBase.updateWorld(world);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Assets.generateAssets();
                }
            }

            return true;
        }


        if(Objects.equals(args[0], "assets")){

            if(args.length == 1){

                if(!sender.hasPermission("profitable.admin.assets.info.getallassets")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                MessagingUtil.sendCustomMessage(sender,

                        Component.text("Showing all registered assets in Profitable:").color(Configuration.COLORHIGHLIGHT).appendNewline()
                                .append(Component.text("--------------------------------------------")).appendNewline()
                                .append(Component.text(Assets.getAll().toString()).color(Configuration.COLORTEXT)).appendNewline()
                                .append(Component.text("--------------------------------------------"))


                );

                return true;
            }

            if(Objects.equals(args[1], "register")){

                if(!sender.hasPermission("profitable.admin.assets.manage.register")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 4){

                    MessagingUtil.sendError(sender, "/admin register currency <Asset Type> <Symbol>");

                    return true;
                }


                String asset = args[3].toUpperCase();

                switch (args[2]){
                    case "currency":

                        if(asset.length() > 3){
                            MessagingUtil.sendError(sender, "Currencies must only have 3 letters");
                            return true;
                        }

                        String generator = asset;

                        if(args.length > 4){
                            generator += "_" + args[4].replace("_", " ");
                        }

                        if(args.length > 5){
                            generator += "_" + args[5];
                        }


                        try {

                            if(Assets.registerAsset(asset, 1, Asset.metaData(Asset.StringToCurrency(generator)))){
                                MessagingUtil.sendSuccsess(sender, "Registered: " + asset);
                            }else{
                                MessagingUtil.sendError(sender, "There is already an asset with Symbol: " + asset);
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                    case "commodityitem":

                        if(Material.getMaterial(asset) != null){

                            try {
                                if(Assets.registerAsset(asset, 2, Asset.metaData(Configuration.COLOREMPTY.value(), NamingUtil.nameCommodity(asset)))){

                                    if(Configuration.GENERATEASSETS){
                                        if(!Profitable.getInstance().getConfig().getBoolean("exchange.commodities.generation.item-whitelisting")){
                                            //blacklist
                                            List<String> itemlist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-item-blacklist");
                                            itemlist.remove(asset);
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-item-blacklist", itemlist);
                                        }else{
                                            //whitelist
                                            List<String> itemlist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-item-whitelist");
                                            itemlist.add(asset);
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-item-whitelist", itemlist);
                                        }
                                        Configuration.ALLOWEITEMS.add(asset);
                                        Profitable.getInstance().saveConfig();
                                    }

                                    MessagingUtil.sendSuccsess(sender, "Registered: " + asset);

                                }else{
                                    MessagingUtil.sendError(sender, asset + " is already registered");
                                }
                            } catch (IOException e) {
                                MessagingUtil.sendError(sender, "Error registering " + asset);
                                throw new RuntimeException(e);
                            }

                        }else{
                            MessagingUtil.sendError(sender, "Commodities must come from an existing item");
                            return true;
                        }

                        break;
                    case "commodityentity":


                        EntityType entity = EntityType.fromName(asset);
                        if(entity != null){

                            Class<?> entityClass = entity.getEntityClass();
                            if (!LivingEntity.class.isAssignableFrom(entityClass) || entity.name().equals("PLAYER")) {
                                MessagingUtil.sendError(sender, "Invalid asset");
                                return true;
                            }

                            try {
                                if(Assets.registerAsset(asset, 3, Asset.metaData(Configuration.COLOREMPTY.value(), NamingUtil.nameCommodity(asset)))){
                                    if(Configuration.GENERATEASSETS){
                                        if(!Profitable.getInstance().getConfig().getBoolean("exchange.commodities.generation.entity-whitelisting")){
                                            //blacklist
                                            List<String> entitylist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-entity-blacklist");
                                            entitylist.remove(asset);
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-entity-blacklist", entitylist);
                                        }else{
                                            //whitelist
                                            List<String> entitylist = Profitable.getInstance().getConfig().getStringList("exchange.commodities.generation.commodity-entity-whitelist");
                                            entitylist.add(asset);
                                            Profitable.getInstance().getConfig().set("exchange.commodities.generation.commodity-entity-whitelist", entitylist);
                                        }
                                        Configuration.ALLOWENTITIES.add(asset);

                                        Profitable.getInstance().saveConfig();
                                    }
                                    MessagingUtil.sendSuccsess(sender, "Registered: " + asset);
                                }else{
                                    MessagingUtil.sendError(sender, asset + " is already registered");
                                    return true;
                                }
                            } catch (IOException e) {
                                MessagingUtil.sendError(sender, "Error");
                                return true;
                            }

                        }else{
                            MessagingUtil.sendError(sender, "Commodities must come from an existing entity");
                        }

                        break;
                    default:
                        MessagingUtil.sendError(sender, asset + "Invalid asset type");
                        break;
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
                        MessagingUtil.sendError(sender, "/admin assets fromid " + args[2] + " newtransaction <price> <volume>");
                        return true;
                    }

                    World world;
                    if(player == null){

                        if(args.length == 6){
                            MessagingUtil.sendError(sender, "Must specify world on console: /admin <assets> " + args[1] + " newtransaction <price> <volume> <world>");
                            return true;
                        }

                        world = Profitable.getInstance().getServer().getWorld(args[6]);

                    }else{
                        world = player.getWorld();
                    }


                    if(Candles.updateDay(args[2], world, Double.parseDouble(args[4]), Double.parseDouble(args[5]))){
                        MessagingUtil.sendSuccsess(sender, "inserted transaction in " + args[2]);
                    }else {
                        MessagingUtil.sendError(sender, "Could not insert transaction on " + args[2]);
                    }

                    return true;

                }

                if(Objects.equals(args[3], "resettransactions")){

                    if(!sender.hasPermission("profitable.admin.assets.manage.resettransactions")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    Candles.assetDeleteAllCandles(args[2]);
                    MessagingUtil.sendSuccsess(sender, "Wiped all " + args[2] + "'s transactions");

                    return true;

                }

                if (Objects.equals(args[3], "delete")) {

                    if(!sender.hasPermission("profitable.admin.assets.manage.delete")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length < 5){
                        MessagingUtil.sendError(sender, "/admin assets fromid <asset> delete <asset again>");
                        return true;
                    }

                    if(!Objects.equals(args[2], args[4])){
                        MessagingUtil.sendError(sender, "Assets don't match");
                        return true;
                    }

                    if(Objects.equals(args[2], Configuration.MAINCURRENCYASSET.getCode())){
                        MessagingUtil.sendError(sender, "Cannot remove main currency");
                        return true;
                    }

                    Asset asset = Assets.getAssetData(args[2]);
                    if(asset == null){
                        MessagingUtil.sendError(sender, "This asset does not exist");
                        return true;
                    }
                    if(Assets.deleteAsset(args[2])){
                        if(Configuration.GENERATEASSETS){

                            if(asset.getAssetType() == 2){


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

                            }else if(asset.getAssetType() == 3){

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
                        MessagingUtil.sendCustomMessage(sender, Component.text("DELETED " + args[2], NamedTextColor.RED));
                    }else{
                        MessagingUtil.sendError(sender, "Could not delete that asset");
                    }
                    return true;

                }

                if(Objects.equals(args[3], "edit")){

                    if(!sender.hasPermission("profitable.admin.assets.manage.edit")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(args.length == 4){
                        MessagingUtil.sendError(sender, "/admin assets fromid <Asset> edit <New symbol> <New name> <New hexcolor>");
                        return true;
                    }

                    String code = args[4].toUpperCase();
                    Asset asset = Assets.getAssetData(args[2]);

                    if(asset == null){
                        MessagingUtil.sendError(sender, "Couldn't find asset: " + args[2]);
                        return true;
                    }

                    if(asset.getAssetType() == 3 || asset.getAssetType() == 2){
                        MessagingUtil.sendError(sender, "Cannot edit commodities");
                        return true;
                    }

                    if(!Objects.equals(args[4], asset.getCode())){
                        if(Assets.getAssetData(args[4]) != null){
                            MessagingUtil.sendError(sender, "There is already an asset with Symbol: " + args[4]);
                            return true;
                        }
                    }

                    if(args[4].length() > 3){
                        MessagingUtil.sendError(sender, "Currencies must only have 3 letters");
                        return true;
                    }

                    String name;
                    if(args.length > 5){
                        name = args[5];
                    }else{
                        name = asset.getName();
                    }

                    TextColor color = null;
                    if(args.length > 6){
                        color = TextColor.fromHexString(args[6]);
                    }
                    if(color == null){
                        color = asset.getColor();
                    }

                    if(Objects.equals(asset.getCode(), Configuration.MAINCURRENCYASSET.getCode())){
                        MessagingUtil.sendError(sender, "Cannot edit the main currency");
                        return true;
                    }

                    if(Objects.equals(asset.getCode(), VaultHook.getAsset().getCode())){
                        MessagingUtil.sendError(sender, "Cannot edit Vault output currency, Change on config!");
                        return true;
                    }

                    if(Objects.equals(asset.getCode(), PlayerPointsHook.getAsset().getCode())){
                        MessagingUtil.sendError(sender, "Cannot edit the PlayerPoints output currency, Change on config!");
                        return true;
                    }

                    if(Assets.updateAsset(asset.getCode(), new Asset(code, asset.getAssetType(), color, name))){
                        MessagingUtil.sendSuccsess(sender, "Updated " + args[4]);
                    }else {
                        MessagingUtil.sendError(sender, "Couldn't edit this asset");
                    }

                }

            }

        }

        if(Objects.equals(args[0], "orders")){

            if(args.length == 1){
                MessagingUtil.sendError(sender, "/profitable:admin <subcommand> <args>...");
                return true;
            }

            if(Objects.equals(args[1], "findbyasset")){

                if(!sender.hasPermission("profitable.admin.orders.info.findbyasset")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 3){

                    MessagingUtil.sendError(sender, "/profitable:admin orders findbyasset <asset>");
                    return true;
                }

                List<String> ordersString = new ArrayList<>();
                List<Order> orders = Orders.getAssetOrders(args[2]);
                for(Order order : orders){
                    ordersString.add(order.toString());
                }

                Component component = Component.text("Showing all active orders for " + args[2] + ":").color(Configuration.COLORHIGHLIGHT).appendNewline()
                        .append(Component.text("--------------------------------------------")).appendNewline();
                for(Order order : orders){
                    component = component.append(order.toComponent()).appendNewline()
                            .append(Component.text("[Cancel] ",Configuration.COLORWARN).clickEvent(ClickEvent.runCommand("/profitable:admin orders getbyid " + order.getUuid() + " cancel")).hoverEvent(HoverEvent.showText(Component.text("Cancel this Order and give back collateral to owner"))))
                            .append(Component.text("[Delete]",NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/profitable:admin orders getbyid " + order.getUuid() + " delete")).hoverEvent(HoverEvent.showText(Component.text("Delete this order, no compensation")))).appendNewline()

                    ;
                }
                component = component.append(Component.text("--------------------------------------------"));
                MessagingUtil.sendCustomMessage(sender, component);

                return true;

            }

            if(Objects.equals(args[1], "getbyid")){

                if(args.length < 3){

                    MessagingUtil.sendError(sender, "/admin orders getbyid <ID> <Action>");
                    return true;

                }

                if(args.length < 4){

                    MessagingUtil.sendError(sender, "/admin orders getbyid <ID> <Action>");
                    return true;

                }

                if(Objects.equals(args[3], "cancel")){

                    if(!sender.hasPermission("profitable.admin.orders.manage.cancel")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(Orders.cancelOrder(UUID.fromString(args[2]))){
                        MessagingUtil.sendSuccsess(sender,"Canceled: "+ args[2]);
                        return true;
                    }else{
                        MessagingUtil.sendError(sender, "Couldn't cancel that order");
                        return true;
                    }

                }

                if(Objects.equals(args[3], "delete")){

                    if(!sender.hasPermission("profitable.admin.orders.manage")){
                        MessagingUtil.sendGenericMissingPerm(sender);
                        return true;
                    }

                    if(Orders.deleteOrder(UUID.fromString(args[2]))){
                        MessagingUtil.sendCustomMessage(sender, Component.text("DELETED order " + args[2], NamedTextColor.RED));
                    }else {
                        MessagingUtil.sendError(sender, "Couldn't delete that order");
                    }

                    return true;

                }

            }

            if (Objects.equals(args[1], "deleteall")) {

                if(!sender.hasPermission("profitable.admin.orders.manage.deleteall")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(Orders.deleteAllOrders()){
                    MessagingUtil.sendCustomMessage(sender, Component.text("DELETED all orders from all assets", NamedTextColor.RED));
                }else{
                    MessagingUtil.sendError(sender, "Couldn't find any");
                }
                return true;

            }

            if (Objects.equals(args[1], "cancelall")) {

                if(!sender.hasPermission("profitable.admin.orders.manage.cancelall")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                List<Order> orders = Orders.getAllOrders();

                if(orders.isEmpty()){

                    MessagingUtil.sendError(sender, "Couldn't find any");

                }else{

                    for(Order order : orders){
                        Orders.cancelOrder(order.getUuid());
                    }

                    MessagingUtil.sendSuccsess(sender, "Cancelled all orders from all assets");
                }

                return true;

            }

            if (Objects.equals(args[1], "newlimitorder")) {

                if(!sender.hasPermission("profitable.admin.orders.manage.newlimitorder")){
                    MessagingUtil.sendGenericMissingPerm(sender);
                    return true;
                }

                if(args.length < 6){
                    MessagingUtil.sendError(sender, "/admin orders newlimitorder <asset> <side> <price> <units>");

                    return true;
                }

                boolean sidebuy = Objects.equals(args[3], "buy");

                double units;
                double price;

                try{

                    units = Double.parseDouble(args[4]);
                    price = Double.parseDouble(args[5]);

                } catch (Exception e) {
                    MessagingUtil.sendError(sender, "Invalid Price/Units");
                    return true;
                }


                if(player != null){
                    if(Orders.insertOrder(UUID.randomUUID(), "server", args[2], sidebuy, price, units, Order.OrderType.LIMIT)){
                        MessagingUtil.sendSuccsess(sender, "Inserted new limit order " + (sidebuy?"buy":"sell") + " " + units + " " + args[2] + " at $" + price + " on server's account");
                    }else{
                        MessagingUtil.sendError(sender, "Couldn't add order for " + args[2]);
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
                MessagingUtil.sendError(sender, "/admin getplayeracc <player>");
                return true;
            }

            Player gotPlayer = Profitable.getInstance().getServer().getPlayer(args[1]);
            if(gotPlayer == null){
                MessagingUtil.sendError(sender, args[1] + " isn't online");
                return true;
            }

            account = Accounts.getAccount(gotPlayer);
            if(args.length == 2){

                MessagingUtil.sendCustomMessage(sender, Component.text(player.getName() + "'s active account is: " + account, Configuration.GUICOLORTEXT));
                return true;
            }

        }else if (Objects.equals(args[0], "account")){

            if(args.length < 3){
                MessagingUtil.sendError(sender, "/admin account <account> <subcommand> <args>");
                return false;
            }
            account = args[1];

        }else {
            return false;
        }

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
                MessagingUtil.sendError(sender,"/admin account <Account> wallet <Asset> <Amount>");
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

        if(Objects.equals(args[2], "passwordreset")){

            if(Objects.equals(args[1], "server")){
                MessagingUtil.sendError(sender, "Not a good idea");
                return true;
            }

            if(!sender.hasPermission("profitable.admin.accounts.manage.passwordreset")){
                MessagingUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(Accounts.changePassword(account, "1234")){
                MessagingUtil.sendSuccsess(sender,account+ "'s password set to '1234' for recovery");
            }else {
                MessagingUtil.sendError(sender, "Account couldn't be found");
            }



            return true;
        }

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

                MessagingUtil.sendError(sender, "/admin account <account> delivery setitem <x> <y> <z> <world (optional)>");

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

        if(Objects.equals(args[2], "delete")){

            if(!sender.hasPermission("profitable.admin.accounts.manage.delete")){
                MessagingUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 4){

                MessagingUtil.sendError(sender, "Must write account name again as confirmation");

                return true;
            }

            if(Objects.equals(account, args[3])){
                if(Accounts.getCurrentAccounts().containsValue(account)){
                    MessagingUtil.sendError(sender, "Someone is still using this account");
                }else {
                    if(Accounts.deleteAccount(account)){
                        MessagingUtil.sendCustomMessage(sender, Component.text("DELETED account: " + account, NamedTextColor.RED));
                    }else {
                        MessagingUtil.sendError(sender, "Couldnt delete " + account);
                    }
                }
            }else {
                MessagingUtil.sendError(sender, "Account names don't match");
            }

            return true;

        }

        return false;
        */
        return false;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
            /*
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
                        List<String> options = List.of("wallet", "passwordreset", "orders", "delivery", "claimid", "delete");

                        StringUtil.copyPartialMatches(args[2], options, suggestions);
                    }

                    if(args.length > 3){

                        if(Objects.equals(args[2], "delete")){

                            if(args.length == 4){
                                suggestions = List.of("[<Account>]");
                            }

                        }

                        if(Objects.equals(args[2], "wallet")){

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
                    List<String> options = List.of("register", "fromid");

                    StringUtil.copyPartialMatches(args[1], options, suggestions);
                }

                if(args.length > 2){

                    if (Objects.equals(args[1], "register")) {

                        if(args.length == 3){
                            suggestions = List.of("commodityentity", "commodityitem", "currency");
                        }

                        if(args.length == 4){
                            suggestions = List.of("[<Symbol>]");
                        }

                        if(args.length == 5){
                            suggestions = List.of("[<Name>]");
                        }

                        if(args.length == 6){
                            suggestions = List.of("[<Hex Color>]");
                        }

                    }

                    if (Objects.equals(args[1], "fromid")) {

                        if(args.length == 3){
                            suggestions = List.of("[<Asset>]");
                        }

                        if(args.length == 4){
                            suggestions = List.of("delete", "newtransaction", "resettransactions", "edit");
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

                            if(Objects.equals(args[3], "edit")){

                                if(args.length == 5){
                                    suggestions = List.of("[<New symbol>]");
                                }

                                if(args.length == 6){
                                    suggestions = List.of("[<New name>]");
                                }

                                if(args.length == 7){
                                    suggestions = List.of("[<New color>]");
                                }

                            }
                        }

                    }

                }

            }

            return suggestions;

             */
            return null;
        }
    }

}
