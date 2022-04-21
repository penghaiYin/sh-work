package com.yph.shwork.pingpong.entity;

import java.io.Serializable;

public class CommonResponse implements Serializable {
    private String apiName;
    private String code;
    private String message;
    private static final long serialVersionUID = 1L;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
