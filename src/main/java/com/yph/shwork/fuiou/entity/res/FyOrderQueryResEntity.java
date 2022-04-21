package com.yph.shwork.fuiou.entity.res;

import java.io.Serializable;

public class FyOrderQueryResEntity implements Serializable {
    private String subCustNo;  //子客户号
    private String txnTp;    //商户订单号
    private String orderDate;    //富友订单号
    private String orderConvertTs;    //参考结汇汇率
    private String orderUptTs;   //结汇金额估算
    private String orderNo;    //计费套餐
    private String orderId;   //结汇币种
    private String orderTp;   //富友服务费
    private String settleAccountsTp;   //应付总金额
    private String payTp;   //富友收款账户开户行SWIFTCODE
    private String outAcntNo;   //富友收款账户开户行名称
    private String outAcntNm;     //富友收款账户开户行地址
    private String outAcntBankNm;    //富友收款账户卡号
    private String outCurCd;    //富友收款账户户名
    private String inAcntNo;    //富友收款账户户名
    private String inAcntNm;
    private String inAcntBankNm;
    private String inAcntSwiftCode;
    private String inAcntBankAddr;
    private String inAcntCountry;
    private String inCurCd;
    private String payPrice;
    private Integer inTxnAmt;
    private String calcDesc;
    private Integer chargeMoney;
    private Integer outTxnAmt;
    private String orderState;
    private String approveNote;

    private static final long serialVersionUID = 1L;

    public String getSubCustNo() {
        return subCustNo;
    }

    public void setSubCustNo(String subCustNo) {
        this.subCustNo = subCustNo;
    }

    public String getTxnTp() {
        return txnTp;
    }

    public void setTxnTp(String txnTp) {
        this.txnTp = txnTp;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderConvertTs() {
        return orderConvertTs;
    }

    public void setOrderConvertTs(String orderConvertTs) {
        this.orderConvertTs = orderConvertTs;
    }

    public String getOrderUptTs() {
        return orderUptTs;
    }

    public void setOrderUptTs(String orderUptTs) {
        this.orderUptTs = orderUptTs;
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

    public String getOrderTp() {
        return orderTp;
    }

    public void setOrderTp(String orderTp) {
        this.orderTp = orderTp;
    }

    public String getSettleAccountsTp() {
        return settleAccountsTp;
    }

    public void setSettleAccountsTp(String settleAccountsTp) {
        this.settleAccountsTp = settleAccountsTp;
    }

    public String getPayTp() {
        return payTp;
    }

    public void setPayTp(String payTp) {
        this.payTp = payTp;
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

    public String getOutCurCd() {
        return outCurCd;
    }

    public void setOutCurCd(String outCurCd) {
        this.outCurCd = outCurCd;
    }

    public String getInAcntNo() {
        return inAcntNo;
    }

    public void setInAcntNo(String inAcntNo) {
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

    public String getInAcntSwiftCode() {
        return inAcntSwiftCode;
    }

    public void setInAcntSwiftCode(String inAcntSwiftCode) {
        this.inAcntSwiftCode = inAcntSwiftCode;
    }

    public String getInAcntBankAddr() {
        return inAcntBankAddr;
    }

    public void setInAcntBankAddr(String inAcntBankAddr) {
        this.inAcntBankAddr = inAcntBankAddr;
    }

    public String getInAcntCountry() {
        return inAcntCountry;
    }

    public void setInAcntCountry(String inAcntCountry) {
        this.inAcntCountry = inAcntCountry;
    }

    public String getInCurCd() {
        return inCurCd;
    }

    public void setInCurCd(String inCurCd) {
        this.inCurCd = inCurCd;
    }

    public String getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(String payPrice) {
        this.payPrice = payPrice;
    }

    public Integer getInTxnAmt() {
        return inTxnAmt;
    }

    public void setInTxnAmt(Integer inTxnAmt) {
        this.inTxnAmt = inTxnAmt;
    }

    public String getCalcDesc() {
        return calcDesc;
    }

    public void setCalcDesc(String calcDesc) {
        this.calcDesc = calcDesc;
    }

    public Integer getChargeMoney() {
        return chargeMoney;
    }

    public void setChargeMoney(Integer chargeMoney) {
        this.chargeMoney = chargeMoney;
    }

    public Integer getOutTxnAmt() {
        return outTxnAmt;
    }

    public void setOutTxnAmt(Integer outTxnAmt) {
        this.outTxnAmt = outTxnAmt;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }

    public String getApproveNote() {
        return approveNote;
    }

    public void setApproveNote(String approveNote) {
        this.approveNote = approveNote;
    }
}
