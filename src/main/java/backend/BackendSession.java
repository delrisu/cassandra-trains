package backend;

import com.datastax.driver.core.*;
import com.google.common.io.Resources;
import model.CommodityWeight;
import model.Station;
import model.Train;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

public class BackendSession {

  public static PreparedStatement GET_TRAINS;
  public static PreparedStatement GET_TRAIN;
  public static PreparedStatement INSERT_TRAIN;
  public static PreparedStatement DELETE_TRAIN;
  public static PreparedStatement GET_TRAIN_LOAD_WEIGHT_BY_TYPE;
  public static PreparedStatement INSERT_TRAIN_LOAD;
  public static PreparedStatement DELETE_TRAIN_LOAD;
  public static PreparedStatement GET_STATIONS;
  public static PreparedStatement GET_STATION;
  public static PreparedStatement INSERT_STATION;
  public static PreparedStatement DELETE_STATION;
  public static PreparedStatement GET_WAREHOUSE_COMMODITY_WEIGHT_BY_TYPE;
  public static PreparedStatement INSERT_WAREHOUSE_COMMODITY;
  public static PreparedStatement DELETE_WAREHOUSE_COMMODITY;
  public static PreparedStatement UPDATE_TRAIN_STATION;
  public static String LOAD_DATA;
  public static String CREATE_SCHEMAS;
  private final Session session;

  public BackendSession(String contactPoint, String keyspace) throws BackendException {
    Cluster cluster = Cluster.builder().addContactPoint(contactPoint)
        .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM)).build();
    try {
      LOAD_DATA = Resources.toString(Resources.getResource("load_data.cql"), StandardCharsets.UTF_8);
      CREATE_SCHEMAS = Resources.toString(Resources.getResource("create_schema.cql"), StandardCharsets.UTF_8);
      session = cluster.connect(keyspace);
      createSchemas(cluster);
    } catch (Exception e) {
      throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
    }
    prepareStatements();
  }

  private void prepareStatements() throws BackendException {
    try {
      GET_TRAINS =
          session.prepare("SELECT * FROM Train");
      GET_TRAIN =
          session.prepare("SELECT * FROM Train WHERE train_id = ?");
      GET_STATIONS =
          session.prepare("SELECT * FROM Station");
      GET_STATION =
          session.prepare("SELECT * FROM Station WHERE station_id = ?");
      GET_TRAIN_LOAD_WEIGHT_BY_TYPE =
          session.prepare("SELECT sum(commodity_weight) as commodity_weight FROM train_load WHERE train_id=? AND commodity_name=?;");
      GET_WAREHOUSE_COMMODITY_WEIGHT_BY_TYPE =
          session.prepare("SELECT SUM(commodity_weight) as commodity_weight FROM station_warehouse WHERE station_id=? AND commodity_name=?;");
      INSERT_WAREHOUSE_COMMODITY =
          session.prepare("INSERT INTO station_warehouse (station_id, commodity_name, log_id, commodity_weight) VALUES (?, ?, ?, ?)");
      DELETE_WAREHOUSE_COMMODITY =
          session.prepare("DELETE FROM station_warehouse WHERE station_id = ? AND commodity_name = ? AND log_id = ?");
      INSERT_TRAIN_LOAD =
          session.prepare("INSERT INTO train_load (train_id, commodity_name, log_id, commodity_weight) VALUES (?, ?, ?, ?)");
      DELETE_TRAIN_LOAD =
          session.prepare("DELETE FROM train_load WHERE train_id = ? AND commodity_name = ? AND log_id = ?");
      DELETE_STATION =
          session.prepare("DELETE FROM station WHERE station_id = ?");
      DELETE_TRAIN =
          session.prepare("DELETE FROM train WHERE train_id = ?");
      INSERT_STATION =
          session.prepare("INSERT INTO station (station_id, station_name) VALUES (?, ?)");
      INSERT_TRAIN =
          session.prepare("INSERT INTO train (train_id, train_name, station_id) VALUES (?, ?, ?)");
      UPDATE_TRAIN_STATION =
          session.prepare("UPDATE train set station_id = ? WHERE train_id = ?");
    } catch (Exception e) {
      throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
    }
  }

  private void createSchemas(Cluster cluster) throws BackendException {
    try {
      Session session = cluster.connect();
      for (String command : BackendSession.CREATE_SCHEMAS.split(System.getProperty("line.separator"))) {
        session.execute(new BoundStatement(session.prepare(command)));
        System.out.println(command + " DONE");
      }
    } catch (Exception e) {
      throw new BackendException(e.getMessage(), e);
    }
  }

  public void loadData() throws BackendException {
    for (String command : BackendSession.LOAD_DATA.replace(")" + System.getProperty("line.separator"), ") ").split(System.getProperty("line.separator"))) {
      try {
        session.execute(new BoundStatement(session.prepare(command)));
        System.out.println(command + " DONE");
      } catch (Exception e) {
        throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
      }
    }
  }

  public ArrayList<Train> getTrains() throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.GET_TRAINS);
    ResultSet resultSet;
    ArrayList<Train> trains = new ArrayList<>();

    try {
      resultSet = session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
    for (Row record : resultSet) {
      trains.add(new Train(record));
    }
    return trains;
  }

  public Optional<Train> getTrain(String trainId) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.GET_TRAIN);
    boundStatement.bind(trainId);
    ResultSet resultSet;

    try {
      resultSet = session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }

    Row row = resultSet.one();
    if (row != null) {
      return Optional.of(new Train(row));
    } else {
      return Optional.empty();
    }
  }

  public Optional<CommodityWeight> getTrainLoadWeightByType(String trainId, String commodityName) throws BackendException {
    BoundStatement boundStatement = new BoundStatement(BackendSession.GET_TRAIN_LOAD_WEIGHT_BY_TYPE);
    boundStatement.bind(trainId, commodityName);
    ResultSet resultSet;

    try {
      resultSet = session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
    Row row = resultSet.one();
    if (row != null) {
      return Optional.of(new CommodityWeight(row));
    } else {
      return Optional.empty();
    }
  }

  public void updateTrainStation(String train_id, String station_id) throws BackendException {
    BoundStatement boundStatement = new BoundStatement(BackendSession.UPDATE_TRAIN_STATION);
    boundStatement.bind(station_id, train_id);
    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public void insertTrain(String train_id, String train_name, String station_id) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.INSERT_TRAIN);
    boundStatement.bind(train_id, train_name, station_id);
    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public void insertTrainLoad(String train_id, String commodity_name, String log_id, Integer commodity_weight) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.INSERT_TRAIN_LOAD);
    boundStatement.bind(train_id, commodity_name, log_id, commodity_weight);
    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public void deleteTrainLoad(String train_id, String commodity_name, String log_id) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.DELETE_TRAIN_LOAD);
    boundStatement.bind(train_id, commodity_name, log_id);
    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public void deleteTrain(String trainId) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.DELETE_TRAIN);
    boundStatement.bind(trainId);

    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public ArrayList<Station> getStations() throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.GET_STATIONS);
    ResultSet resultSet;
    ArrayList<Station> stations = new ArrayList<>();

    try {
      resultSet = session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
    for (Row record : resultSet) {
      stations.add(new Station(record));
    }
    return stations;
  }

  public Optional<Station> getStation(String stationId) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.GET_STATION);
    boundStatement.bind(stationId);
    ResultSet resultSet;

    try {
      resultSet = session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }

    Row row = resultSet.one();
    if (row != null) {
      return Optional.of(new Station(row));
    } else {
      return Optional.empty();
    }
  }

  public Optional<CommodityWeight> getWarehouseCommodityWeightByType(String stationId, String commodityName) throws BackendException {
    BoundStatement boundStatement = new BoundStatement(BackendSession.GET_WAREHOUSE_COMMODITY_WEIGHT_BY_TYPE);
    boundStatement.bind(stationId, commodityName);
    ResultSet resultSet;

    try {
      resultSet = session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
    Row row = resultSet.one();
    if (row != null) {
      return Optional.of(new CommodityWeight(row));
    } else {
      return Optional.empty();
    }
  }

  public void insertStation(String station_id, String station_name) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.INSERT_STATION);
    boundStatement.bind(station_id, station_name);
    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public void insertStationWarehouseCommodity(String station_id, String commodity_name, String log_id, Integer commodity_weight) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.INSERT_WAREHOUSE_COMMODITY);
    boundStatement.bind(station_id, commodity_name, log_id, commodity_weight);
    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public void deleteStationWarehouseCommodity(String station_id, String commodity_name, String log_id) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.DELETE_WAREHOUSE_COMMODITY);
    boundStatement.bind(station_id, commodity_name, log_id);
    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }

  public void deleteStation(String stationId) throws BackendException {

    BoundStatement boundStatement = new BoundStatement(BackendSession.DELETE_STATION);
    boundStatement.bind(stationId);

    try {
      session.execute(boundStatement);
    } catch (Exception e) {
      throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
    }
  }
}
