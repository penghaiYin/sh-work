package com.yph.shwork.utils;

import java.util.Map;

/**
 * @author: sheng.wang
 * @date: 2022/01/27  9:56
 * @description:
 */
public class MapTool {

    public static String getKeyByValue(Map<String, String> map, String value) {
        for(Map.Entry<String, String> entry: map.entrySet()){
            if(entry.getValue().equals(value)){
                return entry.getKey();
            }
        }
        return null;
    }
}
