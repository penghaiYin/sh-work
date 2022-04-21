package com.yph.shwork.utils;

import cn.hutool.poi.excel.BigExcelWriter;
import com.oigbuy.common.utils.oss.OssClientUtils2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @Author: sheng.wang
 * @Date: 2021/07/22  11:19
 * @Description:
 */
public class UploadFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtil.class);

    public static String uploadToOss(String ossKey, BigExcelWriter writer) {
        ByteArrayOutputStream bos = null;
        String ossUrl = null;
        try {
            bos = new ByteArrayOutputStream();
            writer.flush(bos);
            InputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
            ossUrl = OssClientUtils2.uploadFile(ossKey, inputStream);
            System.out.println("Excel文件上传oss成功，ossUrl:" + ossUrl);
            return ossUrl;
        } catch (Exception e) {
            logger.error("导入Excel,上传到oss出现异常");
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
