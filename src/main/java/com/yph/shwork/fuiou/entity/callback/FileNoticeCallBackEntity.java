package com.yph.shwork.fuiou.entity.callback;

import java.io.Serializable;

public class FileNoticeCallBackEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String reqCd;   //上传文件处理结果代码
    private String reqDesc; //上传处理结果描述
    private String fileNm;  //处理的文件名
    private String orderNo; //富友订单号

    public String getReqCd() {
        return reqCd;
    }

    public void setReqCd(String reqCd) {
        this.reqCd = reqCd;
    }

    public String getReqDesc() {
        return reqDesc;
    }

    public void setReqDesc(String reqDesc) {
        this.reqDesc = reqDesc;
    }

    public String getFileNm() {
        return fileNm;
    }

    public void setFileNm(String fileNm) {
        this.fileNm = fileNm;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    @Override
    public String toString() {
        return "{" +
                "reqCd='" + reqCd + '\'' +
                ", reqDesc='" + reqDesc + '\'' +
                ", fileNm='" + fileNm + '\'' +
                ", orderNo='" + orderNo + '\'' +
                '}';
    }
}
