package com.yph.shwork.fuiou.entity.res;

import com.oigbuy.finance.fuiou.common.Common20Response;
import com.oigbuy.finance.fuiou.vo.AccountBalanceVO;

import java.util.List;

public class FyBalanceQueryResEntity extends Common20Response {

    private String fuiouSubCustNo;
    private String mchntSubCustNo;
    private String mainAccountNm;
    private String mainAccountId;
    private List<AccountBalanceVO> accountBalanceList;

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

    public List<AccountBalanceVO> getAccountBalanceList() {
        return accountBalanceList;
    }

    public void setAccountBalanceList(List<AccountBalanceVO> accountBalanceList) {
        this.accountBalanceList = accountBalanceList;
    }
}
