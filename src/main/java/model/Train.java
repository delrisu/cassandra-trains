package model;

import com.datastax.driver.core.Row;

public class Train {
  private String trainId;
  private String trainName;
  private String stationId;

  public Train(Row record) {
    try {
      trainId = record.getString("train_id");
      trainName = record.getString("train_name");
      stationId = record.getString("station_id");
    } catch (Exception e) {
      //log.error
    }
  }

  public String getTrainId() {
    return trainId;
  }

  public String getTrainName() {
    return trainName;
  }

  public String getStationId() {
    return stationId;
  }

  @Override
  public String toString() {
    return "Train{" +
        "trainId='" + trainId + '\'' +
        ", trainName='" + trainName + '\'' +
        ", stationId='" + stationId + '\'' +
        '}';
  }
}
