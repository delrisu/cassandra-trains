package model;

import com.datastax.driver.core.Row;

public class Station {
  private String stationId;
  private String stationName;

  public Station(Row record) {
    try {
      stationId = record.getString("station_id");
      stationName = record.getString("station_name");
    } catch (Exception e) {
      //log.error
    }
  }

  public String getStationId() {
    return stationId;
  }

  public String getStationName() {
    return stationName;
  }

  @Override
  public String toString() {
    return "Station{" +
        "stationId='" + stationId + '\'' +
        ", stationName='" + stationName + '\'' +
        '}';
  }
}
