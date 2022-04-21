package com.yph.shwork.jdy.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.oigbuy.check.batch.service.CheckExportRecordService;
import com.oigbuy.check.common.utils.DateTool;
import com.oigbuy.check.export.util.ExcelAliasUtil;
import com.oigbuy.check.export.util.ExcelStyleUtil;
import com.oigbuy.check.export.util.UploadFileUtil;
import com.oigbuy.check.jdy.dao.JdyLogisticFeeDao;
import com.oigbuy.check.jdy.dto.JdyLogisticFeeDTO;
import com.oigbuy.check.jdy.dto.JdyLogisticFeeExportDTO;
import com.oigbuy.check.jdy.vo.JdyLogisticFeeVO;
import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.pojo.check.export.constant.FileManageConstant;
import com.oigbuy.common.pojo.check.export.entity.CheckExportRecord;
import com.oigbuy.common.pojo.check.export.enums.DataTemplateEnum;
import com.oigbuy.common.pojo.check.export.enums.FileStatusEnum;
import com.oigbuy.common.pojo.check.export.enums.FileTypeEnum;
import com.oigbuy.common.pojo.jdy.JdyLogisticFee;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.common.utils.transform.BeanTransformUtil;
import com.oigbuy.jeesite.common.persistence.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class JdyLogisticService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private JdyLogisticFeeDao jdyLogisticFeeDao;

    @Autowired
    private CheckExportRecordService checkExportRecordService;

    public Page<JdyLogisticFeeVO> findListByPage(JdyLogisticFeeDTO dto) throws Exception {
        initParams(dto);
        Page<JdyLogisticFeeVO> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        PageHelper.startPage(dto.getPageNo(), dto.getPageSize());
        List<JdyLogisticFee> list = jdyLogisticFeeDao.findListByPage(dto);
        PageInfo<JdyLogisticFee> info = new PageInfo<>(list);
        List<JdyLogisticFeeVO> voList = BeanTransformUtil.transformList(JdyLogisticFee.class, JdyLogisticFeeVO.class, list, new ArrayList<>());
        page.setList(transformData(list, voList));
        page.setCount(info.getTotal());
        return page;
    }

    private void initParams(JdyLogisticFeeDTO dto) throws Exception {
        if (dto != null) {
            if (dto.getDateType() != null && dto.getDateType() == 3) {
                if (!StringUtils.isEmpty(dto.getDateStart())) {
                    dto.setDateStartDate(DateTool.getStartDateTime(dto.getDateStart()));
                }
                if (!StringUtils.isEmpty(dto.getDateEnd())) {
                    dto.setDateEndDate(DateTool.getEndDateTime(dto.getDateEnd()));
                }
            }
        }

    }

    private List<JdyLogisticFeeVO> transformData(List<JdyLogisticFee> list, List<JdyLogisticFeeVO> destList) {
        if (CollectionUtils.isEmpty(list)) {
            return destList;
        }
        JdyLogisticFeeVO dest;
        DecimalFormat format = new DecimalFormat("#,##0.00");
        DecimalFormat format4 = new DecimalFormat("#,##0.0000");
        for (int i = 0; i < destList.size(); i++) {
            JdyLogisticFee source = list.get(i);
            dest = destList.get(i);

            dest.setOdooCostAdjust(Objects.isNull(source.getOdooCostAdjust()) ? null : format.format(source.getOdooCostAdjust()));
            dest.setCostExchangeRate(Objects.isNull(source.getCostExchangeRate()) ? null : format4.format(source.getCostExchangeRate()));
            dest.setOdooCostAdjustRmb(Objects.isNull(source.getOdooCostAdjustRmb()) ? null : format.format(source.getOdooCostAdjustRmb()));

            dest.setExpenseAmount(Objects.isNull(source.getExpenseAmount()) ? null : format.format(source.getExpenseAmount()));
            dest.setExpenseExchangeRate(Objects.isNull(source.getExpenseExchangeRate()) ? null : format4.format(source.getExpenseExchangeRate()));
            dest.setActualExpenseAmount(Objects.isNull(source.getActualExpenseAmount()) ? null : format.format(source.getActualExpenseAmount()));
            dest.setActualExpenseRmb(Objects.isNull(source.getActualExpenseRmb()) ? null : format.format(source.getActualExpenseRmb()));

            dest.setExpenseSubtractCost(Objects.isNull(source.getExpenseSubtractCost()) ? null : format.format(source.getExpenseSubtractCost()));
            dest.setAdjustAmount(Objects.isNull(source.getAdjustAmount()) ? null : format.format(source.getAdjustAmount()));
        }
        return destList;
    }


    public CheckExportRecord exportExcel(JdyLogisticFeeExportDTO dto, DataTemplateEnum dataTemplate) throws Exception {
        List<JdyLogisticFeeVO> exportList = getExportList(dto);
        CheckExportRecord exportRecord = createExportRecord(dto, dataTemplate, exportList);
        excelDownload(exportList, exportRecord);
        checkExportRecordService.saveExportRecord(exportRecord);
        return exportRecord;
    }

    public List<JdyLogisticFeeVO> getExportList(JdyLogisticFeeExportDTO dto) throws Exception {
        List<JdyLogisticFee> list = null;
        //导出勾选
        if (FileManageConstant.CHECK_EXPORT.equals(dto.getExportType())) {
            if (CollectionUtils.isEmpty(dto.getIdList())) {
                throw new BusiException("没有勾选");
            }
            List<Integer> ids = dto.getIdList();
            list = jdyLogisticFeeDao.findListByIds(ids);


        } else if (FileManageConstant.ALL_EXPORT.equals(dto.getExportType())) {
            //导出查询结果
            JdyLogisticFeeDTO queryParam = dto.getQueryParam();
            initParams(queryParam);
            list = jdyLogisticFeeDao.findListByPage(queryParam);
        } else {
            throw new BusiException("导出类型参数错误");
        }
        List<JdyLogisticFeeVO> voList = BeanTransformUtil.transformList(JdyLogisticFee.class, JdyLogisticFeeVO.class, list, new ArrayList<>());
        transformData(list, voList);
        return voList;
    }

    private CheckExportRecord createExportRecord(JdyLogisticFeeExportDTO dto, DataTemplateEnum dataTemplate, List<JdyLogisticFeeVO> exportList) {
        CheckExportRecord exportRecord = new CheckExportRecord();
        exportRecord.setType(dto.getExportType());
        exportRecord.setUploadDownloadFlag(FileTypeEnum.DOWNLOAD.getCode());
        exportRecord.setCreateId(dto.getUserId());
        exportRecord.setDataTemplate(dataTemplate.getCode());
        exportRecord.setQueryParamData(FileManageConstant.CHECK_EXPORT.equals(dto.getExportType()) ? JSON.toJSONString(dto.getIdList()) : JSON.toJSONString(dto.getQueryParam()));
        exportRecord.setRowNumber(exportList.size());
        return exportRecord;
    }

    private void excelDownload(List<JdyLogisticFeeVO> exportList, CheckExportRecord exportRecord) {
        String filenamePrefix = DataTemplateEnum.JDY_LOGISTIC_FEE.getName() + "导出列表";
        String fileName = filenamePrefix + DateUtils.getDate("yyyy-MM-dd") + ".xlsx";
        exportRecord.setExportFileName(fileName);
        String ossKey = String.format(filenamePrefix + "%s_%s.xlsx", DateUtils.getDate("yyyy-MM-dd"),
                System.currentTimeMillis() + String.format("%04d", new SecureRandom().nextInt(10000)));
        BigExcelWriter writer = null;
        logger.debug("Excel导出执行开始。。。");
        try {
            writer = ExcelUtil.getBigWriter(ossKey);
            writer.renameSheet(0, filenamePrefix);
            ExcelAliasUtil.addJdyLogisticFeeHeaderAlias(writer);//设置字段别名
            SXSSFSheet sheet = (SXSSFSheet) writer.getSheet();
            sheet.setRandomAccessWindowSize(-1);// 无限制访问sheet
            writer.write(exportList, true);
            writer.setColumnWidth(-1, 22);// 设置宽度自适应
            ExcelStyleUtil.customLogisticFeeColumnLength(writer);// 自定义宽度
            ExcelStyleUtil.customLogisticFeeColumnStyle(writer);
            logger.info("Excel导出执行结束。。。");

            uploadToOSS(exportRecord, ossKey, writer);// excel上传到oss

        } catch (Exception e) {
            e.printStackTrace();
            exportRecord.setDealEndTime(new Date());
            exportRecord.setStatus(FileStatusEnum.EXCEPTION.getCode());
            exportRecord.setMessage(e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    SXSSFWorkbook workbook = (SXSSFWorkbook) writer.getWorkbook();
                    workbook.dispose();
                    IoUtil.close(workbook);
                    workbook = null;
                }
            } catch (Exception e) {

            }
        }
    }

    private void uploadToOSS(CheckExportRecord exportRecord, String ossKey, BigExcelWriter writer) {
        String ossUrl = UploadFileUtil.uploadToOss(ossKey, writer);
        if (StringUtils.isBlank(ossUrl)) {
            throw new RuntimeException("上传oss失败!");
        }
        exportRecord.setOssKey(ossKey);
        exportRecord.setOssUrl(ossUrl);
        exportRecord.setDealEndTime(new Date());
        exportRecord.setStatus(FileStatusEnum.FINISH.getCode());
    }
}
