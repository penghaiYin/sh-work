package com.yph.shwork.pingpong.entity;

public class GetStoreInfoResponse extends CommonResponse{
    private GetStoreInfoData data;

    public GetStoreInfoData getData() {
        return data;
    }

    public void setData(GetStoreInfoData data) {
        this.data = data;
    }
}
