package com.yph.shwork.fuiou.entity.callback;

import com.oigbuy.finance.fuiou.common.Common20Response;

public class BankTransferCallBackEntity extends Common20Response {
    private String mchntOrderId; //商户订单号
    private String fuiouTransNo; //富友交易流水号
    private String totalAmt; //交易总金额
    private String actualAmt; //实际到账金额
    private String serviceFee; //手续费
    private String feeSource; //手续费承担方

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
