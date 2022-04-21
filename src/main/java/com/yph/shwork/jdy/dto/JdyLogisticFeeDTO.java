package com.yph.shwork.jdy.dto;

import com.oigbuy.common.pojo.check.capital.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

public class JdyLogisticFeeDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("调拨单号")
    private String allotNo;

    @ApiModelProperty("供应商名称")
    private String supplierName;

    @ApiModelProperty("简道云取值节点")
    private String valueNode;

    @ApiModelProperty("简道云流水号")
    private String serialNo;

    @ApiModelProperty("是否调整：是或否")
    private String adjustFlag;

    @ApiModelProperty("时间类型:1 报销单提交时间; 2 理论费用时间; 3 更新时间")
    private Integer dateType;

    @ApiModelProperty(value = "开始时间，pattern=yyyy-MM-dd")
    private String dateStart;

    @ApiModelProperty(value = "结束时间，pattern=yyyy-MM-dd")
    private String dateEnd;


    @ApiModelProperty(hidden = true)
    private Date dateStartDate;

    @ApiModelProperty(hidden = true)
    private Date dateEndDate;

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

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getAdjustFlag() {
        return adjustFlag;
    }

    public void setAdjustFlag(String adjustFlag) {
        this.adjustFlag = adjustFlag;
    }

    public Integer getDateType() {
        return dateType;
    }

    public void setDateType(Integer dateType) {
        this.dateType = dateType;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Date getDateStartDate() {
        return dateStartDate;
    }

    public void setDateStartDate(Date dateStartDate) {
        this.dateStartDate = dateStartDate;
    }

    public Date getDateEndDate() {
        return dateEndDate;
    }

    public void setDateEndDate(Date dateEndDate) {
        this.dateEndDate = dateEndDate;
    }
}
