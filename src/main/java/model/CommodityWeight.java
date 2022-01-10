package model;

import com.datastax.driver.core.Row;

public class CommodityWeight {

  private Integer commodityWeight;

  public CommodityWeight(Row record) {
    try {
      commodityWeight = record.getInt("commodity_weight");
    } catch (Exception e) {
      //log.error
    }
  }

  public Integer getCommodityWeight() {
    return commodityWeight;
  }

  @Override
  public String toString() {
    return "CommodityWeight{" +
        "commodityWeight=" + commodityWeight +
        '}';
  }
}
