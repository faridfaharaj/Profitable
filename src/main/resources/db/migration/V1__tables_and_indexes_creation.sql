CREATE TABLE IF NOT EXISTS assets
(
    world BINARY(16),
    asset_id VARCHAR(20),
    asset_type INT,
    meta BLOB,

    PRIMARY KEY(world ,asset_id)
);

CREATE INDEX idx_asset_type ON assets (world, asset_type);




CREATE TABLE IF NOT EXISTS accounts
(
    world BINARY(16),
    account_name VARCHAR(36),
    password BINARY(16),
    salt BINARY(16),
    item_delivery_pos BINARY(40),
    entity_delivery_pos BINARY(40),
    entity_claim_id INT,

    PRIMARY KEY (world, account_name)
);
CREATE INDEX idx_entity_claim_id ON accounts(entity_claim_id DESC);



CREATE TABLE IF NOT EXISTS account_assets
(
    world BINARY(16),
    account_name VARCHAR(36),
    asset_id VARCHAR(20),
    quantity DOUBLE,

    PRIMARY KEY (world, account_name, asset_id),

    FOREIGN KEY (world, asset_id) REFERENCES assets(world, asset_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (world, account_name) REFERENCES accounts(world, account_name) ON DELETE CASCADE
);
CREATE INDEX idx_account_asset_lookup ON account_assets (world, account_name);




CREATE TABLE IF NOT EXISTS orders
(
    world BINARY(16),
    order_uuid BINARY(16),
    owner VARCHAR(36),
    asset_id VARCHAR(20),
    sideBuy BOOLEAN,
    price DOUBLE,
    units DOUBLE,
    order_type TINYINT,

    PRIMARY KEY(world, order_uuid),

    FOREIGN KEY (world, asset_id) REFERENCES assets(world, asset_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (world, owner) REFERENCES accounts(world, account_name) ON DELETE CASCADE
);
CREATE INDEX idx_order_uuid ON orders(world, order_uuid);
CREATE INDEX idx_orders_owner ON orders (world, owner);
CREATE INDEX idx_orders_best_price ON orders (world, asset_id, sideBuy, price, order_type);
CREATE INDEX idx_orders_stop_update ON orders(world, order_type, price);




CREATE TABLE IF NOT EXISTS candles_day
(
    world BINARY(16),
    time BIGINT,
    open DOUBLE,
    close DOUBLE,
    high DOUBLE,
    low DOUBLE,
    volume DOUBLE,
    asset_id VARCHAR(20),

    PRIMARY KEY (world, time, asset_id),

    FOREIGN KEY (world, asset_id) REFERENCES assets(world, asset_id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_candles_lookup ON candles_day (world, asset_id, time DESC);

CREATE TABLE IF NOT EXISTS candles_week
(
    world BINARY(16),
    time BIGINT,
    open DOUBLE,
    close DOUBLE,
    high DOUBLE,
    low DOUBLE,
    volume DOUBLE,
    asset_id VARCHAR(20),

    PRIMARY KEY (world, time, asset_id),

    FOREIGN KEY (world, asset_id) REFERENCES assets(world, asset_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS candles_month
(
    world BINARY(16),
    time BIGINT,
    open DOUBLE,
    close DOUBLE,
    high DOUBLE,
    low DOUBLE,
    volume DOUBLE,
    asset_id VARCHAR(20),

    PRIMARY KEY (world, time, asset_id),

    FOREIGN KEY (world, asset_id) REFERENCES assets(world, asset_id) ON DELETE CASCADE ON UPDATE CASCADE
);