package com.yph.shwork.jdy.vo;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

public class JdyLogisticFeeVO implements Serializable {
    private static final long serialVersionUID = 210687679027253394L;

    private Integer id;

    @ApiModelProperty("调拨单号")
    private String allotNo;

    @ApiModelProperty("供应商名称")
    private String supplierName;

    @ApiModelProperty("简道云取值节点")
    private String valueNode;

    @ApiModelProperty("简道云费用名称")
    private String feeName;

    @ApiModelProperty("odoo费用名称")
    private String odooFeeName;

    @ApiModelProperty("简道云流水号")
    private String serialNo;

    @ApiModelProperty("报销单提交时间")
    private String expenseAccountSubmitDate;

    @ApiModelProperty("odoo累计成本调整")
    private String odooCostAdjust;

    @ApiModelProperty("odoo成本调整币种")
    private String odooCostCurrency;

    @ApiModelProperty("成本调整汇率")
    private String costExchangeRate;

    @ApiModelProperty("odoo累计成本调整（人民币）")
    private String odooCostAdjustRmb;

    @ApiModelProperty("本次报销金额")
    private String expenseAmount;

    @ApiModelProperty("报销币种")
    private String expenseCurrency;

    @ApiModelProperty("本次报销汇率")
    private String expenseExchangeRate;

    @ApiModelProperty("累计实际报销金额")
    private String actualExpenseAmount;

    @ApiModelProperty("累计实际报销金额（人民币）")
    private String actualExpenseRmb;

    @ApiModelProperty("累计报销-odoo累计调整")
    private String expenseSubtractCost;

    @ApiModelProperty("是否调整")
    private String adjustFlag;

    @ApiModelProperty("本次应调整金额")
    private String adjustAmount;

    @ApiModelProperty("理论值发生日期")
    private String theoreticalValueHappenDate;

    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAllotNo() {
        return allotNo;
    }

    public void setAllotNo(String allotNo) {
        this.allotNo = allotNo;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getValueNode() {
        return valueNode;
    }

    public void setValueNode(String valueNode) {
        this.valueNode = valueNode;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public String getOdooFeeName() {
        return odooFeeName;
    }

    public void setOdooFeeName(String odooFeeName) {
        this.odooFeeName = odooFeeName;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getExpenseAccountSubmitDate() {
        return expenseAccountSubmitDate;
    }

    public void setExpenseAccountSubmitDate(String expenseAccountSubmitDate) {
        this.expenseAccountSubmitDate = expenseAccountSubmitDate;
    }

    public String getOdooCostAdjust() {
        return odooCostAdjust;
    }

    public void setOdooCostAdjust(String odooCostAdjust) {
        this.odooCostAdjust = odooCostAdjust;
    }

    public String getOdooCostCurrency() {
        return odooCostCurrency;
    }

    public void setOdooCostCurrency(String odooCostCurrency) {
        this.odooCostCurrency = odooCostCurrency;
    }

    public String getCostExchangeRate() {
        return costExchangeRate;
    }

    public void setCostExchangeRate(String costExchangeRate) {
        this.costExchangeRate = costExchangeRate;
    }

    public String getOdooCostAdjustRmb() {
        return odooCostAdjustRmb;
    }

    public void setOdooCostAdjustRmb(String odooCostAdjustRmb) {
        this.odooCostAdjustRmb = odooCostAdjustRmb;
    }

    public String getExpenseAmount() {
        return expenseAmount;
    }

    public void setExpenseAmount(String expenseAmount) {
        this.expenseAmount = expenseAmount;
    }

    public String getExpenseCurrency() {
        return expenseCurrency;
    }

    public void setExpenseCurrency(String expenseCurrency) {
        this.expenseCurrency = expenseCurrency;
    }

    public String getExpenseExchangeRate() {
        return expenseExchangeRate;
    }

    public void setExpenseExchangeRate(String expenseExchangeRate) {
        this.expenseExchangeRate = expenseExchangeRate;
    }

    public String getActualExpenseAmount() {
        return actualExpenseAmount;
    }

    public void setActualExpenseAmount(String actualExpenseAmount) {
        this.actualExpenseAmount = actualExpenseAmount;
    }

    public String getActualExpenseRmb() {
        return actualExpenseRmb;
    }

    public void setActualExpenseRmb(String actualExpenseRmb) {
        this.actualExpenseRmb = actualExpenseRmb;
    }

    public String getExpenseSubtractCost() {
        return expenseSubtractCost;
    }

    public void setExpenseSubtractCost(String expenseSubtractCost) {
        this.expenseSubtractCost = expenseSubtractCost;
    }

    public String getAdjustFlag() {
        return adjustFlag;
    }

    public void setAdjustFlag(String adjustFlag) {
        this.adjustFlag = adjustFlag;
    }

    public String getAdjustAmount() {
        return adjustAmount;
    }

    public void setAdjustAmount(String adjustAmount) {
        this.adjustAmount = adjustAmount;
    }

    public String getTheoreticalValueHappenDate() {
        return theoreticalValueHappenDate;
    }

    public void setTheoreticalValueHappenDate(String theoreticalValueHappenDate) {
        this.theoreticalValueHappenDate = theoreticalValueHappenDate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
