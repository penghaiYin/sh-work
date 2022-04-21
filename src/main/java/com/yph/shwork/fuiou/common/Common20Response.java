package com.yph.shwork.fuiou.common;

import java.io.Serializable;
import java.util.List;

public class Common20Response implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 公共响应参数
     */
    private String rspCd;
    private String rspDesc; //SUCCESS：成功；SYSTEMERROR：接口错误；PARAMETERERROR：参数不正确
    private List<String> errorList;

    /**
     * rspCd为SUCCESS的时候有返回
     */
    private String mchntCd;
    private String randomStr;
    private String sign;
    private String resultCode;
    private String errCode;
    private String errCodeDes;

    public String getRspCd() {
        return rspCd;
    }

    public void setRspCd(String rspCd) {
        this.rspCd = rspCd;
    }

    public String getRspDesc() {
        return rspDesc;
    }

    public void setRspDesc(String rspDesc) {
        this.rspDesc = rspDesc;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
    }

    public String getMchntCd() {
        return mchntCd;
    }

    public void setMchntCd(String mchntCd) {
        this.mchntCd = mchntCd;
    }

    public String getRandomStr() {
        return randomStr;
    }

    public void setRandomStr(String randomStr) {
        this.randomStr = randomStr;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrCodeDes() {
        return errCodeDes;
    }

    public void setErrCodeDes(String errCodeDes) {
        this.errCodeDes = errCodeDes;
    }
}
