package com.yph.shwork.fuiou.entity.res;

import com.oigbuy.finance.fuiou.common.Common20Response;

public class FyBankTransferResEntity extends Common20Response {

    private String fuiouSubCustNo;
    private String mchntSubCustNo;
    private String mchntOrderId;
    private String fuiouTransNo;
    private String totalAmt;
    private String actualAmt;
    private String serviceFee;
    private String feeSource;

    public String getFuiouSubCustNo() {
        return fuiouSubCustNo;
    }

    public void setFuiouSubCustNo(String fuiouSubCustNo) {
        this.fuiouSubCustNo = fuiouSubCustNo;
    }

    public String getMchntSubCustNo() {
        return mchntSubCustNo;
    }

    public void setMchntSubCustNo(String mchntSubCustNo) {
        this.mchntSubCustNo = mchntSubCustNo;
    }

    public String getMchntOrderId() {
        return mchntOrderId;
    }

    public void setMchntOrderId(String mchntOrderId) {
        this.mchntOrderId = mchntOrderId;
    }

    public String getFuiouTransNo() {
        return fuiouTransNo;
    }

    public void setFuiouTransNo(String fuiouTransNo) {
        this.fuiouTransNo = fuiouTransNo;
    }

    public String getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getActualAmt() {
        return actualAmt;
    }

    public void setActualAmt(String actualAmt) {
        this.actualAmt = actualAmt;
    }

    public String getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(String serviceFee) {
        this.serviceFee = serviceFee;
    }

    public String getFeeSource() {
        return feeSource;
    }

    public void setFeeSource(String feeSource) {
        this.feeSource = feeSource;
    }
}
