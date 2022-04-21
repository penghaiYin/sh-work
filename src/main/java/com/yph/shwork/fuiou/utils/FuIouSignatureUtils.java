package com.yph.shwork.fuiou.utils;

import com.oigbuy.common.constant.FyConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 参数名ASCII码从小到大排序（字典序）；
 * ◆ 如果参数的值为空不参与签名；
 * ◆ 如果参数为LIST不参与签名；
 * ◆ 参数名区分大小写；
 * ◆ 验证调用返回或富友主动通知签名时，传送的sign参数不参与签名，将生成的签名与该sign值作校验；
 * ◆ 富友接口可能增加字段，验证签名时必须支持增加的扩展字段。
 * 第二步，在stringA最后拼接上key得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，再将得到的字符串所有字符转换为大写，得到sign值signValue。
 */
public class FuIouSignatureUtils {

    //    @Value("{fuIou.secret-key}")
//    private static String secretKey;
//    private static final String secretKey = "3DFDBFE215430F3165970F4376A96487";

    public static String generate20Sign(Object object, String secretKey) {
        StringBuilder result = new StringBuilder();
        Map<String, String> map = new TreeMap<>();
        for (Field field : ReflectionUtils.getAllFields(object.getClass())) {
            field.setAccessible(true);
            try {
                if (!Objects.isNull(field.get(object))
                        && !List.class.isAssignableFrom(field.getType())
                        && !"serialVersionUID".equals(field.getName())) {
                    map.put(field.getName(), field.get(object).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            result.append(entry.getKey()).append("=").append(entry.getValue());
            if (iterator.hasNext()) {
                result.append(FyConstants.DELIMITER);
            }
        }
        String md5Hex = DigestUtils.md5Hex(result.toString() + "&key="+secretKey);
        return md5Hex.toUpperCase();
    }
}
