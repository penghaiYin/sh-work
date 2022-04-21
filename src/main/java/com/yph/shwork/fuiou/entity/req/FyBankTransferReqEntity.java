package com.yph.shwork.fuiou.entity.req;

import com.oigbuy.finance.fuiou.common.Common20ReqParam;

import java.io.Serializable;

public class FyBankTransferReqEntity extends Common20ReqParam implements Serializable {
    private static final long serialVersionUID = 1L;
    private String randomStr;
    private String backNotifyUrl;
    private String fuiouSubCustNo;
    private String mchntSubCustNo;
    private String mchntOrderId;
    private String bankCardTp;
    private String bankCardNo;
    private String oppName;
    private String oppIdNo;
    private String bankNo;
    private String cityNo;
    private String subbranchName;
    private String CNAPSCode;
    private Integer amt;
    private String remark;
    private String isNotify;
    private String oppMobile;
    private String isNeedReview;
    private String fuiouOrderNo;


    public String getRandomStr() {
        return randomStr;
    }

    public void setRandomStr(String randomStr) {
        this.randomStr = randomStr;
    }

    public String getBackNotifyUrl() {
        return backNotifyUrl;
    }

    public void setBackNotifyUrl(String backNotifyUrl) {
        this.backNotifyUrl = backNotifyUrl;
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

    public String getMchntOrderId() {
        return mchntOrderId;
    }

    public void setMchntOrderId(String mchntOrderId) {
        this.mchntOrderId = mchntOrderId;
    }

    public String getBankCardTp() {
        return bankCardTp;
    }

    public void setBankCardTp(String bankCardTp) {
        this.bankCardTp = bankCardTp;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getOppName() {
        return oppName;
    }

    public void setOppName(String oppName) {
        this.oppName = oppName;
    }

    public String getOppIdNo() {
        return oppIdNo;
    }

    public void setOppIdNo(String oppIdNo) {
        this.oppIdNo = oppIdNo;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getCityNo() {
        return cityNo;
    }

    public void setCityNo(String cityNo) {
        this.cityNo = cityNo;
    }

    public String getSubbranchName() {
        return subbranchName;
    }

    public void setSubbranchName(String subbranchName) {
        this.subbranchName = subbranchName;
    }

    public String getCNAPSCode() {
        return CNAPSCode;
    }

    public void setCNAPSCode(String CNAPSCode) {
        this.CNAPSCode = CNAPSCode;
    }

    public Integer getAmt() {
        return amt;
    }

    public void setAmt(Integer amt) {
        this.amt = amt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getIsNotify() {
        return isNotify;
    }

    public void setIsNotify(String isNotify) {
        this.isNotify = isNotify;
    }

    public String getOppMobile() {
        return oppMobile;
    }

    public void setOppMobile(String oppMobile) {
        this.oppMobile = oppMobile;
    }

    public String getIsNeedReview() {
        return isNeedReview;
    }

    public void setIsNeedReview(String isNeedReview) {
        this.isNeedReview = isNeedReview;
    }

    public String getFuiouOrderNo() {
        return fuiouOrderNo;
    }

    public void setFuiouOrderNo(String fuiouOrderNo) {
        this.fuiouOrderNo = fuiouOrderNo;
    }
}
