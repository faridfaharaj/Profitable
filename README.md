# Profitable

![wideboy profitable](https://github.com/user-attachments/assets/ba556248-c80e-4241-91cd-cc5accb431d5)


## About
Profitable is a Minecraft Economy plugin that adds **real** supply and demand to the game using an exchange!

This means that prices arent pre-set, nor just go up +1 when someone buys something,
Prices are **placed by the players** by using orders, which transact whenever two orders agree on certain price range, and thus turning into the current value of the asset.


![Sin título](https://github.com/user-attachments/assets/1b4a3f2a-2f9b-4d6a-85b5-fdbfee64bdce)

# 🔖 Plugin Features

## ⭐️ Highlights
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
- **Fees (Taxes)**
- **Automatic database migration**
- **MySQL support**
- **Full Folia support**

## Integration
- **Vault**: Anything compatible with vault its compatible with Profitable
- **PlayerPoints**

<details>

<summary>Config file</summary>

```
# allows Vault currency be added as an asset, withdrawn or deposited to your account
vault-support: true

# allows PlayerPoints currency be added as an asset, withdrawn or deposited to your account
player-points-support: true

colors: # Allows customization of command outputs and graphs
  # Color for prices going up (hex)
  bullish: "#8CD740"

  # Color for prices going down (hex)
  bearish: "#FA413B"

database:
  # orders are able to transact across worlds if false
  data-per-world: false

  # (0)-SQLite (1)-mySQL
  database-type: 0

  mysql:
    host:
    port:
    database:
    username:
    password:
    options: "?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"

exchange:
  fees: # Fixed: 23    Percentage: 23%
    # Fees when you take money from your wallet
    withdrawal-fees: 0
    # Fees when you add money to your wallet
    deposit-fees: 0

  commodities:
    fees: # Fixed: 23    Percentage: 23%
      # Fees when you transact immediately with another order
      taker-fees: 0
      # Fees when you add an order to the order book
      maker-fees: 0

      # Fees when you use the claiming tag on an entity (fixed only)
      entity-claiming-fees: 0

    # Commodities spawn on account's delivery location when transacting (commodities are added to wallet when false)
    physical-delivery: true

    # Allow taking commodities from your wallet for transactions
    take-wallet: true

    # Allow taking items from your inventory for transactions
    take-inventory: true

    # Allow taking claimed entities from world for transactions
    take-world: true

    # note: commodities are RAW and FUNGIBLE materials traded in BULK that are then processed into different products
    #       crops and building material can be commodities as well:
    #
    #       IRON IS a commodity (in real life)
    #             -All bars are the same quality, therefore interchangeable (Fungible) v
    #             -Traded in large amounts (Traded in Bulk) v
    #             -Usually gathered from the ground (raw material) v
    #
    #       DIAMOND HELMET is NOT a commodity
    #             -Enchantments, damage, and trims make each one unique (Non-Fungible) x
    #             -Usually traded one at a time (not in bulk) x
    #             -Finished product (not raw) x
    #
    #     but this is only a recommendation anyway, use this plugin however you want.
    #
    generation:
      # Allows auto-generation of assets for commodities in each world using whitelisting/blacklisting
      active: true

      # Enables whitelist and disables blacklist for items
      item-whitelisting: true
      commodity-item-whitelist:
        - COAL
        - CHARCOAL
        - IRON_INGOT
        - COPPER_INGOT
        - GOLD_INGOT
        - NETHERITE_SCRAP
        - AMETHYST_SHARD
        - DIAMOND
        - NETHER_QUARTZ
        - EMERALD
        - LAPIS_LAZULI
        - GLOWSTONE_DUST
        - REDSTONE
        - WHEAT
        - EGG
        - APPLE
        - MELON_SLICE
        - PUMPKIN
        - SWEET_BERRIES
        - GLOW_BERRIES
        - CHORUS_FRUIT
        - CARROT
        - POTATO
        - POISONOUS_POTATO
        - BEETROOT
        - FISH
        - RAW_COD
        - RAW_SALMON
        - TROPICAL_FISH
        - PUFFERFISH
        - SUGARCANE
        - KELP
        - COCOA_BEANS
        - HONEY_BOTTLE
        - SPIDER_EYE
        - ROTTEN_FLESH
        - FLINT
        - BONE
        - STRING
        - FEATHER
        - LEATHER
        - HONEYCOMB
        - INK_SAC
        - GLOW_INK_SAC
        - TURTLE_SCUTE
        - ARMADILLO_SCUTE
        - SLIME_BALL
        - CLAY_BALL
        - PRISMARINE_SHARD
        - PRISMARINE_CRYSTALS
        - NAUTILUS_SHELL
        - HEART_OF_THE_SEA
        - BLAZE_ROD
        - BREEZE_ROD
        - HEAVY_CORE
        - NETHER_STAR
        - ENDER_PEARL
        - SHULKER_SHELL
        - ECHO_SHARD
        - NETHER_WART
        - GUNPOWDER
        - DRAGON_BREATH
        - GHAST_TEAR
        - PHANTOM_MEMBRANE
        - BAMBOO
        - CACTUS
        - POWDER_SNOW_BUCKET
        - INK_SAC
        - WATER_BUCKET
        - MILK_BUCKET
        - LAVA_BUCKET
        - OAK_LOG
        - SPRUCE_LOG
        - BIRCH_LOG
        - JUNGLE_LOG
        - ACACIA_LOG
        - DARK_OAK_LOG
        - MANGROVE_LOG
        - CHERRY_LOG
        - CRIMSON_STEM
        - WARPED_STEM
        - DIRT
        - GRAVEL
        - SAND
        - COBBLESTONE
        - GRANITE
        - DIORITE
        - ANDESITE
        - OBSIDIAN
        - NETHERRACK
        - SPONGE
        - SOUL_SAND
        - SOUL_SOIL
        - ICE
        - END_STONE
        - TOTEM_OF_UNDYING
        - BROWN_MUSHROOM
        - RED_MUSHROOM

      commodity-item-blacklist:
        - BARRIER
        - COMMAND_BLOCK
        - STRUCTURE_BLOCK
        - STRUCTURE_VOID

      # Enables whitelist and disables blacklist for items
      entity-whitelisting: true
      commodity-entity-whitelist:
        - PIG
        - SHEEP
        - COW
        - CHICKEN
        - MOOSHROOM
        - VILLAGER

      # withers can be used for mining... imagine trading withers... hmmm....
      commodity-entity-blacklist:
        - ENDER_DRAGON
        - WARDEN

  forex:
    fees: # Fixed: 23    Percentage: 23%
      # Fees when you transact immediately with another order
      taker-fees: 0
      # Fees when you add an order to the order book
      maker-fees: 0

main-currency: # setting for the currency used to trade on the exchange (This can be changed in game)

  # Sets Main currency to a currency with matching code
  # If no match is found, the asset will be created using values below
  currency: EMD_Villager Emerald_#00ff00
  #
  #           <code>_<name>_<hex color>
  #
  # Example: EMD_Villager Emerald_#21ff59   (use this if you want it to look good even if it's not found)
  # Example: USD_US dollars                 (use if you want a random color)
  # Example: EUR                            (use this is you are really sure currency already exists)
  #
  # creation:
  # 3-letter codes only, example: USD ---> you have: 89.32 USD !!
  # Name, color and description are optional. (color is selected at random) (name uses code)


  # When true, if no match is found a hook currency will take its place, or create if no hooks are found
  # hooks: Vault (VLT), Player Points (PTS)
  create-last-resort-only: true

  # Initial amount of Main Currency that new players will have on their wallets by default (other than 0 may devalue your currency, use only for initial supply)
  initial-balance: 0

  # currency that will be deposited or withdrawn for every hook
  vault-currency: VLT_Vault Currency_#ffbb15
  playerpoints-currency: PTS_Player Points_#ff6d92 
```

</details>

# 📑 Orders
This plugin uses **Orders** to trade.
Orders are instructions to sell or buy an asset (item, entity, currency, etc...) under specified conditions

## 📗 Market Order

A **Market order** lets you transact immediately with the best available price in the order book.


![Untitled video - Made with Clipchamp (1)](https://github.com/user-attachments/assets/79305223-eb12-4910-af62-429dc131a6dd)

## 📘 Limit Order

**Limit Orders** lets you choose the price you want, but may not execute right away as it needs someone else to agree on your price, Players can place an order to trade stuff at the price they think fair, actually influencing the market.

![Untitled video - Made with Clipchamp](https://github.com/user-attachments/assets/c091b8f5-9f20-44d2-bd6f-17b3ca0171b3)

## 📕 Stop-Limit Order

A **Stop-Limit Order** becomes a **Limit Order** once the market reaches the specified trigger price

Useful if you think Prices will keep going up after a certain price

![bii](https://github.com/user-attachments/assets/79c4bc07-290e-42e7-a194-05c332c7d328)




# Market Data & Analysis

Since Profitable is a plugin designed to simulate real trading, which includes speculation and market data tracking,
Players can monitor **prices**, **price movements**, **liquidity**, **supply**.

Even some nice fancy candle graphs, so players can predict and profit off of speculation.

![2025-04-20_20 30 33](https://github.com/user-attachments/assets/7a7d318c-c17d-4f68-b403-386a3527d711)


# Why?

**Put it like this**, 

> Imagine you have a server where two groups of players start fighting, they start buying a lot of diamonds and netherite for gear so people start selling it a bit > more expensive every time because they have to go farther and farther to find them, so **prices start rising**.
> 
> now our friend **johnny** here, notices this and buys 100 diamonds with all his balance expecting prices to go even higher,
> 
> Sadly the leader of one of these groups gets banned a day later so they stop fighting, and now everyone has so many and the **price is so high** people don't want to buy anymore. 
> And those who do, **want it cheaper**, so every time someone sells a diamond, it transacts with a cheaper order, so **prices start to fall**.
> 
> johnny has now a bunch of useless diamonds and no money,
> however, if **prices had gone up** as he expected **he'd be rich**
 
Profitable makes this kind of scenarios possible, it lets everyone experience the actual depth of a real market. It's not just a store like many linear price adjustment systems that have led to **broken economies** due to farms and the rising number of raids, 
  
**Profitable** not only makes that add to the fun, but prices adjust **themselves**. 
Because you’re not just buying and selling; you’re participating in an **actual economy** where people and events are behind prices.

I hope with that I have convinced you to download this wonderful plugin, have a great day.


# Final notes

This is an early version of the plugin. Future updates will bring more refined features and improvements.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/V7V110GP3T)
