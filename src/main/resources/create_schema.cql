CREATE KEYSPACE IF NOT EXISTS STATION_SYSTEM WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Train(tid text, tname text, sid text, PRIMARY KEY (tid));

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Station(sid text, sname text, PRIMARY KEY (sid));

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Station_Warehouse(sid text, cname text, lid text, cw int, PRIMARY KEY ((sid, cname), lid));

CREATE TABLE IF NOT EXISTS STATION_SYSTEM.Train_Load(tid text, cname text, lid text, cw int, PRIMARY KEY ((tid, cname), lid));
