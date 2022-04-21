package com.yph.shwork.fuiou.entity.req;

import java.io.Serializable;

public class FyFileNotifyReqEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mchntCd;
    private String backNotifyUrl;  //异步接收富友文件处理结果通知
    private String fileTp;  //1：跨境收汇明细文件；2：跨境付汇明细文件
    private String fileNm;    //文件名称，不包含FTP路径，必须以.txt结尾，文件名不包含中文及特殊字符
    private String orderNo;    //富友订单号
    private String md5;

    public String getMchntCd() {
        return mchntCd;
    }

    public void setMchntCd(String mchntCd) {
        this.mchntCd = mchntCd;
    }

    public String getBackNotifyUrl() {
        return backNotifyUrl;
    }

    public void setBackNotifyUrl(String backNotifyUrl) {
        this.backNotifyUrl = backNotifyUrl;
    }

    public String getFileTp() {
        return fileTp;
    }

    public void setFileTp(String fileTp) {
        this.fileTp = fileTp;
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

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
