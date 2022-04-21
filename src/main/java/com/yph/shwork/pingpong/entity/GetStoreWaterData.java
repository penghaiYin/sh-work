package com.yph.shwork.pingpong.entity;

import com.oigbuy.pingpong.dto.StoreWater;

import java.io.Serializable;
import java.util.List;

public class GetStoreWaterData implements Serializable {
    private List<StoreWater> flowList;

    private static final long serialVersionUID = 1L;

    public List<StoreWater> getFlowList() {
        return flowList;
    }

    public void setFlowList(List<StoreWater> flowList) {
        this.flowList = flowList;
    }
}
