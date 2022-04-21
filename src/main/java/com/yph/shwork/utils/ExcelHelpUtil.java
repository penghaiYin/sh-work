package com.yph.shwork.utils;

import cn.hutool.poi.excel.BigExcelWriter;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Author: sheng.wang
 * @Date: 2021/07/22  11:19
 * @Description:
 */
public class ExcelHelpUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExcelHelpUtil.class);

    public static void flushRows(BigExcelWriter writer) {
        //Excel刷新数据到硬盘
        SXSSFSheet sheet = (SXSSFSheet) writer.getSheet();
        try {
            sheet.flushRows();
        } catch (IOException e) {
            logger.error("Excel刷新行数据到硬盘异常",e);
        }

    }
}
