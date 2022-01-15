package loadtest;

import backend.BackendException;
import backend.BackendSession;
import menu.Menu;
import menu.MenuFeedback;
import model.CommodityWeight;
import model.Station;
import model.Train;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class LoadTestThread implements Runnable {
    private static final String[] load_types = {"coal", "iron", "copper", "gold", "diamond"};
    private final Integer minimumPercentOfLoad;
    private final Integer maximumPercentOfLoad;
    private final Integer operationIntervalLength;
    private final Integer testLength;
    private final Menu menu;
    private final BackendSession backendSession;
    private Integer negativeCommodityWeightCount = 0;
    private Integer trainChangedStationCount = 0;
    private Integer stationWasDeletedCount = 0;
    private Integer preconditionFailedCount = 0;
    private Integer loadUnloadWasSuccessfulCount = 0;

    public LoadTestThread(Integer operationIntervalLength, Integer testLength, Integer maximumPercentOfLoad, Integer minimumPercentOfLoad, BackendSession backendSession) {
        this.operationIntervalLength = operationIntervalLength;
        this.minimumPercentOfLoad = minimumPercentOfLoad;
        this.maximumPercentOfLoad = maximumPercentOfLoad;
        this.menu = new Menu(backendSession);
        this.backendSession = backendSession;
        this.testLength = testLength;
    }

    public Integer getNegativeCommodityWeightCount() {
        return negativeCommodityWeightCount;
    }

    public Integer getTrainChangedStationCount() {
        return trainChangedStationCount;
    }

    public Integer getPreconditionFailedCount() {
        return preconditionFailedCount;
    }

    public Integer getLoadUnloadWasSuccessfulCount() {
        return loadUnloadWasSuccessfulCount;
    }

    public Integer getStationWasDeletedCount() {
        return stationWasDeletedCount;
    }

    @Override
    public void run() {
        long testStartTime = System.currentTimeMillis();
        while ((System.currentTimeMillis()) - testStartTime < testLength * 1000) {
            int operationNumber = new Random().nextInt(2);
            try {
                switch (operationNumber) {
                    case 0:
                        moveRandomTrain();
                        break;
                    case 1:
                        loadUnloadRandomTrain();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(operationIntervalLength);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUnloadRandomTrain() throws BackendException {
        try {
            ArrayList<Train> trains = backendSession.getTrains();
            if (trains.size() > 0) {
                String result = null;
                Train chosenTrain = trains.get(new Random().nextInt(trains.size() - 1));
                String chosenCommodityName = LoadTestThread.load_types[new Random().nextInt(LoadTestThread.load_types.length)];
                if (new Random().nextInt(2) < 1) {
                    result = loadRandomTrain(chosenTrain, chosenCommodityName);
                } else {
                    result = unloadRandomTrain(chosenTrain, chosenCommodityName);
                }
                if (result != null) {
                    checkResult(result);
                }
            }
        } catch (BackendException e) {
            throw new BackendException(e.getMessage(), e);
        }
    }

    private void checkResult(String result) {
        switch (result) {
            case MenuFeedback.LOAD_UNLOAD_TRAIN_NEGATIVE_LOAD:
                this.negativeCommodityWeightCount += 1;
                break;
            case MenuFeedback.LOAD_UNLOAD_TRAIN_CHANGED_STATION_OR_TRAIN_DELETED:
                this.trainChangedStationCount += 1;
                break;
            case MenuFeedback.LOAD_UNLOAD_TRAIN_STATION_DELETED:
                this.stationWasDeletedCount += 1;
                break;
            case MenuFeedback.COMMODITY_WEIGHT_ARGUMENT_MUST_BE_POSITIVE:
            case MenuFeedback.MISSING_TRAIN:
            case MenuFeedback.MISSING_TRAIN_STATION:
            case MenuFeedback.UNLOAD_TRAIN_NOT_ENOUGH_LOAD:
            case MenuFeedback.LOAD_TRAIN_NOT_ENOUGH_COMMODITY:
                this.preconditionFailedCount += 1;
                break;
            default:
                this.loadUnloadWasSuccessfulCount += 1;
                break;
        }
    }

    private String unloadRandomTrain(Train chosenTrain, String chosenCommodityName) throws BackendException {
        Optional<CommodityWeight> commodityWeight = backendSession.getWarehouseCommodityWeightByType(chosenTrain.getStationId(), chosenCommodityName);
        if (commodityWeight.isPresent()) {
            Integer chosenWeightToLoad = Math.round(commodityWeight.get().getCommodityWeight() * ((new Random().nextInt(
                    maximumPercentOfLoad - minimumPercentOfLoad) + minimumPercentOfLoad) * 0.01f)
            );
            return menu.loadTrain(chosenTrain.getTrainId(), chosenCommodityName, chosenWeightToLoad);
        }
        return null;
    }

    private String loadRandomTrain(Train chosenTrain, String chosenCommodityName) throws BackendException {
        Optional<CommodityWeight> commodityWeight = backendSession.getTrainLoadWeightByType(chosenTrain.getTrainId(), chosenCommodityName);
        if (commodityWeight.isPresent()) {
            Integer chosenWeightToLoad = Math.round(commodityWeight.get().getCommodityWeight() * ((new Random().nextInt(
                    maximumPercentOfLoad - minimumPercentOfLoad) + minimumPercentOfLoad) * 0.01f)
            );
            return menu.unloadTrain(chosenTrain.getTrainId(), chosenCommodityName, chosenWeightToLoad);
        }
        return null;
    }

    private void moveRandomTrain() throws BackendException {
        try {
            ArrayList<Train> trains = backendSession.getTrains();
            if (trains.size() > 0) {
                Train chosenTrain = trains.get(new Random().nextInt(trains.size() - 1));
                ArrayList<Station> stations = backendSession.getStations();
                if (stations.size() > 0) {
                    Station chosenStation = stations.get(new Random().nextInt(stations.size() - 1));
                    menu.moveTrain(chosenTrain.getTrainId(), chosenStation.getStationId());
                }
            }
        } catch (BackendException e) {
            throw new BackendException(e.getMessage(), e);
        }
    }
}
