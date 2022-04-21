package com.yph.shwork.pingpong.entity;

import com.oigbuy.pingpong.dto.StoreInfo;

import java.io.Serializable;
import java.util.List;

public class GetStoreInfoData implements Serializable {
    private Integer pg_no;
    private Integer total_pg;
    private List<StoreInfo> storeInfoList;
    private static final long serialVersionUID = 1L;

    public Integer getPg_no() {
        return pg_no;
    }

    public void setPg_no(Integer pg_no) {
        this.pg_no = pg_no;
    }

    public Integer getTotal_pg() {
        return total_pg;
    }

    public void setTotal_pg(Integer total_pg) {
        this.total_pg = total_pg;
    }

    public List<StoreInfo> getStoreInfoList() {
        return storeInfoList;
    }

    public void setStoreInfoList(List<StoreInfo> storeInfoList) {
        this.storeInfoList = storeInfoList;
    }
}
