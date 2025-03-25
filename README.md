#### Profitable
Profitable is a Minecraft plugin that adds real supply and demand to the game by introducing an exchange system designed to be as close to reality as possible.

A part of this pluggin was designed so people can learn a thing or two about trading and investments while playing this nice game, so realism and ease of use are core principles of this plugin :D.

# Features

- Multi-asset wallets (Multi-asset meaning multiple currencies as of now)
- Vault compatibility, If vault is present it can be used to deposit or withdraw funds from wallet
- Player-driven prices
- Instant transactions at the best price using orders
- Partial fills
- Offline transactions
- Entity trading!
- Spot market for items and entities (Commodities)
- Currency trading (Forex)
- Graphs

# About
Profitable adds a quick and authentic way for players to buy and sell stuff on your server.

**This plugins uses *Orders* to trade**,

Orders are instructions to sell or buy an asset (item, entity, currency, etc..) under specified conditions

For example:
- **Limit order** lets you choose prize, but may not execute right away
- **Market order** acts inmediatly at the lowest available prize

Profitable will find the best prices in the order book, filling your order with as many existing orders as needed or just adding it to the book if no match is found;
However, for you, it only takes a simple command.

By using a single:

``/sell <Asset> limit <units> <price>``
or 
``/buy <Asset> limit <units> <price>``

Players can place an order to trade stuff at the price they think fair, 
Then, they simply wait for someone else to transact.

Players can even trade instantly at the best available price by using:

``/sell <Asset> market <units>``
or 
``/buy <Asset> market <units>``

Ensuring they get the asset inmediatly.

*Market Data & Analysis*

Since profitable its a plugin that simulates real trading, it includes speculation and market data tracking, wich of course you can do.

Players can monitor price movements, liquidity, supply, and more stuff using:

``/asset peek <asset>``

Even some nice candle graphs to display prices.

# Compatibility

This plugin can work with vault, meaning its compatible with anything compatible with vault! (this can be configured too)

# Final notes

This is an early version of the plugin. Future updates will bring more refined features and improvements.
