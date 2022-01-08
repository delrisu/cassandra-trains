package backend;

import com.datastax.driver.core.*;

public class BackendSession {

    private Session session;

    public Session getSession() {
        return session;
    }

    public BackendSession(String contactPoint, String keyspace) throws BackendException {
        Cluster cluster = Cluster.builder().addContactPoint(contactPoint)
                .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM)).build();
        try {
            session = cluster.connect(keyspace);
        } catch (Exception e){
            throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
        }
        prepareStatements();
    }


    public static PreparedStatement GET_TRAINS;
    public static PreparedStatement GET_TRAIN;
    public static PreparedStatement INSERT_TRAIN;
    public static PreparedStatement DELETE_TRAIN;
    public static PreparedStatement GET_TRAIN_LOAD_WEIGHT_BY_TYPE;
    public static PreparedStatement INSERT_TRAIN_LOAD_BY_TYPE;
    public static PreparedStatement GET_STATIONS;
    public static PreparedStatement GET_STATION;
    public static PreparedStatement INSERT_STATION;
    public static PreparedStatement DELETE_STATION;
    public static PreparedStatement GET_WAREHOUSE_COMMODITY_WEIGHT_BY_TYPE;
    public static PreparedStatement INSERT_WAREHOUSE_COMMODITY_BY_TYPE;
    public static PreparedStatement UPDATE_TRAIN_STATION;

    private void prepareStatements() throws BackendException {
        try{
            GET_TRAINS =
                    session.prepare("SELECT * FROM Train");
            GET_TRAIN =
                    session.prepare("SELECT * FROM Train WHERE train_id = ?");
            GET_STATIONS =
                    session.prepare("SELECT * FROM Station");
            GET_STATION =
                    session.prepare("SELECT * FROM Station WHERE station_id = ?");
            GET_TRAIN_LOAD_WEIGHT_BY_TYPE =
                    session.prepare("SELECT sum(commodity_weight) as commodity_weight FROM train_load WHERE train_id=? AND cname=?;");
            GET_WAREHOUSE_COMMODITY_WEIGHT_BY_TYPE =
                    session.prepare("SELECT SUM(commodity_weight) as commodity_weight FROM station_warehouse WHERE station_id=? AND cname=?;");
            INSERT_WAREHOUSE_COMMODITY_BY_TYPE =
                    session.prepare("INSERT INTO station_warehouse (station_id, commodity_name, log_id, commodity_weight) VALUES (?, ?, ?, ?)");
            INSERT_TRAIN_LOAD_BY_TYPE =
                    session.prepare("INSERT INTO train_load (train_id, commodity_name, log_id, commodity_weight) VALUES (?, ?, ?, ?)");
            DELETE_STATION =
                    session.prepare("DELETE FROM station WHERE station_id = ?");
            DELETE_TRAIN =
                    session.prepare("DELETE FROM train WHERE train_id = ?");
            INSERT_STATION =
                    session.prepare("INSERT INTO station (station_id, station_name) VALUES (?, ?)");
            INSERT_TRAIN =
                    session.prepare("INSERT INTO train (train_id, train_name, station_id) VALUES (?, ?)");
            UPDATE_TRAIN_STATION =
                    session.prepare("UPDATE train set station_id = ? WHERE train_id = ?");
        } catch (Exception e){
            throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
        }
    }
}
