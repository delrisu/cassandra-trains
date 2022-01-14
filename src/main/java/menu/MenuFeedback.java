package menu;

public class MenuFeedback {
    public static final String LOADING_DATA_SUCCESS = "Loading data is done!";
    public static final String ADD_TRAIN_SUCCESS = "Train %s with id: %s has been added to station with id: %s";
    public static final String DELETE_TRAIN_SUCCESS = "Train %s with id: %s has been deleted";
    public static final String GET_TRAINS_NO_TRAINS = "No trains available :(";
    public static final String GET_TRAIN_NO_TRAIN = "No train with given id";
    public static final String GET_STATION_NO_STATION = "No station with given id";
    public static final String MISSING_TRAIN_STATION = "Train station is missing";
    public static final String MISSING_TRAIN = "Train no longer exist in system";
    public static final String UNLOAD_TRAIN_NOT_ENOUGH_LOAD = "Current train load weight is not sufficient for request";
    public static final String LOAD_TRAIN_NOT_ENOUGH_COMMODITY = "Current station warehouse commodity volume is not sufficient for request";
    public static final String LOAD_UNLOAD_TRAIN_NEGATIVE_LOAD = "Operation cancelled due to race for commodity resources";
    public static final String LOAD_UNLOAD_TRAIN_CHANGED_STATION = "Operation cancelled due to change of train station";
    public static final String LOAD_UNLOAD_TRAIN_DELETED_STATION = "Operation cancelled due to deletion of train station";
    public static final String UNLOAD_TRAIN_SUCCESS = "Moved %dt of %s from train with id: %s to warehouse near the station with id: %s";
    public static final String LOAD_TRAIN_SUCCESS = "Moved %dt of %s from warehouse near station with id: %s to train with id: %s";
    public static final String MOVE_TRAIN_SUCCESS = "Moved train %s with id: %s from station with id %s to station with id: %s";
    public static final String INSERT_COMMODITY_SUCCESS = "%dt of %s has been inserted into train with id: %s";
    public static final String ADD_STATION_SUCCESS = "Station %s with id: %s has been added";
    public static final String DELETE_STATION_SUCCESS = "Deleted station with id: %s";
    public static final String GET_STATIONS_NO_STATION = "No stations available";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong! :(";
}
