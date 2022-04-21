package com.yph.shwork.fuiou.entity.res;

import java.io.Serializable;
import java.math.BigDecimal;

public class ResultVO  implements Serializable {
    private static final long serialVersionUID = 1L;
    private String orderSubmitType;
    private String mchntOrderId;
    private String fuiouTransNo;
    private String oppAccountId;
    private String oppAccountNm;
    private String oppBankCardNo;
    private Integer totalAmt;
    private Integer orderAmt;
    private Integer serviceFee;
    private BigDecimal rate;
    private String transState;
    private String balanceDirection;
    private String transType;
    private String transTime;
    private String remark;
    private String failReason;

    public String getOrderSubmitType() {
        return orderSubmitType;
    }

    public void setOrderSubmitType(String orderSubmitType) {
        this.orderSubmitType = orderSubmitType;
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

    public String getOppAccountId() {
        return oppAccountId;
    }

    public void setOppAccountId(String oppAccountId) {
        this.oppAccountId = oppAccountId;
    }

    public String getOppAccountNm() {
        return oppAccountNm;
    }

    public void setOppAccountNm(String oppAccountNm) {
        this.oppAccountNm = oppAccountNm;
    }

    public String getOppBankCardNo() {
        return oppBankCardNo;
    }

    public void setOppBankCardNo(String oppBankCardNo) {
        this.oppBankCardNo = oppBankCardNo;
    }

    public Integer getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Integer totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Integer getOrderAmt() {
        return orderAmt;
    }

    public void setOrderAmt(Integer orderAmt) {
        this.orderAmt = orderAmt;
    }

    public Integer getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(Integer serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getTransState() {
        return transState;
    }

    public void setTransState(String transState) {
        this.transState = transState;
    }

    public String getBalanceDirection() {
        return balanceDirection;
    }

    public void setBalanceDirection(String balanceDirection) {
        this.balanceDirection = balanceDirection;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
