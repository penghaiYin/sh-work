package com.yph.shwork.pingpong.entity;

public class GetStoreInfoDetailResponse extends CommonResponse{
    private GetStoreInfoDetailData data;

    public GetStoreInfoDetailData getData() {
        return data;
    }

    public void setData(GetStoreInfoDetailData data) {
        this.data = data;
    }
}
