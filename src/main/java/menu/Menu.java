package menu;

import asg.cliche.Command;
import asg.cliche.Param;
import backend.BackendException;
import backend.BackendSession;
import loadtest.LoadTestThread;
import model.CommodityWeight;
import model.Station;
import model.Train;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Menu {

  /*
  TODO: Add meaningful error messages.
   */

    private static final String[] load_types = {"coal", "iron", "copper", "gold", "diamond"};

    BackendSession backendSession;

    public Menu(BackendSession backendSession) {
        this.backendSession = backendSession;
    }

    /////////////////////////////////////////////WTF////////////////////////////////////////////////////////////////////

    @Command
    public String loadData() throws BackendException {
        backendSession.loadData();

        return MenuFeedback.LOADING_DATA_SUCCESS;
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
            return String.format(MenuFeedback.ADD_TRAIN_SUCCESS, trainName, trainId, stationUUID);
        }

        return MenuFeedback.SOMETHING_WENT_WRONG;

    }

    @Command
    public String deleteTrain(
            @Param(name = "train_UUID", description = "Unique id for train") String trainUUID) throws BackendException {


        Optional<Train> train = backendSession.getTrain(trainUUID);
        if (train.isPresent()) {
            backendSession.deleteTrain(trainUUID);
            return String.format(MenuFeedback.DELETE_TRAIN_SUCCESS, train.get().getTrainName(), trainUUID);
        }

        return MenuFeedback.SOMETHING_WENT_WRONG;
    }

    @Command
    public String getTrain(
            @Param(name = "train_UUID", description = "Unique id for train") String trainUUID) throws BackendException {
        Optional<Train> train = backendSession.getTrain(trainUUID);
        if (train.isPresent()) {
            return train.get().toString();
        }
        return MenuFeedback.SOMETHING_WENT_WRONG;
    }

    @Command
    public String getTrainLoad(
            @Param(name = "train_UUID", description = "Unique id for train") String trainUUID
    ) throws BackendException {

        StringBuilder answer = new StringBuilder("Load of train with id: " + trainUUID + ":\n");

        for (String type : load_types) {
            Optional<CommodityWeight> trainLoadWeightByType = backendSession.getTrainLoadWeightByType(trainUUID, type);
            trainLoadWeightByType.ifPresent(commodityWeight -> answer.append(type).append(": ").append(commodityWeight.getCommodityWeight()).append("\n"));
        }

        return answer.toString();
    }

    @Command
    public String getTrains() throws BackendException {
        ArrayList<Train> trains = backendSession.getTrains();
        if (trains.size() == 0) {
            return MenuFeedback.GET_TRAINS_NO_TRAINS;
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
            Optional<Station> optionalStation = backendSession.getStation(stationUUID);
            if (optionalStation.isPresent()) {
                backendSession.updateTrainStation(trainUUID, stationUUID);
                return String.format(MenuFeedback.MOVE_TRAIN_SUCCESS, train.getTrainName(), trainUUID, train.getStationId(), stationUUID);
            }
            return MenuFeedback.GET_STATION_NO_STATION;
        }
        return MenuFeedback.GET_TRAIN_NO_TRAIN;
    }

    @Command
    public String unloadTrain(
            @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
            @Param(name = "commodity_name", description = "Name of commodity") String commodityName,
            @Param(name = "commodity_weight", description = "Weight of commodity") Integer commodityWeight) throws BackendException {

        Optional<Train> optionalTrain = backendSession.getTrain(trainUUID);

        if (commodityWeight > 0) {
            if (optionalTrain.isPresent()) {
                Train train = optionalTrain.get();
                Optional<Station> optionalStation = backendSession.getStation(train.getStationId());
                if (optionalStation.isPresent()) {
                    Optional<CommodityWeight> optionalCommodityWeight = backendSession.getTrainLoadWeightByType(trainUUID, commodityName);
                    if (optionalCommodityWeight.isPresent() && optionalCommodityWeight.get().getCommodityWeight() >= commodityWeight) {
                        Station station = optionalStation.get();

                        UUID logIdTrain = UUID.randomUUID();
                        backendSession.insertTrainLoad(trainUUID, commodityName, logIdTrain.toString(), -1 * commodityWeight);

                        //CHECK 1
                        Optional<Train> optionalTrainCheck = backendSession.getTrain(trainUUID);
                        if (!optionalTrainCheck.isPresent() || !optionalTrainCheck.get().getStationId().equals(train.getStationId())) {
                            backendSession.deleteTrainLoad(trainUUID, commodityName, logIdTrain.toString());
                            return MenuFeedback.LOAD_UNLOAD_TRAIN_CHANGED_STATION_OR_TRAIN_DELETED;
                        }
                        //CHECK 2
                        Optional<CommodityWeight> optionalCommodityWeightCheck = backendSession.getTrainLoadWeightByType(trainUUID, commodityName);
                        if (!optionalCommodityWeightCheck.isPresent() || optionalCommodityWeightCheck.get().getCommodityWeight() < 0) {
                            backendSession.deleteTrainLoad(trainUUID, commodityName, logIdTrain.toString());
                            return MenuFeedback.LOAD_UNLOAD_TRAIN_NEGATIVE_LOAD;
                        }

                        //CHECK 3
                        Optional<Station> optionalStationCheck = backendSession.getStation(train.getStationId());
                        if (!optionalStationCheck.isPresent()) {
                            backendSession.deleteTrainLoad(trainUUID, commodityName, logIdTrain.toString());
                            return MenuFeedback.LOAD_UNLOAD_TRAIN_STATION_DELETED;
                        }


                        backendSession.insertStationWarehouseCommodity(station.getStationId(), commodityName, UUID.randomUUID().toString(), commodityWeight);

                        return String.format(MenuFeedback.UNLOAD_TRAIN_SUCCESS, commodityWeight, commodityName, trainUUID, station.getStationId());
                    }
                    return MenuFeedback.UNLOAD_TRAIN_NOT_ENOUGH_LOAD;
                }
                return MenuFeedback.MISSING_TRAIN_STATION;
            }
            return MenuFeedback.MISSING_TRAIN;
        }
        return MenuFeedback.COMMODITY_WEIGHT_ARGUMENT_MUST_BE_POSITIVE;
    }

    @Command
    public String loadTrain(
            @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
            @Param(name = "commodity_name", description = "Name of commodity") String commodityName,
            @Param(name = "commodity_weight", description = "Weight of commodity") Integer commodityWeight) throws BackendException {

        Optional<Train> optionalTrain = backendSession.getTrain(trainUUID);


        if (commodityWeight > 0) {
            if (optionalTrain.isPresent()) {
                Train train = optionalTrain.get();
                Optional<Station> optionalStation = backendSession.getStation(train.getStationId());
                if (optionalStation.isPresent()) {
                    Station station = optionalStation.get();
                    Optional<CommodityWeight> optionalCommodityWeight = backendSession.getTrainLoadWeightByType(trainUUID, commodityName);
                    if (optionalCommodityWeight.isPresent() && optionalCommodityWeight.get().getCommodityWeight() >= commodityWeight) {
                        UUID logIdStation = UUID.randomUUID();
                        backendSession.insertStationWarehouseCommodity(station.getStationId(), commodityName, logIdStation.toString(), -1 * commodityWeight);

                        //CHECK 1
                        Optional<Station> optionalStationCheck = backendSession.getStation(train.getStationId());
                        if (!optionalStationCheck.isPresent()) {
                            backendSession.deleteStationWarehouseCommodity(station.getStationId(), commodityName, logIdStation.toString());
                            return MenuFeedback.LOAD_UNLOAD_TRAIN_STATION_DELETED;
                        }

                        //CHECK 2
                        Optional<CommodityWeight> optionalCommodityWeightCheck = backendSession.getWarehouseCommodityWeightByType(station.getStationId(), commodityName);
                        if (!optionalCommodityWeightCheck.isPresent() || optionalCommodityWeightCheck.get().getCommodityWeight() < 0) {
                            backendSession.deleteStationWarehouseCommodity(station.getStationId(), commodityName, logIdStation.toString());
                            return MenuFeedback.LOAD_UNLOAD_TRAIN_NEGATIVE_LOAD;
                        }

                        //CHECK 3
                        Optional<Train> optionalTrainCheck = backendSession.getTrain(trainUUID);
                        if (!optionalTrainCheck.isPresent() || !optionalTrainCheck.get().getStationId().equals(train.getStationId())) {
                            backendSession.deleteStationWarehouseCommodity(station.getStationId(), commodityName, logIdStation.toString());
                            return MenuFeedback.LOAD_UNLOAD_TRAIN_CHANGED_STATION_OR_TRAIN_DELETED;
                        }

                        backendSession.insertTrainLoad(trainUUID, commodityName, UUID.randomUUID().toString(), commodityWeight);

                        return String.format(MenuFeedback.LOAD_TRAIN_SUCCESS, commodityWeight, commodityName, station.getStationId(), trainUUID);
                    }
                    return MenuFeedback.LOAD_TRAIN_NOT_ENOUGH_COMMODITY;
                }
                return MenuFeedback.MISSING_TRAIN_STATION;
            }
            return MenuFeedback.MISSING_TRAIN;
        }
        return MenuFeedback.COMMODITY_WEIGHT_ARGUMENT_MUST_BE_POSITIVE;
    }

    @Command
    public String insertTrainLoad(
            @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
            @Param(name = "commodity_name", description = "Name of commodity") String commodityName,
            @Param(name = "commodity_weight", description = "Weight of commodity") Integer commodityWeight) throws BackendException {

        Optional<Train> optionalTrain = backendSession.getTrain(trainUUID);

        if (optionalTrain.isPresent()) {
            backendSession.insertTrainLoad(trainUUID, commodityName, UUID.randomUUID().toString(), commodityWeight);

            return String.format(MenuFeedback.INSERT_COMMODITY_SUCCESS, commodityWeight, commodityName, trainUUID);
        }
        return MenuFeedback.SOMETHING_WENT_WRONG;
    }

    /////////////////////////////////////////////STATION////////////////////////////////////////////////////////////////

    @Command//Generate UUID for station, Add to database
    public String addStation(
            @Param(name = "station_name", description = "Name for new station") String stationName) throws BackendException {
        UUID stationId = UUID.randomUUID();
        backendSession.insertStation(stationId.toString(), stationName);
        return String.format(MenuFeedback.ADD_STATION_SUCCESS, stationName, stationId);
    }

    @Command
    public String deleteStation(
            @Param(name = "station_UUID", description = "UUID of an existing station") String stationUUID) throws BackendException {

        Optional<Station> optionalStation = backendSession.getStation(stationUUID);

        if (optionalStation.isPresent()) {
            backendSession.deleteStation(stationUUID);

            return String.format(MenuFeedback.DELETE_STATION_SUCCESS, stationUUID);
        }

        return MenuFeedback.SOMETHING_WENT_WRONG;
    }

    @Command
    public String getStation(
            @Param(name = "station_UUID", description = "Unique id for station") String stationUUID) throws BackendException {
        Optional<Station> optionalStation = backendSession.getStation(stationUUID);

        if (optionalStation.isPresent()) {
            return optionalStation.get().toString();
        }

        return MenuFeedback.SOMETHING_WENT_WRONG;
    }

    @Command
    public String getStationWarehouseLoad(
            @Param(name = "station_UUID", description = "Unique id for station") String stationUUID
    ) throws BackendException {

        StringBuilder answer = new StringBuilder("Load of station with id: " + stationUUID + ":\n");

        for (String type : load_types) {
            Optional<CommodityWeight> trainLoadWeightByType = backendSession.getWarehouseCommodityWeightByType(stationUUID, type);
            trainLoadWeightByType.ifPresent(commodityWeight -> answer.append(type).append(": ").append(commodityWeight.getCommodityWeight()).append("\n"));
        }
        return answer.toString();
    }

    @Command
    public String getStations() throws BackendException {
        ArrayList<Station> stations = backendSession.getStations();

        if (stations.size() > 0) {
            return stations.toString();
        }

        return MenuFeedback.GET_STATIONS_NO_STATION;
    }

    @Command
    public String insertStationWarehouseCommodity(
            @Param(name = "station_UUID", description = "Name of an existing train") String stationUUID,
            @Param(name = "commodity_name", description = "Name of commodity") String commodityName,
            @Param(name = "commodity_weight", description = "Weight of commodity") Integer commodityWeight) throws BackendException {

        Optional<Station> optionalStation = backendSession.getStation(stationUUID);

        if (optionalStation.isPresent()) {
            backendSession.insertStationWarehouseCommodity(stationUUID, commodityName, UUID.randomUUID().toString(), commodityWeight);

            return String.format(MenuFeedback.INSERT_COMMODITY_SUCCESS, commodityWeight, commodityName, stationUUID);
        }
        return MenuFeedback.SOMETHING_WENT_WRONG;
    }

    @Command
    public String performLoadTest(
            @Param(name = "thread_count", description = "Number of threads performing test") Integer threadCount,
            @Param(name = "test_length", description = "Length of test in seconds") Integer testLength,
            @Param(name = "operation_interval", description = "Interval which indicates how often threads perform actions in milliseconds") Integer operationInterval,
            @Param(name = "maximum_percent_of_commodity_weight", description = "Indicates maximum percent of available commodity which will be used per operation") Integer maximumPercentOfCommodity,
            @Param(name = "minimum_percent_of_commodity_weight", description = "Indicates minimum percent of available commodity which will be used per operation") Integer minimumPercentOfCommodity
    ) {
        if (maximumPercentOfCommodity < minimumPercentOfCommodity) {
            return MenuFeedback.LOAD_TEST_BAD_PERCENTAGES;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        ArrayList<LoadTestThread> loadTestThreads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            LoadTestThread loadTestThread = new LoadTestThread(operationInterval, testLength, maximumPercentOfCommodity, minimumPercentOfCommodity, backendSession);
            loadTestThreads.add(loadTestThread);
            executorService.execute(loadTestThread);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int operationSucceededCount = loadTestThreads.stream().flatMapToInt(loadTestThread -> IntStream.of(loadTestThread.getLoadUnloadWasSuccessfulCount())).sum();
        int negativeCommodityWeightCount = loadTestThreads.stream().flatMapToInt(loadTestThread -> IntStream.of(loadTestThread.getNegativeCommodityWeightCount())).sum();
        int trainChangedStationCount = loadTestThreads.stream().flatMapToInt(loadTestThread -> IntStream.of(loadTestThread.getTrainChangedStationCount())).sum();
        int stationDeletionCount = loadTestThreads.stream().flatMapToInt(loadTestThread -> IntStream.of(loadTestThread.getStationWasDeletedCount())).sum();
        int preconditionFailedCount = loadTestThreads.stream().flatMapToInt(loadTestThread -> IntStream.of(loadTestThread.getPreconditionFailedCount())).sum();
        int operationFailedCount = negativeCommodityWeightCount + trainChangedStationCount + preconditionFailedCount + stationDeletionCount;
        return String.format(MenuFeedback.LOAD_TEST_RESULTS, operationSucceededCount, operationFailedCount, negativeCommodityWeightCount, trainChangedStationCount, stationDeletionCount, preconditionFailedCount);
    }

}
