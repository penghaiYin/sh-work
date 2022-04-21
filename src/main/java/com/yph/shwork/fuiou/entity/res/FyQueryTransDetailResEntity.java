package com.yph.shwork.fuiou.entity.res;

import com.oigbuy.finance.fuiou.common.Common20Response;

import java.util.List;

public class FyQueryTransDetailResEntity extends Common20Response {
    private String hasNextPage;
    private Integer pageNo;
    private Integer pageSize;
    private String accountType;
    private String fuiouSubCustNo;
    private String mchntSubCustNo;
    private String mainAccountNm;
    private String mainAccountId;
    private List<ResultVO> resultList;

    public String getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(String hasNextPage) {
        this.hasNextPage = hasNextPage;
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

    public String getMainAccountNm() {
        return mainAccountNm;
    }

    public void setMainAccountNm(String mainAccountNm) {
        this.mainAccountNm = mainAccountNm;
    }

    public String getMainAccountId() {
        return mainAccountId;
    }

    public void setMainAccountId(String mainAccountId) {
        this.mainAccountId = mainAccountId;
    }

    public List<ResultVO> getResultList() {
        return resultList;
    }

    public void setResultList(List<ResultVO> resultList) {
        this.resultList = resultList;
    }
}
