package com.yph.shwork.fuiou.entity.res;

import com.oigbuy.finance.fuiou.common.CommonResponse;

public class FyOrderSubmitResEntity extends CommonResponse {
    private String mchntCd;
    // 机构商户必填且二选一
    private String subCustNo;  //子客户号
    private String orderId;    //商户订单号
    private String orderNo;    //富友订单号
    private String payPrice;    //参考结汇汇率
    private String amtEstimate;   //结汇金额估算
    private String calcDesc;    //计费套餐
    private String outCurCd;   //结汇币种
    private String chargeMoney;   //富友服务费
    private String outTxnAmt;   //应付总金额
    private String bankBicCode;   //富友收款账户开户行SWIFTCODE
    private String bankNm;   //富友收款账户开户行名称
    private String bankAddress;     //富友收款账户开户行地址
    private String fuiouAcntNo;    //富友收款账户卡号
    private String fuiouAcntNm;    //富友收款账户户名
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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(String payPrice) {
        this.payPrice = payPrice;
    }

    public String getAmtEstimate() {
        return amtEstimate;
    }

    public void setAmtEstimate(String amtEstimate) {
        this.amtEstimate = amtEstimate;
    }

    public String getCalcDesc() {
        return calcDesc;
    }

    public void setCalcDesc(String calcDesc) {
        this.calcDesc = calcDesc;
    }

    public String getOutCurCd() {
        return outCurCd;
    }

    public void setOutCurCd(String outCurCd) {
        this.outCurCd = outCurCd;
    }

    public String getChargeMoney() {
        return chargeMoney;
    }

    public void setChargeMoney(String chargeMoney) {
        this.chargeMoney = chargeMoney;
    }

    public String getOutTxnAmt() {
        return outTxnAmt;
    }

    public void setOutTxnAmt(String outTxnAmt) {
        this.outTxnAmt = outTxnAmt;
    }

    public String getBankBicCode() {
        return bankBicCode;
    }

    public void setBankBicCode(String bankBicCode) {
        this.bankBicCode = bankBicCode;
    }

    public String getBankNm() {
        return bankNm;
    }

    public void setBankNm(String bankNm) {
        this.bankNm = bankNm;
    }

    public String getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(String bankAddress) {
        this.bankAddress = bankAddress;
    }

    public String getFuiouAcntNo() {
        return fuiouAcntNo;
    }

    public void setFuiouAcntNo(String fuiouAcntNo) {
        this.fuiouAcntNo = fuiouAcntNo;
    }

    public String getFuiouAcntNm() {
        return fuiouAcntNm;
    }

    public void setFuiouAcntNm(String fuiouAcntNm) {
        this.fuiouAcntNm = fuiouAcntNm;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
