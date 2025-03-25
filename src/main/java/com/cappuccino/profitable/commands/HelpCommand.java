package com.cappuccino.profitable.commands;

import com.cappuccino.profitable.util.NamingUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class HelpCommand implements CommandExecutor {

    String[] pages =
            {
                    """
> /buy <Asset> <Order type> <Units> <Price>
§e Sends an order to the exchange to buy an asset §r
|
> /sell <Asset> <Order type> <Units> <Price>
§e Sends an order to the exchange to sell an asset §r
|
|
> /account
§e Returns current active account §r
|
> /account register <account> <password> <Repeat password>
§e Creates a new account §r
|
> /account login <account> <password>
§e Changes active account §r
|
> /account logout
§e logs out of active account §r
|
> /account password <Old password> <New password>
§e Changes active account's password §r
|
> /account delete <account> <password>
§e Deletes current active account §r
|
> /account wallet
§e Displays all asset balances on your account §r
|
> /account orders
§e Displays all active orders on your account §r
|
> /account delivery
§e Displays delivery locations of your account §r
|
> /account delivery set entity
§e Lets you select the block where bought entities spawn §r
|
> /account delivery set item
§e Lets you select the container where bought items go §r
|
> /account claim
§e Sends you a name tag to mark entities as yours §r
|
> /account deposit <amount>
§e Transfers desired amount from vault to your profitable's account wallet §r
|
> /account withdraw <amount>
§e Transfers desired amount from your wallet to vault §r
|
|
> /assets TOP
§e Displays the top 9 best performing assets (Monthly MC time) §r
|
> /assets HOT
§e Displays the top 9 assets with more % price movement (Monthly MC time) §r
|
> /assets LIQUID
§e Displays the top 9 most traded assets (Monthly MC time) §r
|
> /assets BIG
§e Displays the top 9 most expensive assets (Monthly MC time) §r
|
> /assets categories commodity
§e Displays all assets from type commodity §r
|
> /assets categories currency
§e Displays all assets from type currency §r
|
> /assets peek <Asset>
§e Gives you a written book with market information about specified asset §r
|
> /assets peek <Asset> graph <Time frame>
§e Gives you a map containing a candles graph showcasing price movements across a certain time frame §r
"""
            };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        int page;

        if(args.length == 2){

            try{
                page = Integer.parseInt(args[1]);
            }catch (Exception e){
                sender.sendMessage(ChatColor.RED+"Invalid page number");
                return true;
            }

            if(page > pages.length){
                sender.sendMessage(ChatColor.RED+"Invalid page number");
                return true;
            }
        }else{
            page = 0;
        }

        sender.sendMessage(NamingUtil.profitableTopSeparator());
        sender.sendMessage(pages[page]);
        sender.sendMessage(NamingUtil.profitableBottomSeparator());

        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
            return List.of();
        }

    }
}
