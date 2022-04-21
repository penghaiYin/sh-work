package com.yph.shwork.fuiou.entity.withdrawalorder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther zhuo.lv
 * @Date 2021-11-17
 * 页面请求显示参数
 */
public class OrderWithdrawalDetail implements Serializable {

    private static final long serialVersionUID = -6434182454387818409L;
    private Integer id;

    //单据状态 0:待提交富友 1:待富友审核 2:待富友收款 3:富友已结汇 4:交易失败
    private String documentStatus;

    //富友订单号
    private String fyOrderNo;

    //PO单号
    private String poOrderCode;

    //交易日期
    private Date transactionDate;

    //结汇批次
    private String polineRkbatchNo;

    //收款人
    private String receiptName;

    //收款人证件号
    private String receiptIdCardNo;

    //交易币种
    private String transactionCurrency;

    //交易金额
    private BigDecimal transactionAmount;

    //公司
    private String company;

    //转账编号
    private String mchntMergeOrderId;

    //付款人常驻国家/地区
    private String payerAddress;

    //付款人名称
    private String payerName;

    //付款账号
    private String payerAccountNo;

    //贸易类型
    private String tradeType;

    //商品SKU
    private String productCode;

    //商品名称
    private String productName;

    //数量
    private Integer qty ;

    //单价
    private BigDecimal priceUnit;

    //商品种类")
    private String goodsCategory;

    //收款人联系电话
    private String receiptTelNumber;

    //提现主单编号
    private String withdrawalCode;

    //提现子单编号
    private String billDetailCode;

    //提现总金额
    private BigDecimal totalWithdrawalAmount;

    //提现申请时间
    private Date withdrawalTime;

    //供应商编码
    private Long supplierCode;

    //订单编号
    private String mchntDetailOrderId;

    private String fyAccountCode;

    //提交富友时间
    private Date submitTime;

    //创建时间
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getFyOrderNo() {
        return fyOrderNo;
    }

    public void setFyOrderNo(String fyOrderNo) {
        this.fyOrderNo = fyOrderNo;
    }

    public String getPoOrderCode() {
        return poOrderCode;
    }

    public void setPoOrderCode(String poOrderCode) {
        this.poOrderCode = poOrderCode;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPolineRkbatchNo() {
        return polineRkbatchNo;
    }

    public void setPolineRkbatchNo(String polineRkbatchNo) {
        this.polineRkbatchNo = polineRkbatchNo;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }

    public String getReceiptIdCardNo() {
        return receiptIdCardNo;
    }

    public void setReceiptIdCardNo(String receiptIdCardNo) {
        this.receiptIdCardNo = receiptIdCardNo;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getPayerAddress() {
        return payerAddress;
    }

    public void setPayerAddress(String payerAddress) {
        this.payerAddress = payerAddress;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public BigDecimal getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(BigDecimal priceUnit) {
        this.priceUnit = priceUnit;
    }

    public String getGoodsCategory() {
        return goodsCategory;
    }

    public void setGoodsCategory(String goodsCategory) {
        this.goodsCategory = goodsCategory;
    }

    public String getReceiptTelNumber() {
        return receiptTelNumber;
    }

    public void setReceiptTelNumber(String receiptTelNumber) {
        this.receiptTelNumber = receiptTelNumber;
    }

    public String getWithdrawalCode() {
        return withdrawalCode;
    }

    public void setWithdrawalCode(String withdrawalCode) {
        this.withdrawalCode = withdrawalCode;
    }

    public String getBillDetailCode() {
        return billDetailCode;
    }

    public void setBillDetailCode(String billDetailCode) {
        this.billDetailCode = billDetailCode;
    }

    public BigDecimal getTotalWithdrawalAmount() {
        return totalWithdrawalAmount;
    }

    public void setTotalWithdrawalAmount(BigDecimal totalWithdrawalAmount) {
        this.totalWithdrawalAmount = totalWithdrawalAmount;
    }

    public Date getWithdrawalTime() {
        return withdrawalTime;
    }

    public void setWithdrawalTime(Date withdrawalTime) {
        this.withdrawalTime = withdrawalTime;
    }

    public Long getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(Long supplierCode) {
        this.supplierCode = supplierCode;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMchntDetailOrderId() {
        return mchntDetailOrderId;
    }

    public void setMchntDetailOrderId(String mchntDetailOrderId) {
        this.mchntDetailOrderId = mchntDetailOrderId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getFyAccountCode() {
        return fyAccountCode;
    }

    public void setFyAccountCode(String fyAccountCode) {
        this.fyAccountCode = fyAccountCode;
    }

    public String getMchntMergeOrderId() {
        return mchntMergeOrderId;
    }

    public void setMchntMergeOrderId(String mchntMergeOrderId) {
        this.mchntMergeOrderId = mchntMergeOrderId;
    }
}
