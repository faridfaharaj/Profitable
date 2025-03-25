## Profitable
Profitable is a Minecraft pluggin that adds real supply and demand to minecraft, by adding an exchange that attempts to be as close to the real thing as possible.

A part of this pluggin was designed so people can learn a thing or two about trading and investments while playing the best game of all times, so realism and ease of use is core part of this plugin :D.

# Features

- Multi-asset wallets (Multi-asset meaning multiple currencies as of now)
- Vault compatibility, If vault is present it can be used to deposit or withdraw funds from wallet
- Player driven prices
- Instant transactions at the best price using orders
- Partial fills
- Offline transactions
- Entity trading!
- Spot market for items and entities (Commodities)
- Currency trading (Forex)

# About
Profitable adds a quick and realistic way for players to buy and sell anything you want on your server.

This plugins uses *Orders* to trade,

Orders are instructions to sell or buy an asset (item, entity, currency, etc..) under specified conditions

For instance:
- Limit order lets you choose prize, but may not execute right away
- Market order acts inmediatly at the lowest available prize

Profitable will find the best prices among the order book, and fill your order with as many other orders needed;
but for you, it only takes one command.

By using a single:

``/sell <Asset> limit <units> <price>``
or 
``/buy <Asset> limit <units> <price>``

players can place an order to trade stuff at the price they think fair, 
and just wait for some one else to transact.

Players can even trade instantly at the best available price by using:

``/sell <Asset> market <units>``
or 
``/buy <Asset> market <units>``

Ensuring they get the asset inmediatly.

Profitable its a plugin that simulates trading, wich includes speculation and... of course... *data*

Players can watch price movements, liquidity, supply, demand and everything you need to actually trade.

``/asset peek <asset>``



# Compatibility

This plugin can work with vault, meaning its compatible with anything compatible with vault! (this can be configured too)

# Final notes

This is a rather primitive version of this plugin, update will come soon, with better and more refined features.
