package com.yph.shwork.fuiou.entity.res;

import com.oigbuy.finance.fuiou.common.CommonResponse;

public class FyFileNotifyResEntity extends CommonResponse {
    private String mchntCd;
    private String fileTp;  //与请求参数一致
    private String fileNm;    //处理的文件名
    private String processSt;    //1111：处理中
    private String note;    //处理信息
    private String md5;

    public String getMchntCd() {
        return mchntCd;
    }

    public void setMchntCd(String mchntCd) {
        this.mchntCd = mchntCd;
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

    public String getProcessSt() {
        return processSt;
    }

    public void setProcessSt(String processSt) {
        this.processSt = processSt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
