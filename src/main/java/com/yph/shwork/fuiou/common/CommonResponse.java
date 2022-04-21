package com.yph.shwork.fuiou.common;

import java.io.Serializable;
import java.util.List;

public class CommonResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String rspCd;
    private String rspDesc; //SUCCESS：成功；SYSTEMERROR：接口错误；PARAMETERERROR：参数不正确
    private List<String> errorList;

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
}
