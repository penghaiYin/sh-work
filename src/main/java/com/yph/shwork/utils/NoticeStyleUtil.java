package com.yph.shwork.utils;

/**
 * @author: sheng.wang
 * @date: 2021/12/09  16:54
 * @description:
 */
public class NoticeStyleUtil {

    /**
     * 数量超过0显示为红色
     * @param num
     * @return
     */
    public static String transferColorRed(int num){
        String str = "";
        if (num == 0) {
            str = String.valueOf(num);
        }else{
            str = "**<font color=#FF0000 face='黑体'>"+ num + "</font>**";
        }
        return str;
    }
}
