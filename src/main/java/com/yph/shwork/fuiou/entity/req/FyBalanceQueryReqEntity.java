package com.yph.shwork.fuiou.entity.req;

import com.oigbuy.finance.fuiou.common.Common20ReqParam;

import java.io.Serializable;

public class FyBalanceQueryReqEntity extends Common20ReqParam implements Serializable {
    private static final long serialVersionUID = 1L;
    private String randomStr;
    private String fuiouSubCustNo;
    private String mchntSubCustNo;
    private String accountList;
    private String accountType;

    public String getRandomStr() {
        return randomStr;
    }

    public void setRandomStr(String randomStr) {
        this.randomStr = randomStr;
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

    public String getAccountList() {
        return accountList;
    }

    public void setAccountList(String accountList) {
        this.accountList = accountList;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    @Override
    public String toString() {
        return "{" +
                "mchntCd='" + super.getMchntCd() + '\'' +
                ", charset='" + super.getCharset() + '\'' +
                ", sign='" + super.getSign() + '\'' +
                ", timestamp='" + super.getTimestamp() + '\'' +
                ", version='" + super.getVersion() + '\'' +
                ", format='" + super.getFormat() + '\'' +
                ", signType='" + super.getSignType() + '\'' +
                ", lang='" + super.getLang() + '\'' +
                "randomStr='" + randomStr + '\'' +
                ", fuiouSubCustNo='" + fuiouSubCustNo + '\'' +
                ", mchntSubCustNo='" + mchntSubCustNo + '\'' +
                ", accountList='" + accountList + '\'' +
                ", accountType='" + accountType + '\'' +
                '}';
    }
}
