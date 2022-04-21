package com.yph.shwork.fuiou.entity.req;

import com.oigbuy.finance.fuiou.common.Common20ReqParam;

import java.io.Serializable;

public class FyQueryTransDetailReqEntity extends Common20ReqParam implements Serializable {
    private static final long serialVersionUID = 1L;
    private String randomStr;
    private String accountType;
    private String fuiouSubCustNo;
    private String mchntSubCustNo;
    private Integer pageNo;
    private String startTime;
    private String endTime;
    private String fuiouTransNo;
    private String mchntOrderId;
    private Integer pageSize;
    private String transState;
    private String balanceDirection;
    private String transType;

    public String getRandomStr() {
        return randomStr;
    }

    public void setRandomStr(String randomStr) {
        this.randomStr = randomStr;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

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



    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFuiouTransNo() {
        return fuiouTransNo;
    }

    public void setFuiouTransNo(String fuiouTransNo) {
        this.fuiouTransNo = fuiouTransNo;
    }

    public String getMchntOrderId() {
        return mchntOrderId;
    }

    public void setMchntOrderId(String mchntOrderId) {
        this.mchntOrderId = mchntOrderId;
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

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
