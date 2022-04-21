package com.yph.shwork.fuiou.entity.req;

import java.io.Serializable;

public class FyOrderSubmitReqEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mchntCd;

    private String subCustNo;  //子客户号

    private String orderId;

    private String backNotifyUrl;

    private String txnTp;

    private String settleAccountsTp;

    private String outAcntNo;

    private String outAcntNm;

    private String outAcntBankNm;

    private String countryCd;

    private String bankCountryCd;

    private String outCurCd;

    private Integer inAcntNo;

    private String inAcntNm;

    private String inAcntBankNm;

    private Integer bankNo;

    private Integer cityNo;

    private String orderTp;

    private String orderType;

    private Integer orderAmt;

    private String ver;

    private String md5;

    public String getMchntCd() {
        return mchntCd;
    }

    public void setMchntCd(String mchntCd) {
        this.mchntCd = mchntCd;
    }

    public String getSubCustNo() {
        return subCustNo;
    }

    public void setSubCustNo(String subCustNo) {
        this.subCustNo = subCustNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBackNotifyUrl() {
        return backNotifyUrl;
    }

    public void setBackNotifyUrl(String backNotifyUrl) {
        this.backNotifyUrl = backNotifyUrl;
    }

    public String getTxnTp() {
        return txnTp;
    }

    public void setTxnTp(String txnTp) {
        this.txnTp = txnTp;
    }

    public String getSettleAccountsTp() {
        return settleAccountsTp;
    }

    public void setSettleAccountsTp(String settleAccountsTp) {
        this.settleAccountsTp = settleAccountsTp;
    }

    public String getOutAcntNo() {
        return outAcntNo;
    }

    public void setOutAcntNo(String outAcntNo) {
        this.outAcntNo = outAcntNo;
    }

    public String getOutAcntNm() {
        return outAcntNm;
    }

    public void setOutAcntNm(String outAcntNm) {
        this.outAcntNm = outAcntNm;
    }

    public String getOutAcntBankNm() {
        return outAcntBankNm;
    }

    public void setOutAcntBankNm(String outAcntBankNm) {
        this.outAcntBankNm = outAcntBankNm;
    }

    public String getCountryCd() {
        return countryCd;
    }

    public void setCountryCd(String countryCd) {
        this.countryCd = countryCd;
    }

    public String getBankCountryCd() {
        return bankCountryCd;
    }

    public void setBankCountryCd(String bankCountryCd) {
        this.bankCountryCd = bankCountryCd;
    }

    public String getOutCurCd() {
        return outCurCd;
    }

    public void setOutCurCd(String outCurCd) {
        this.outCurCd = outCurCd;
    }

    public Integer getInAcntNo() {
        return inAcntNo;
    }

    public void setInAcntNo(Integer inAcntNo) {
        this.inAcntNo = inAcntNo;
    }

    public String getInAcntNm() {
        return inAcntNm;
    }

    public void setInAcntNm(String inAcntNm) {
        this.inAcntNm = inAcntNm;
    }

    public String getInAcntBankNm() {
        return inAcntBankNm;
    }

    public void setInAcntBankNm(String inAcntBankNm) {
        this.inAcntBankNm = inAcntBankNm;
    }

    public Integer getBankNo() {
        return bankNo;
    }

    public void setBankNo(Integer bankNo) {
        this.bankNo = bankNo;
    }

    public Integer getCityNo() {
        return cityNo;
    }

    public void setCityNo(Integer cityNo) {
        this.cityNo = cityNo;
    }

    public String getOrderTp() {
        return orderTp;
    }

    public void setOrderTp(String orderTp) {
        this.orderTp = orderTp;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Integer getOrderAmt() {
        return orderAmt;
    }

    public void setOrderAmt(Integer orderAmt) {
        this.orderAmt = orderAmt;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }
}
