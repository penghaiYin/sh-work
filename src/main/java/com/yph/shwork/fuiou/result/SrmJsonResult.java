package com.yph.shwork.fuiou.result;

import java.io.Serializable;

/**
 * 
 * @Description
 * @author penghai.yin
 * @date 2022年3月25日15:09:15
 */
public class SrmJsonResult<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	private String code;
    private String message;
    private Boolean success;
    private T datas;

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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getDatas() {
        return datas;
    }

    public void setDatas(T datas) {
        this.datas = datas;
    }

    @Override
    public String toString() {
        return "SrmJsonResult{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", success=" + success +
                ", datas=" + datas +
                '}';
    }
}
