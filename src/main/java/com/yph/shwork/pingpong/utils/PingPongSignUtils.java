package com.yph.shwork.pingpong.utils;

import com.oigbuy.common.constant.OigFinanceConstant;
import com.oigbuy.common.utils.AlgorithmUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class PingPongSignUtils {

    final static Base64.Encoder encoder = Base64.getEncoder();

    /**
     * 对请求参数的签名校验
     * 签名算法描述：
     * 	所有参数按照key的顺序排序，获得a=value1&b=value2的字符串
     * 	上一步得到的字符串和app_secret做sha1的哈希运算
     * 	上一步得到的字符串做base64编码
     * 	上一步得到的字符串做MD5运算
     * 	上一步得到的字符串转为大写，得到sign信息
     * 	以sign作为key，签名信息作为value，附在请求上: a=value1&b=value2&sign=abc
     * @param params
     * @return
     */
    public static Map<String, Object> generateSign(Map<String, Object> params, String appSecret) throws NoSuchAlgorithmException {
        if (!params.isEmpty()) {
            StringBuilder result = new StringBuilder();
            Map<String, Object> treeMap = new TreeMap<>();
            for (String key : params.keySet()) {
                treeMap.put(key, params.get(key));
            }

            for (Iterator<Map.Entry<String, Object>> iterator = treeMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Object> entry = iterator.next();
                result.append(entry.getKey()).append("=").append(entry.getValue());
                if (iterator.hasNext()) {
                    result.append(OigFinanceConstant.DELIMITER);
                }
            }
            // sha1 上一步得到的String + appSecret DO哈希运算
            String sha1Text = AlgorithmUtils.sha1(result.toString() + appSecret);

            // 上一步得到的字符串DO base64编码
            final String encodedText = encoder.encodeToString(sha1Text.getBytes(StandardCharsets.UTF_8));

            // 上一步得到的字符串DO MD5运算
            String md5Text = AlgorithmUtils.md5(encodedText);
            System.out.println(md5Text.toUpperCase());
            params.put("sign", md5Text.toUpperCase());

        }
        return params;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("app_id","9001060");
        hashMap.put("app_secret","07571C37FF1CD42A");
        hashMap.put("client_id","2111190004017172");
        hashMap.put("account_id","10390020164336385206147");
        hashMap.put("start_time","20220130000000");
        hashMap.put("end_time","20220131000000");

        //E74AFC3FD49535F32CBE506DD2199A4D
        generateSign(hashMap,"07571C37FF1CD42A");
    }
}
