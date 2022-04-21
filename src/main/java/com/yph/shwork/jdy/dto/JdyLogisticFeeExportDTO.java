package com.yph.shwork.jdy.dto;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

public class JdyLogisticFeeExportDTO implements Serializable {

	private static final long serialVersionUID = 2810397747494497688L;
	@ApiModelProperty(value = "当前操作的用户", required = true)
	private String userId;

	@ApiModelProperty(value = "导出类型 '1'表示勾选导出  '2'表示全量导出", required = true)
	private String exportType;

	@ApiModelProperty(value = "全量导出时页面的查询参数")
	private JdyLogisticFeeDTO queryParam;

	@ApiModelProperty(value = "勾选导出时勾选数据的id集合")
	private List<Integer> idList;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public JdyLogisticFeeDTO getQueryParam() {
		return queryParam;
	}

	public void setQueryParam(JdyLogisticFeeDTO queryParam) {
		this.queryParam = queryParam;
	}

	public List<Integer> getIdList() {
		return idList;
	}

	public void setIdList(List<Integer> idList) {
		this.idList = idList;
	}
}