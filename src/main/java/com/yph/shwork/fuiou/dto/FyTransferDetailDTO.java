package com.yph.shwork.fuiou.dto;

import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyTransferDetail;

public class FyTransferDetailDTO extends FyTransferDetail {
    private String fyMchntCd;

    private String fySecretKey;

    public String getFyMchntCd() {
        return fyMchntCd;
    }

    public void setFyMchntCd(String fyMchntCd) {
        this.fyMchntCd = fyMchntCd;
    }

    public String getFySecretKey() {
        return fySecretKey;
    }

    public void setFySecretKey(String fySecretKey) {
        this.fySecretKey = fySecretKey;
    }
}
