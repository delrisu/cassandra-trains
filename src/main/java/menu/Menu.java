package menu;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.ShellFactory;

import java.io.IOException;

public class Menu {

    public static void main(String[] args) throws IOException {
        ShellFactory.createConsoleShell("train-master-69", "", new Menu()).commandLoop();
    }

    /////////////////////////////////////////////TRAIN//////////////////////////////////////////////////////////////////

    @Command//Generate UUID for train, Add to database
    public int addTrain(
            @Param(name = "train_name", description = "Name for new train") String trainName, //TODO: Name should be unique?
            @Param(name = "station_UUID", description = "UUID of an existing station") String stationName) {
        return 200;
    }

    @Command
    public int deleteTrain(
            @Param(name = "train_UUID", description = "Unique id for train") String trainUUID) {
        return 200;
    }

    @Command
    public String getTrain(
            @Param(name = "train_UUID", description = "Unique id for train") String trainUUID) {
        return "Train: xxxx";
    }

    @Command
    public String getTrains() {
        return "Train1: xxxx\nTrain2: yyy";
    }

    @Command
    public int moveTrain(
            @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
            @Param(name = "station_name", description = "Name of an existing station") String stationName) {
        return 200;
    }

    @Command
    public int unloadTrain(
            @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
            @Param(name = "commodity_name", description = "Name of commodity") String commodityName) {
        return 200;
    }

    @Command
    public int loadTrain(
            @Param(name = "train_UUID", description = "Name of an existing train") String trainUUID,
            @Param(name = "commodity_name", description = "Name of commodity") String commodityName) {
        return 200;
    }

    /////////////////////////////////////////////STATION////////////////////////////////////////////////////////////////

    @Command//Generate UUID for station, Add to database
    public int addStation(
            @Param(name = "station_name", description = "Name for new station") String stationName) { //TODO: Name should be unique?
        return 200;
    }

    @Command
    public int deleteStation(
            @Param(name = "station_UUID", description = "UUID of an existing station") String stationUUID) {
        return 200;
    }

    @Command
    public String getStation(
            @Param(name = "station_UUID", description = "Unique id for station") String trainUUID) {
        return "Train: xxxx";
    }

    @Command
    public String getStations() {
        return "Station1: xxxx\nStation2: yyy";
    }

}
