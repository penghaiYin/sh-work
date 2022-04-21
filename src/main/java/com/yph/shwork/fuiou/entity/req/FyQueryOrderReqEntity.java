package com.yph.shwork.fuiou.entity.req;

import java.io.Serializable;

public class FyQueryOrderReqEntity implements Serializable {
    private String mchntCd;
    private String subCustNo;
    private String backNotifyUrl;  //通知地址
    private Integer pageNo;  //查询页号
    private String startTime;    //查询开始时间
    private String endTime;    //查询结束时间
    private String orderNo;    //富友订单号
    private String orderId;    //商户订单号
    private Integer pageSize;    //分页大小
    private String txnTp;    //交易类型
    private String md5;

    private static final long serialVersionUID = 1L;

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

    public String getBackNotifyUrl() {
        return backNotifyUrl;
    }

    public void setBackNotifyUrl(String backNotifyUrl) {
        this.backNotifyUrl = backNotifyUrl;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTxnTp() {
        return txnTp;
    }

    public void setTxnTp(String txnTp) {
        this.txnTp = txnTp;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
