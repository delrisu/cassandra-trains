package menu;

import asg.cliche.Command;
import asg.cliche.Param;
import backend.BackendException;
import backend.BackendSession;
import model.CommodityWeight;
import model.Station;
import model.Train;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class Menu {

  /*
  TODO: Add meaningful error messages.
   */

  private static final String SOMETHING_WENT_WRONG = "Something went wrong! :(";
  BackendSession backendSession;

  public Menu(BackendSession backendSession) {
    this.backendSession = backendSession;
  }

  /////////////////////////////////////////////TRAIN//////////////////////////////////////////////////////////////////

  @Command//Generate UUID for train, Add to database
  public String addTrain(
      @Param(name = "train_name", description = "Name for new train") String trainName,
      @Param(name = "station_UUID", description = "UUID of an existing station") String stationUUID) throws BackendException {

    Optional<Station> optionalStation = backendSession.getStation(stationUUID);

    if (optionalStation.isPresent()) {
      UUID trainId = UUID.randomUUID();
      backendSession.insertTrain(trainId.toString(), trainName, stationUUID);
      return "Train " + trainName + " with id: " + trainId + " was added to station with id: " + stationUUID;
    }

    return SOMETHING_WENT_WRONG;

  }

  @Command
  public String deleteTrain(
      @Param(name = "train_UUID", description = "Unique id for train") String trainUUID) throws BackendException {


    Optional<Train> train = backendSession.getTrain(trainUUID);
    if (train.isPresent()) {
      backendSession.deleteTrain(trainUUID);
      return "Train with id: " + trainUUID + " was deleted";
    }

    return SOMETHING_WENT_WRONG;
  }

  @Command
  public String getTrain(
      @Param(name = "train_UUID", description = "Unique id for train") String trainUUID) throws BackendException {
    Optional<Train> train = backendSession.getTrain(trainUUID);
    if (train.isPresent()) {
      return train.get().toString();
    }
    return SOMETHING_WENT_WRONG;
  }

  @Command
  public String getTrains() throws BackendException {
    ArrayList<Train> trains = backendSession.getTrains();
    if (trains.size() == 0) {
      return "No available trains. :(";
    }
    return trains.toString();
  }

  @Command
  public String moveTrain(
      @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
      @Param(name = "station_UUID", description = "UUID of an existing station") String stationUUID) throws BackendException {

    Optional<Train> optionalTrain = backendSession.getTrain(trainUUID);

    if (optionalTrain.isPresent()) {
      Train train = optionalTrain.get();
      Optional<Station> optionalStation = backendSession.getStation(train.getStationId());
      if (optionalStation.isPresent()) {
        backendSession.updateTrainStation(trainUUID, stationUUID);
        return "Moved train " + train.getTrainName() + " with id: " + trainUUID + " from station with id "
            + train.getStationId() + " to station with id: " + stationUUID;
      }
    }
    return SOMETHING_WENT_WRONG;
  }

  @Command
  public String unloadTrain(
      @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
      @Param(name = "commodity_name", description = "Name of commodity") String commodityName,
      @Param(name = "commodity_weight", description = "Weight of commodity") Integer commodityWeight) throws BackendException {

    Optional<Train> optionalTrain = backendSession.getTrain(trainUUID);

    if (optionalTrain.isPresent()) {
      Train train = optionalTrain.get();
      Optional<CommodityWeight> optionalCommodityWeight = backendSession.getTrainLoadWeightByType(trainUUID, commodityName);
      if (optionalCommodityWeight.isPresent() && optionalCommodityWeight.get().getCommodityWeight() >= commodityWeight) {
        Optional<Station> optionalStation = backendSession.getStation(train.getStationId());
        if (optionalStation.isPresent()) {
          Station station = optionalStation.get();

          UUID logIdTrain = UUID.randomUUID();
          backendSession.insertTrainLoad(trainUUID, commodityName, logIdTrain.toString(), -1 * optionalCommodityWeight.get().getCommodityWeight());

          //CHECK 1
          Optional<Train> optionalTrainCheck = backendSession.getTrain(trainUUID);
          if (!optionalTrainCheck.isPresent() || optionalTrainCheck.get().getStationId().equals(train.getStationId())) {
            backendSession.deleteTrainLoad(trainUUID, commodityName, logIdTrain.toString());
            return SOMETHING_WENT_WRONG;
          }
          //CHECK 2
          Optional<CommodityWeight> optionalCommodityWeightCheck = backendSession.getTrainLoadWeightByType(trainUUID, commodityName);
          if (!optionalCommodityWeightCheck.isPresent() || optionalCommodityWeightCheck.get().getCommodityWeight() < 0) {
            backendSession.deleteTrainLoad(trainUUID, commodityName, logIdTrain.toString());
            return SOMETHING_WENT_WRONG;
          }

          UUID logIdStation = UUID.randomUUID();
          backendSession.InsertStationWarehouseCommodity(station.getStationId(), commodityName, logIdStation.toString(), optionalCommodityWeight.get().getCommodityWeight());

          return "Moved " + commodityWeight + "t of " + commodityName + " from train with id: " + trainUUID + " to station with id: " + station.getStationId();
        }
        return SOMETHING_WENT_WRONG;
      }
      return SOMETHING_WENT_WRONG;
    }
    return SOMETHING_WENT_WRONG;
  }

  @Command
  public String loadTrain(
      @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
      @Param(name = "commodity_name", description = "Name of commodity") String commodityName,
      @Param(name = "commodity_weight", description = "Weight of commodity") Integer commodityWeight) throws BackendException {

    Optional<Train> optionalTrain = backendSession.getTrain(trainUUID);

    if (optionalTrain.isPresent()) {
      Train train = optionalTrain.get();
      Optional<Station> optionalStation = backendSession.getStation(train.getStationId());
      if (optionalStation.isPresent()) {
        Station station = optionalStation.get();
        Optional<CommodityWeight> optionalCommodityWeight = backendSession.getTrainLoadWeightByType(trainUUID, commodityName);
        if (optionalCommodityWeight.isPresent() && optionalCommodityWeight.get().getCommodityWeight() >= commodityWeight) {
          UUID logIdStation = UUID.randomUUID();
          backendSession.InsertStationWarehouseCommodity(station.getStationId(), commodityName, logIdStation.toString(), -1 * optionalCommodityWeight.get().getCommodityWeight());

          //CHECK 1
          Optional<CommodityWeight> optionalCommodityWeightCheck = backendSession.getWarehouseCommodityWeightByType(station.getStationId(), commodityName);
          if (!optionalCommodityWeightCheck.isPresent() || optionalCommodityWeightCheck.get().getCommodityWeight() < 0) {
            backendSession.deleteStationWarehouseCommodity(trainUUID, commodityName, logIdStation.toString());
            return SOMETHING_WENT_WRONG;
          }

          //CHECK 2
          Optional<Train> optionalTrainCheck = backendSession.getTrain(trainUUID);
          if (!optionalTrainCheck.isPresent() || optionalTrainCheck.get().getStationId().equals(train.getStationId())) {
            backendSession.deleteStationWarehouseCommodity(trainUUID, commodityName, logIdStation.toString());
            return SOMETHING_WENT_WRONG;
          }

          return "Moved " + commodityWeight + "t of " + commodityName + " from station with id: " + station.getStationId() + " to train with id: " + trainUUID;
        }
        return SOMETHING_WENT_WRONG;
      }
      return SOMETHING_WENT_WRONG;
    }
    return SOMETHING_WENT_WRONG;
  }

  /////////////////////////////////////////////STATION////////////////////////////////////////////////////////////////

  @Command//Generate UUID for station, Add to database
  public String addStation(
      @Param(name = "station_name", description = "Name for new station") String stationName) throws BackendException {
    UUID stationId = UUID.randomUUID();
    backendSession.insertStation(stationId.toString(), stationName);
    return "Station " + stationName + " with id: " + stationId + " was added";
  }

  @Command
  public String deleteStation(
      @Param(name = "station_UUID", description = "UUID of an existing station") String stationUUID) throws BackendException {

    Optional<Station> optionalStation = backendSession.getStation(stationUUID);

    if (optionalStation.isPresent()) {
      backendSession.deleteStation(stationUUID);

      return "Deleted station with id: " + stationUUID;
    }

    return SOMETHING_WENT_WRONG;
  }

  @Command
  public String getStation(
      @Param(name = "station_UUID", description = "Unique id for station") String stationUUID) throws BackendException {
    Optional<Station> optionalStation = backendSession.getStation(stationUUID);

    if (optionalStation.isPresent()) {
      return optionalStation.get().toString();
    }

    return SOMETHING_WENT_WRONG;
  }

  @Command
  public String getStations() throws BackendException {
    ArrayList<Station> stations = backendSession.getStations();

    if (stations.size() > 0) {
      return stations.toString();
    }

    return "No stations found.";
  }

}
