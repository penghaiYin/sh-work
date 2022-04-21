package com.yph.shwork.fuiou.common;

import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.utils.DateUtils;

public class Common20ReqParam {
    /**
     * 必填
     */
    private String mchntCd; //富友支付分配给各合作商户的唯一识别码
    private String charset = FyConstants.Version20.CHARSET; //请求使用的编码格式，如utf-8,gbk,gb2312等
    private String sign;    //商户请求参数的签名串
    private String timestamp = DateUtils.getDate("yyyy-MM-dd HH:mm:ss");   //发送请求的时间，格式"YYYY-MM-DD HH:MM:SS"
    private String version = FyConstants.Version20.CALL_VER; //调用的接口版本，固定为：2.0

    /**
     * 非必填
     */
    private String format;  //仅支持JSON
    private String signType;    //商户生成签名字符串所使用的签名算法类型，目前支持MD5，后续支持RSA
    private String lang;    //响应码描述语言，不传默认中文 zh：中文 en：英文

    public String getMchntCd() {
        return mchntCd;
    }

    public void setMchntCd(String mchntCd) {
        this.mchntCd = mchntCd;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
