package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.util.MessagingUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Objects;

public class HelpCommand implements CommandExecutor {

    String[] pages =
            {
                    """
> /help
§e Sends a list of commands you can use §r
-----
> /help admin
§e Sends a list of commands meant for administrators and ops §r
-----
> /sell <Asset> <Units>
§e Sends an order to sell an asset immediately at the lowest price §r
-----
> /sell <Asset> <Units> <Price>
§e Sends an order to sell an asset at an specified price §r
-----
> /sell <Asset> <Units> <Price> stop-limit
§e Sends an order that turns into a limit order when market reaches its price §r
-----
> /buy <Asset> <Units>
§e Sends an order to buy an asset immediately at the lowest price §r
-----
> /buy <Asset> <Units> <Price>
§e Sends an order to buy an asset at an specified price §r
-----
> /buy <Asset> <Units> <Price> stop-limit
§e Sends an order that turns into a limit order when market reaches its price §r
-----
> /account
§e Returns current active account §r
-----
> /account register <account> <password> <Repeat password>
§e Creates a new account §r
-----
> /account login <account> <password>
§e Changes active account §r
-----
> /account logout
§e logs out of active account §r
-----
> /account password <Old password> <New password>
§e Changes active account's password §r
-----
> /account delete <account> <password>
§e Deletes current active account §r
-----
> /wallet
§e Displays all asset balances on your account §r
-----
> /wallet deposit <Asset> <amount>
§e Transfers desired amount to your profitable's account wallet §r
-----
> /wallet withdraw <Asset> <amount>
§e Transfers desired amount from your wallet to you §r
-----
> /orders
§e Displays all active orders on your account §r
-----
> /delivery
§e Displays delivery locations of your account §r
-----
> /delivery set entity
§e Lets you select the block where bought entities spawn §r
-----
> /delivery set item
§e Lets you select the container where bought items go §r
-----
> /claimtag
§e Sends you a name-tag to mark entities as yours §r
-----
> /top TOP
§e Displays the top 9 best performing assets (Monthly MC time) §r
-----
> /top HOT
§e Displays the top 9 assets with more % price movement (Monthly MC time) §r
-----
> /top LIQUID
§e Displays the top 9 most traded assets (Monthly MC time) §r
-----
> /top BIG
§e Displays the top 9 most expensive assets (Monthly MC time) §r
-----
> /assets categories commodity
§e Displays all assets from type commodity §r
-----
> /assets categories currency
§e Displays all assets from type currency §r
-----
> /asset <Asset> graph <Time frame>
§e Gives you a map containing a candles graph showcasing price movements across a certain time frame §r""", """
> /admin config reload
§e Reloads and updates most config changes §r
-----
> /admin getplayeracc <player>
§e shows player's current active account §r
-----
> /admin account <account> wallet
§e shows account asset balances §r
-----
> /admin account <account> wallet <asset> <amount>
§e allows you to set an account's asset balance to a specific amount §r
-----
> /admin account <account> passwordreset
§e turns an account's password into '1234' for recovery §r
-----
> /admin account <account> orders
§e shows all active orders on the account §r
-----
> /admin account <account> delivery
§e shows delivery locations on the account §r
-----
> /admin account <account> delivery setitem <x> <y> <z> <world name>
§e allows you to set an account's item delivery location §r
-----
> /admin account <account> delivery setentity <x> <y> <z> <world name>
§e allows you to set an account's entity delivery location §r
-----
> /admin account <account> claimid
§e shows the nametag name that recognizes someone's owned entities §r
-----
> /admin account <account> delete
§e deletes an account §r
-----
> /admin orders findbyasset <asset>
§e shows all orders from a specific asset §r
-----
> /admin orders getbyid <ID> cancel
§e cancels an order, giving collateral back to owner §r
-----
> /admin orders getbyid <ID> delete
§e deletes an order §r
-----
> /admin orders deleteall
§e deletes all existing orders §r
-----
> /admin orders cancelall
§e cancels all existing orders (warning: may be heavy) §r
-----
> /admin orders newlimitorder <asset> <price> <units>
§e creates a limit order from thin air (useful for asset's initial supply) §r
-----
> /admin assets
§e shows all registered assets §r
-----
> /admin assets register commodityEntity <code>
§e allows you to enable trading for a specific entity (won't account for multiple worlds if not in config) §r
-----
> /admin assets register commodityItem <code>
§e allows you to enable trading for a specific item (won't account for multiple worlds if not in config) §r
-----
> /admin assets register currency <code>
§e allows you to create a currency to trade in the exchange §r
-----
> /admin assets fromid <asset> delete
§e deletes a certain asset (will come back if in config) §r
-----
> /admin assets fromid <asset> newtransaction <price> <volume>
§e fakes transactions to make the illusion of market movement on an asset §r
-----
> /admin assets fromid <asset> edit <New symbol> <New name> <New Color>
§e Allows you to edit the appearance and identifier of registered assets §r
-----
> /admin assets fromid <asset> resettransactions
§e deletes all records of transactions from an asset (this kills graphs) §r"""

            };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        int page;

        if(args.length == 1){

            if(Objects.equals(args[0], "admin")){
                page = 1;
            }else{
                page = 0;
            }

        }else{
            page = 0;
        }

        MessagingUtil.sendCustomMessage(sender,
                MessagingUtil.profitableTopSeparator("Help", "-------------------").appendNewline()
                        .append(Component.text(pages[page])).appendNewline()
                        .append(MessagingUtil.profitableBottomSeparator())
        );

        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
            return List.of("admin");
        }

    }
}
