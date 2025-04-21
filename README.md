# Profitable

![wideboy profitable](https://github.com/user-attachments/assets/ba556248-c80e-4241-91cd-cc5accb431d5)


## About
Profitable is a Minecraft Economy plugin that adds **real** supply and demand to the game using an exchange!

This means that prices arent pre-set, nor just go up +1 when someone buys something,
Prices are **placed by the players** by using orders, which transact whenever two orders agree on certain price range, and thus turning into the current value of the asset.

# 🔖 New features - `0.2.0-beta`
## ⭐️ Highlights
- **Stop-Limit Orders**
- **Fees (Taxes)**
- **MySQL support**
- **PlayerPoints support**
- **Automatic database migration**
- **Multiple Order Complexity Levels**
    - Market order
      - `/buy <Asset>`
      - `/buy <Asset> <Units>`
    - Limit order
      - `/buy <Asset> <Units> <Price>`
    - Stop-Limit order
      - `/buy <Asset> <Units> <Price> stop-limit`
- **Extra customization on configuration file**
    - Asset customization
    - Exchange Colors
    - Fees (fixed and percentage)
    - Transact items and entities without delivery
- **Command to reload configuration**
- **Console update notice**

## 📑 Limit-Order

A **Stop-Limit Order** becomes a **Limit Order** once the market reaches the specified trigger price

``/buy <Asset> <Units> <Price> stop-limit`` or ``/sell <Asset> <Price> stop-limit``

Useful if you think Prices will keep going up after a certain price

![bii](https://github.com/user-attachments/assets/79c4bc07-290e-42e7-a194-05c332c7d328)

## ⚠️ Important

This Update had major changes on the way databases are managed, meaning databases from pre 0.2.0 versions will only migrate partially as **Orders and Multi-World data cannot be migrated**, cancel all orders and back-up your data **before** runing this new version.

# 📌 Features

## ⭐️ Core Features
- **Player-driven prices**
- **Instant transactions at the best price using orders**
- **Partial fills**
- **Multi-asset wallets**
- **Offline transactions**
- **Entity trading**
- **Item trading**
- **Currency trading (Forex)**
- **Advanced trading**
- **Candle graphs display prices**

## 🪝 Hooks
- **Vault**: Anything compatible with vault its compatible with Profitable
- **PlayerPoints**

# 📑 Orders
This plugin uses **Orders** to trade.
Orders are instructions to sell or buy an asset (item, entity, currency, etc...) under specified conditions

## 📗 Market Order

A **Market order** lets you transact immediately with the best available price in the order book.

``/buy <Asset>``
``/sell <Asset>``

or

``/buy <Asset> <Units>``
``/sell <Asset> <Units>``

![Untitled video - Made with Clipchamp (1)](https://github.com/user-attachments/assets/79305223-eb12-4910-af62-429dc131a6dd)

## 📘 Limit Order

**Limit Orders** lets you choose the price you will transact, but may not execute right away as it needs some other order to agree on the price waiting on the order book, Players can place an order to trade stuff at the price they think fair, actually influencing the market.

``/buy <Asset> <Units> <Price>`` or ``/sell <Asset> <Units> <Price>``

![Untitled video - Made with Clipchamp](https://github.com/user-attachments/assets/c091b8f5-9f20-44d2-bd6f-17b3ca0171b3)




# 📊 Market Data & Analysis

Since Profitable is a plugin designed to simulate real trading, which includes speculation and market data tracking,
Players can monitor **prices**, **price movements**, **liquidity**, **supply**, and more stuff using a single command:

``/asset <asset>``

Even some nice fancy candle graphs, so players can predict and profit off of speculation.

![2025-04-20_20 30 33](https://github.com/user-attachments/assets/7a7d318c-c17d-4f68-b403-386a3527d711)


# 💸 Why?

**Put it like this**, 

Imagine you have a server where two groups of players start fighting, they start buying a lot of diamonds and netherite for gear so people start selling it a bit more expensive every time because they have to go farther and farther to find them, so **prices start rising**.

now our friend **johnny** here, notices this and buys 100 diamonds with all his balance expecting prices to go even higher,

Sadly the leader of one of these groups gets banned a day later so they stop fighting, and now everyone has so many and the **price is so high** people don't want to buy anymore. 
And those who do, **want it cheaper**, so every time someone sells a diamond, it transacts with a cheaper order, so **prices start to fall**.

johnny has now a bunch of useless diamonds and no money,
however, if **prices had gone up** as he expected **he'd be rich**

Profitable makes this kind of scenarios possible, it lets everyone experience the actual depth of a real market. It's not just a store like many linear price adjustment systems that have led to **broken economies** due to farms and the rising number of raids, 
 
**Profitable** not only makes that add to the fun, but prices adjust **themselves**. 
Because you’re not just buying and selling; you’re participating in an **actual economy** where people and events are behind prices.
With this plugin You can outsmart other players, invest, predict trends, and see how real markets work by playing a game.


I hope with that I have convinced you to download this wonderful plugin, have a great day.


# Final notes

This is an early version of the plugin. Future updates will bring more refined features and improvements.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/V7V110GP3T)
