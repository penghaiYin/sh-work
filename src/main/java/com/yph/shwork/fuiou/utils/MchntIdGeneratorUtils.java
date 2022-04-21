package com.yph.shwork.fuiou.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 商户ID生产器
 * @author ming.ma
 * @date 
 */
public class MchntIdGeneratorUtils {
	
	/**
	 * 商户订单号生成规则，MO2111211607
	 * @return
	 */
	public static String getMchntOrderId() {
		String str = new SimpleDateFormat("yyMMddHHmmss").format(new Date());  
		return "MO" + str;
	}
	
	/**
	 * 源订单号生成规则 订单号(28位) = PO单号（补齐12位） + 日期（12位） + 4位随机数（4位）
	 * 
	 * @param poNo po单号
	 * @return
	 */
	public static String getMchntChildOrderId(String poNo) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyMMddHHmmss");
        String newDate=sdf.format(new Date());
        String result="";
        Random random=new Random();
		for (int i = 0; i < 4; i++) {
			result += random.nextInt(10);
		}
		int poLength = 12;
		if(poNo.length() < poLength) {
			poNo = poNo + "A";
			if(poNo.length() < poLength) {
				poNo = poNo + String.format("%0"+(poLength-poNo.length())+"d", 0);
			}
		}else {
			poNo = poNo.substring(0, poLength);
		}
		
        return poNo + newDate + result;
	}

}
