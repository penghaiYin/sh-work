package com.yph.shwork.utils;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @Author: sheng.wang
 * @Date: 2021/07/22  11:19
 * @Description:
 */
public class DownloadFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(DownloadFileUtil.class);

    public static ExcelReader getExcelReaderByUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
//        File url = new File(urlStr);//本地测试用
//        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(url));//本地测试用
        ExcelReader reader = ExcelUtil.getReader(bis);
        bis.close();
        return reader;
    }
}
