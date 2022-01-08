package backend;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import model.CommodityWeight;
import model.Station;
import model.Train;

import java.util.ArrayList;

public class StatementHandler {
    private Session session;

    public StatementHandler(Session session) {
        this.session = session;
    }


    public ArrayList<Train> getTrains() throws BackendException {

        BoundStatement boundStatement = new BoundStatement(BackendSession.GET_TRAINS);
        ResultSet resultSet = null;
        ArrayList<Train> trains = new ArrayList<>();

        try{
            resultSet = session.execute(boundStatement);
        } catch(Exception e){
           throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        for(Row record: resultSet){
            trains.add(new Train(record));
        }
        return trains;
    }

    public Train getTrain(String trainId) throws BackendException {

        BoundStatement boundStatement = new BoundStatement(BackendSession.GET_TRAINS);
        boundStatement.bind(trainId);
        ResultSet resultSet = null;

        try{
            resultSet = session.execute(boundStatement);
        } catch(Exception e){
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        return new Train(resultSet.one());
    }

    public CommodityWeight getTrainLoadWeightByType(String trainId, String commodityName) throws BackendException {
        BoundStatement boundStatement = new BoundStatement(BackendSession.GET_TRAIN_LOAD_WEIGHT_BY_TYPE);
        boundStatement.bind(trainId, commodityName);
        ResultSet resultSet = null;

        try {
            resultSet = session.execute(boundStatement);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        return new CommodityWeight(resultSet.one());
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

    public void insertTrain(String train_id, String train_name) throws BackendException {

        BoundStatement boundStatement = new BoundStatement(BackendSession.INSERT_TRAIN);
        boundStatement.bind(train_id, train_name);
        try {
            session.execute(boundStatement);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
    }

    public void deleteTrain(String trainId) throws BackendException {

        BoundStatement boundStatement = new BoundStatement(BackendSession.DELETE_TRAIN);
        boundStatement.bind(trainId);

        try{
            session.execute(boundStatement);
        } catch(Exception e){
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
    }

    public ArrayList<Station> getStations() throws BackendException {

        BoundStatement boundStatement = new BoundStatement(BackendSession.GET_STATIONS);
        ResultSet resultSet = null;
        ArrayList<Station> stations = new ArrayList<>();

        try{
            resultSet = session.execute(boundStatement);
        } catch(Exception e){
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        for(Row record: resultSet){
            stations.add(new Station(record));
        }
        return stations;
    }

    public Station getStation(String stationId) throws BackendException {

        BoundStatement boundStatement = new BoundStatement(BackendSession.GET_STATION);
        boundStatement.bind(stationId);
        ResultSet resultSet = null;

        try {
            resultSet = session.execute(boundStatement);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        return new Station(resultSet.one());
    }

    public CommodityWeight getWarehouseCommodityWeightByType(String stationId, String commodityName) throws BackendException {
        BoundStatement boundStatement = new BoundStatement(BackendSession.GET_WAREHOUSE_COMMODITY_WEIGHT_BY_TYPE);
        boundStatement.bind(stationId, commodityName);
        ResultSet resultSet = null;

        try {
            resultSet = session.execute(boundStatement);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
        return new CommodityWeight(resultSet.one());
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

    public void deleteStation(String stationId) throws BackendException {

        BoundStatement boundStatement = new BoundStatement(BackendSession.DELETE_STATION);
        boundStatement.bind(stationId);

        try{
            session.execute(boundStatement);
        } catch(Exception e){
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }
    }

}
