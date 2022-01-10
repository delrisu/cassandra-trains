CREATE KEYSPACE IF NOT EXISTS STATION_SYSTEM WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Train
(
    train_id   text,
    train_name text,
    station_id text,
    PRIMARY KEY (train_id)
);

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Station
(
    station_id   text,
    station_name text,
    PRIMARY KEY (station_id)
);

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Station_Warehouse
(
    station_id       text,
    commodity_name   text,
    log_id           text,
    commodity_weight int,
    PRIMARY KEY ((station_id, commodity_name), log_id)
);

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Train_Load
(
    train_id         text,
    commodity_name   text,
    log_id           text,
    commodity_weight int,
    PRIMARY KEY ((train_id, commodity_name), log_id)
);
