package com.yph.shwork.pingpong.entity;

public class GetWithdrawDetailResponse extends CommonResponse{
    private GetWithdrawDetailData data;

    public GetWithdrawDetailData getData() {
        return data;
    }

    public void setData(GetWithdrawDetailData data) {
        this.data = data;
    }
}
