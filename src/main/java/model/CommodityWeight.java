package model;

import com.datastax.driver.core.Row;

public class CommodityWeight {

    private Integer commodityWeight;

    public Integer getCommodityWeight() {
        return commodityWeight;
    }

    public CommodityWeight(Row record) {
        try{
        commodityWeight = record.getInt("commodity_weight");
        }catch(Exception e){
            //log.error
        }
    }

}
